package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestClient;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Ignore("TODO: ignored until WhoisProfile.isDeployed() check is removed from Authenticator")
@Category(IntegrationTest.class)
public class RipeMaintainerAuthenticationRestTestIntegration extends AbstractIntegrationTest {

    @Autowired IpRanges ipRanges;

    @Before
    public void setup() throws Exception {
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse(
                        "mntner:  RIPE-NCC-HM-MNT\n" +
                                "descr:   description\n" +
                                "admin-c: TEST-RIPE\n" +
                                "mnt-by:  RIPE-NCC-HM-MNT\n" +
                                "referral-by: RIPE-NCC-HM-MNT\n" +
                                "upd-to:  dbtest@ripe.net\n" +
                                "auth:    MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # emptypassword" +
                                "changed: dbtest@ripe.net 20120707\n" +
                                "source:  TEST"),
                RpslObject.parse(
                        "person:  Admin Person\n" +
                                "address: Admin Road\n" +
                                "address: Town\n" +
                                "address: UK\n" +
                                "phone:   +44 282 411141\n" +
                                "nic-hdl: TEST-RIPE\n" +
                                "mnt-by:  TST-MNT\n" +
                                "changed: dbtest@ripe.net 20120101\n" +
                                "source:  TEST"),
                RpslObject.parse("" +
                        "mntner:  TST-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "mnt-by:  TST-MNT\n" +
                        "referral-by: TST-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "changed: dbtest@ripe.net 20120707\n" +
                        "source:  TEST")));
    }

    @Test
    public void rest_api_update_from_outside_ripe_network() throws IOException {
        ipRanges.setTrusted("53.67.0.1");
        final String person = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
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
            RestClient.target(getPort(), "whois/test/person?password=emptypassword")
                    .request()
                    .post(Entity.entity(person, MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class), Matchers.containsString("Unauthorized"));
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
            RestClient.target(getPort(), "whois/test/person?password=emptypassword")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Unauthorized"));
        }
    }
}
