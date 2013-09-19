package net.ripe.db.whois;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.mail.internet.MimeMessage;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Ignore("TODO: ignored until WhoisProfile.isDeployed() check is removed from Authenticator")
@ContextConfiguration(locations = {"classpath:applicationContext-whois-test.xml"})
@Category(IntegrationTest.class)
public class RipeMaintainerAuthenticationTestIntegration extends AbstractRestClientTest {
    @Autowired IpRanges ipRanges;
    @Autowired MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired MailSenderStub mailSenderStub;

    private static final Audience AUDIENCE = Audience.PUBLIC;

    private static final String RPSL_PERSON_WITH_RIPE_MAINTAINER = "" +
            "person:    TEST Person\n" +
            "address:   Singel 258\n" +
            "e-mail:    ppalse@ripe.net\n" +
            "phone:     +311234567\n" +
            "mnt-by:    RIPE-NCC-HM-MNT\n" +
            "nic-hdl:   AUTO-1\n" +
            "changed:   ppalse@ripe.net 20120303\n" +
            "source:    TEST\n" +
            "password:  emptypassword";

    @Before
    public void setup() throws Exception {
        final RpslObject rootMntner = RpslObject.parse("" +
                "mntner:  RIPE-NCC-HM-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  RIPE-NCC-HM-MNT\n" +
                "referral-by: RIPE-NCC-HM-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # emptypassword" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        final RpslObject adminPerson = RpslObject.parse("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST");

        final RpslObject normalMntner = RpslObject.parse("" +
                "mntner:  TST-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        databaseHelper.addObjects(Lists.newArrayList(rootMntner, adminPerson, normalMntner));
    }

    @Test
    public void rest_api_update_from_outside_ripe_network() throws IOException {
        ipRanges.setTrusted("53.67.0.1");
        final String person =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                        "<whois-resources>\n" +
                        "  <objects>\n" +
                        "    <object type=\"person\">\n" +
                        "      <source id=\"test\"/>\n" +
                        "      <attributes>\n" +
                        "        <attribute name=\"person\" value=\"Test Person\"/>\n" +
                        "        <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                        "        <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                        "        <attribute name=\"e-mail\" value=\"ppalse@ripe.net\"/>\n" +
                        "        <attribute name=\"mnt-by\" value=\"RIPE-NCC-HM-MNT\"/>\n" +
                        "        <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                        "        <attribute name=\"changed\" value=\"ppalse@ripe.net 20101228\"/>\n" +
                        "        <attribute name=\"source\" value=\"TEST\"/>\n" +
                        "      </attributes>\n" +
                        "    </object>\n" +
                        "  </objects>\n" +
                        "</whois-resources>\n";

        try {
            createResource(AUDIENCE, "whois/test/person?password=emptypassword")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Unauthorized"));
        }
    }

    @Test
    public void rest_api_update_from_within_ripe_network() throws IOException {
        ipRanges.setTrusted("127.0.0.1", "::1");
        final String person =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                        "<whois-resources>\n" +
                        "  <objects>\n" +
                        "    <object type=\"person\">\n" +
                        "      <source id=\"test\"/>\n" +
                        "      <attributes>\n" +
                        "        <attribute name=\"person\" value=\"Test Person\"/>\n" +
                        "        <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                        "        <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                        "        <attribute name=\"e-mail\" value=\"ppalse@ripe.net\"/>\n" +
                        "        <attribute name=\"mnt-by\" value=\"RIPE-NCC-HM-MNT\"/>\n" +
                        "        <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                        "        <attribute name=\"changed\" value=\"ppalse@ripe.net 20101228\"/>\n" +
                        "        <attribute name=\"source\" value=\"TEST\"/>\n" +
                        "      </attributes>\n" +
                        "    </object>\n" +
                        "  </objects>\n" +
                        "</whois-resources>\n";

        try {
            createResource(AUDIENCE, "whois/test/person?password=emptypassword")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Unauthorized"));
        }
    }

    @Test
    public void mail_update_from_outside_ripe_network() throws Exception {
        ipRanges.setTrusted("53.67.0.1");

        final String response = mailUpdatesTestSupport.insert("NEW", RPSL_PERSON_WITH_RIPE_MAINTAINER);

        final MimeMessage message = mailSenderStub.getMessage(response);

        assertThat(message.getContent().toString(), containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network"));
    }

    @Test
    public void mail_update_from_within_ripe_network() throws Exception {
        ipRanges.setTrusted("127.0.0.1", "::1");

        final String response = mailUpdatesTestSupport.insert("NEW", RPSL_PERSON_WITH_RIPE_MAINTAINER);

        final MimeMessage message = mailSenderStub.getMessage(response);

        assertThat(message.getContent().toString(), containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network"));
    }

    @Test
    public void sync_update_from_outside_ripe_network() throws Exception {
        ipRanges.setTrusted("53.67.0.1");

        final String response = WhoisFixture.syncupdate(jettyConfig, RPSL_PERSON_WITH_RIPE_MAINTAINER,
                false, false, true, false, true, 200);

        assertThat(response, containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network"));
    }

    @Test
    public void sync_update_from_within_ripe_network() throws Exception {
        ipRanges.setTrusted("127.0.0.1", "::1");

        final String response = WhoisFixture.syncupdate(jettyConfig, RPSL_PERSON_WITH_RIPE_MAINTAINER,
                false, false, true, false, true, 200);

        assertThat(response, not(containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network")));
    }

    @Override
    protected WebTarget createResource(final Audience audience, final String path) {
        return client.target(String.format("http://localhost:%s/%s", getPort(audience), path));
    }
}
