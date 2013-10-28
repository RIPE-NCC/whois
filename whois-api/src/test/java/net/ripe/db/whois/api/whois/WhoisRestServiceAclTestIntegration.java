package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestClient;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.ClientErrorException;
import java.net.InetAddress;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class WhoisRestServiceAclTestIntegration extends AbstractIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @Before
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

    @After
    public void reset() throws Exception {
        databaseHelper.getAclTemplate().update("DELETE FROM acl_denied");
        databaseHelper.getAclTemplate().update("DELETE FROM acl_event");
        ipResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();
    }

    @Test
    public void lookup_person_ok() throws Exception {
        final String response = RestClient.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);

        assertThat(response, containsString("<object type=\"person\">"));
    }

    @Test
    public void lookup_person_acl_denied() throws Exception {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                RestClient.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
                fail();
            } catch (ClientErrorException e) {
                assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
                assertThat(e.getResponse().readEntity(String.class), containsString("Error 429"));          // TODO: HTML response should be plaintext, and doesn't include acl denied message
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
                RestClient.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
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
    public void lookup_autnum_ok() {
        final String response = RestClient.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);

        assertThat(response, containsString("<object type=\"aut-num\">"));
    }

    @Test
    public void lookup_autnum_acl_denied() throws Exception {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                RestClient.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);
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
            RestClient.target(getPort(), "whois/test/aut-num/AS102").request().get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }
}
