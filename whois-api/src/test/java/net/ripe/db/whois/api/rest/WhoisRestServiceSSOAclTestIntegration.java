package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.acl.SSOResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;

import static net.ripe.db.whois.api.RestTest.assertOnlyErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class WhoisRestServiceSSOAclTestIntegration extends AbstractIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";
    public static final String VALID_TOKEN_USER_NAME = "person@net.net";
    public static final UserSession VALID_TOKEN_USER_SESSION = new UserSession("aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef","person@net.net", "Test User", true, "2033-01-30T16:38:27.369+11:00");
    public static final String VALID_TOKEN = "valid-token";

    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private SSOResourceConfiguration ssoResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject(
                "person:    Test Person\n" +
                "nic-hdl:   TP1-TEST\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "mntner:    OWNER-MNT\n" +
                "source:    TEST");
        databaseHelper.addObject("aut-num:   AS102\n" + "source:    TEST\n");
    }

    @AfterEach
    public void reset() throws Exception {
        databaseHelper.clearAclTables();

        ipResourceConfiguration.reload();
        ssoResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();
    }

    @Test
    public void lookup_person_using_sso_acl_denied() {
        databaseHelper.insertAclSSODenied(VALID_TOKEN_USER_NAME);
        ssoResourceConfiguration.reload();

        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                        .request()
                        .cookie("crowd.token_key", VALID_TOKEN)
                        .get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
            assertOnlyErrorMessage(e, "Error", "ERROR:201: access denied for %s\n\nSorry, access from your host has been permanently\ndenied because of a repeated excessive querying.\nFor more information, see\nhttps://docs.db.ripe.net/FAQ/#why-did-i-receive-an-error-201-access-denied\n", "person@net.net");
        }
    }

    @Test
    public void lookup_person_using_sso_acl_remote_addr_denied() {
        databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
        ipResourceConfiguration.reload();

        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                        .request()
                        .cookie("crowd.token_key", VALID_TOKEN)
                        .get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
            assertOnlyErrorMessage(e, "Error", "ERROR:201: access denied for %s\n\nSorry, access from your host has been permanently\ndenied because of a repeated excessive querying.\nFor more information, see\nhttps://docs.db.ripe.net/FAQ/#why-did-i-receive-an-error-201-access-denied\n", "127.0.0.1");
        }
    }

    @Test
    public void lookup_person_using_sso_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = accessControlListManager.getAccountingIdentifier(localhost, VALID_TOKEN_USER_SESSION.getUsername());

        accessControlListManager.accountPersonalObjects(accountingIdentifier, accessControlListManager.getPersonalObjects(accountingIdentifier) + 1);

        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                        .request()
                        .cookie("crowd.token_key", VALID_TOKEN)
                        .get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void lookup_person_using_sso_acl_counted_no_ip_counted() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedByIP = testPersonalObjectAccounting.getQueriedPersonalObjects(localhost);
        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);

        final WhoisResources whoisResources =  RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                    .request()
                    .cookie("crowd.token_key", VALID_TOKEN)
                    .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes()
                            .stream()
                            .anyMatch( (attribute)-> attribute.getName().equals(AttributeType.E_MAIL.getName())),
                    is(false));

        final int accountedByIp = testPersonalObjectAccounting.getQueriedPersonalObjects(localhost);
        assertThat(accountedByIp, is(queriedByIP));

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);
        assertThat(accountedBySSO, is(queriedBySSO + 1));
    }

    @Test
    public void lookup_person_using_sso_no_acl_for_unlimited_remoteAddr() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = accessControlListManager.getAccountingIdentifier(localhost, VALID_TOKEN_USER_SESSION.getUsername());

        databaseHelper.insertAclIpLimit(LOCALHOST_WITH_PREFIX, -1, true);
        ipResourceConfiguration.reload();

        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int limit = accessControlListManager.getPersonalObjects(accountingIdentifier);

        final Response response =  RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                .request()
                .cookie("crowd.token_key", VALID_TOKEN)
                .get(Response.class);

        assertThat(response.getStatus(), is(HttpStatus.OK_200));

        final int remaining = accessControlListManager.getPersonalObjects(accountingIdentifier);
        assertThat(remaining, is(limit));
    }

    @Test
    public void lookup_person_belong_to_user_using_sso_acl_is_not_accounted() {
        databaseHelper.addObject(
                "mntner:      USER-OWNING-MNT\n" +
                        "descr:       Owner Maintainer\n" +
                        "admin-c:     TP1-TEST\n" +
                        "auth:        SSO person@net.net\n" +
                        "mnt-by:      USER-OWNING-MNT\n" +
                        "source:      TEST");

        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "mnt-by:   USER-OWNING-MNT\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);

        RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                .request()
                .cookie("crowd.token_key", VALID_TOKEN)
                .get(WhoisResources.class);

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);
        assertThat(accountedBySSO, is(queriedBySSO ));
    }

    @Test
    public void lookup_person_do_not_belong_to_user_using_sso_acl_is_accounted() {
        databaseHelper.addObject(
                "mntner:      USER-OWNING-MNT\n" +
                        "descr:       Owner Maintainer\n" +
                        "admin-c:     TP1-TEST\n" +
                        "auth:        SSO test@ripe.net\n" +
                        "mnt-by:      USER-OWNING-MNT\n" +
                        "source:      TEST");

        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "mnt-by:   USER-OWNING-MNT\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);

        RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                .request()
                .cookie("crowd.token_key", VALID_TOKEN)
                .get(WhoisResources.class);

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);
        assertThat(accountedBySSO, is(queriedBySSO +1 ));
    }
}
