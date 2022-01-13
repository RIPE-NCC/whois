package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

@org.junit.jupiter.api.Tag("IntegrationTest")
public class WhoisRestServiceAclTestIntegration extends AbstractIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
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
        databaseHelper.getAclTemplate().update("DELETE FROM acl_denied");
        databaseHelper.getAclTemplate().update("DELETE FROM acl_event");
        ipResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();
    }

    @Test
    public void lookup_person_ok() throws Exception {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);

        assertThat(response, containsString("<object type=\"person\">"));
    }

    @Test
    public void lookup_person_acl_denied() throws Exception {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
                fail();
            } catch (ClientErrorException e) {
                assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
                assertOnlyErrorMessage(e, "Error", "ERROR:201: access denied for %s\n\nSorry, access from your host has been permanently\ndenied because of a repeated excessive querying.\nFor more information, see\nhttp://www.ripe.net/data-tools/db/faq/faq-db/why-did-you-receive-the-error-201-access-denied\n", "127.0.0.1");
            }
        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_person_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        try {
            accessControlListManager.accountPersonalObjects(localhost, accessControlListManager.getPersonalObjects(localhost) + 1);

            try {
                RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
                fail();
            } catch (ClientErrorException e) {
                assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
            }
        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_person_filtered_acl_still_counted() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        try {
            final int limit = accessControlListManager.getPersonalObjects(localhost);

            final WhoisResources whoisResources =  RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                                                    .request()
                                                    .get(WhoisResources.class);

            assertThat(whoisResources.getWhoisObjects().get(0).getAttributes()
                            .stream()
                            .anyMatch( (attribute)-> attribute.getName().equals(AttributeType.E_MAIL)),
                        is(false));

            final int remaining = accessControlListManager.getPersonalObjects(localhost);
            assertThat(remaining, is(limit-1));

        } finally {
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_autnum_ok() {
        final String response = RestTest.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);

        assertThat(response, containsString("<object type=\"aut-num\">"));
    }

    @Test
    public void lookup_autnum_acl_denied() throws Exception {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                RestTest.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);
                fail();
            } catch (ClientErrorException e) {
                assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
            }
        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_autnum_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        accessControlListManager.accountPersonalObjects(localhost, accessControlListManager.getPersonalObjects(localhost) + 1);

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void lookup_version_acl_denied() throws Exception {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                        .request(MediaType.APPLICATION_JSON)
                        .get(WhoisResources.class);
                fail();
            } catch (ClientErrorException e) {
                assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
                assertOnlyErrorMessage(e, "Error", "ERROR:201: access denied for %s\n\nSorry, access from your host has been permanently\ndenied because of a repeated excessive querying.\nFor more information, see\nhttp://www.ripe.net/data-tools/db/faq/faq-db/why-did-you-receive-the-error-201-access-denied\n", "127.0.0.1");
            }

        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_version_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        try {
            accessControlListManager.accountPersonalObjects(localhost, accessControlListManager.getPersonalObjects(localhost) + 1);

            try {
                RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                        .request(MediaType.APPLICATION_JSON)
                        .get(WhoisResources.class);
                fail();
            } catch (ClientErrorException e) {
                assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
            }
        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void diff_version_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        try {
            accessControlListManager.accountPersonalObjects(localhost, accessControlListManager.getPersonalObjects(localhost) + 1);

            assertThat(TelnetWhoisClient.queryLocalhost(QueryServer.port, "--diff-versions 1 TP1-TEST"),
                    containsString(" Access from your host has been temporarily denied."));

            assertThat( TelnetWhoisClient.queryLocalhost(QueryServer.port, "--show-version 1 TP1-TEST"),
                    containsString(" Access from your host has been temporarily denied."));

        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }
}
