package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.acl.PersonalAccountingManager;
import net.ripe.db.whois.query.acl.SSOResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.MediaType;
import java.net.InetAddress;

import static net.ripe.db.whois.api.RestTest.assertOnlyErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class WhoisRestServiceIpAclTestIntegration extends AbstractIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";
    public static final String VALID_TOKEN_USER_NAME = "person@net.net";
    public static final String VALID_TOKEN = "valid-token";

    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private SSOResourceConfiguration ssoResourceConfiguration;

    @BeforeAll
    public static void setProperties() {
        System.setProperty("personal.accounting.by.sso", "false");
    }
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
        System.setProperty("personal.accounting.by.sso", "false");

        databaseHelper.clearAclTables();

        ipResourceConfiguration.reload();
        ssoResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();
    }

    @Test
    public void lookup_person_ok() {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);

        assertThat(response, containsString("<object type=\"person\">"));
    }

    @Test
    public void lookup_person_acl_denied() {
        databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
        ipResourceConfiguration.reload();

        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
            assertOnlyErrorMessage(e, "Error", "ERROR:201: access denied for %s\n\nSorry, access from your host has been permanently\ndenied because of a repeated excessive querying.\nFor more information, see\nhttps://apps.db.ripe.net/docs/FAQ/#why-did-i-receive-an-error-201-access-denied\n", "127.0.0.1");
        }
    }

    @Test
    public void lookup_person_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = new AccountingIdentifier(localhost, null);

        accessControlListManager.accountPersonalObjects(accountingIdentifier, accessControlListManager.getPersonalObjects(accountingIdentifier) + 1);

        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void lookup_person_filtered_acl_still_counted() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = new AccountingIdentifier(localhost, null);

        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int limit = accessControlListManager.getPersonalObjects(accountingIdentifier);

        final WhoisResources whoisResources =  RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                                                    .request()
                                                    .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes()
                            .stream()
                            .anyMatch( (attribute)-> attribute.getName().equals(AttributeType.E_MAIL)),
                        is(false));

        final int remaining = accessControlListManager.getPersonalObjects(accountingIdentifier);
        assertThat(remaining, is(limit-1));
    }

    @Test
    public void lookup_autnum_ok() {
        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);

        assertThat(response, containsString("<object type=\"aut-num\">"));
    }

    @Test
    public void lookup_autnum_acl_denied() {
        databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
        ipResourceConfiguration.reload();

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void lookup_autnum_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = new AccountingIdentifier(localhost, null);

        accessControlListManager.accountPersonalObjects(accountingIdentifier, accessControlListManager.getPersonalObjects(accountingIdentifier) + 1);

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void lookup_version_acl_denied() {
        databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
        ipResourceConfiguration.reload();

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                        .request(MediaType.APPLICATION_JSON)
                        .get(WhoisResources.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
            assertOnlyErrorMessage(e, "Error", "ERROR:201: access denied for %s\n\nSorry, access from your host has been permanently\ndenied because of a repeated excessive querying.\nFor more information, see\nhttps://apps.db.ripe.net/docs/FAQ/#why-did-i-receive-an-error-201-access-denied\n", "127.0.0.1");
        }
    }

    @Test
    public void lookup_version_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = new AccountingIdentifier(localhost, null);

        accessControlListManager.accountPersonalObjects(accountingIdentifier, accessControlListManager.getPersonalObjects(accountingIdentifier) + 1);

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                        .request(MediaType.APPLICATION_JSON)
                        .get(WhoisResources.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void diff_version_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = new AccountingIdentifier(localhost, null);

        accessControlListManager.accountPersonalObjects(accountingIdentifier, accessControlListManager.getPersonalObjects(accountingIdentifier) + 1);

        assertThat(TelnetWhoisClient.queryLocalhost(QueryServer.port, "--diff-versions 1 TP1-TEST"),
                    containsString(" Access from your host has been temporarily denied."));

        assertThat( TelnetWhoisClient.queryLocalhost(QueryServer.port, "--show-version 1 TP1-TEST"),
                    containsString(" Access from your host has been temporarily denied."));
    }

    @Test
    public void lookup_person_acl_counted_by_ip_sso_disabled() throws Exception {
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
                        .anyMatch( (attribute)-> attribute.getName().equals(AttributeType.E_MAIL)),
                is(false));

        final int accountedByIp = testPersonalObjectAccounting.getQueriedPersonalObjects(localhost);
        assertThat(accountedByIp, is(queriedByIP+1));

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);
        assertThat(accountedBySSO, is(queriedBySSO));
    }
}
