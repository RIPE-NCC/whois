package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class RipeMaintainerAuthenticationRestTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject TEST_PERSON = RpslObject.parse(
            "person:        Test Person\n" +
            "address:       Singel 258\n" +
            "phone:         +31-1234567890\n" +
            "e-mail:        ppalse@ripe.net\n" +
            "mnt-by:        RIPE-NCC-HM-MNT\n" +
            "nic-hdl:       AUTO-1\n" +
            "source:        TEST");

    @Autowired private IpRanges ipRanges;
    @Autowired private WhoisObjectMapper whoisObjectMapper;

    @Before
    public void setup() {
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse(
                        "mntner:  RIPE-NCC-HM-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "mnt-by:  RIPE-NCC-HM-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # emptypassword" +
                        "source:  TEST"),
                RpslObject.parse(
                        "person:  Admin Person\n" +
                        "address: Admin Road\n" +
                        "address: Town\n" +
                        "address: UK\n" +
                        "phone:   +44 282 411141\n" +
                        "nic-hdl: TEST-RIPE\n" +
                        "mnt-by:  TST-MNT\n" +
                        "source:  TEST"),
                RpslObject.parse("" +
                        "mntner:  TST-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "mnt-by:  TST-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "source:  TEST")));
    }

    @Test
    public void rest_api_update_from_outside_ripe_network() {
        ipRanges.setTrusted("53.67.0.1");

        try {
            RestTest.target(getPort(), "whois/test/person?password=emptypassword")
                    .request()
                    .post(Entity.entity(map(TEST_PERSON), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Authentication by RIPE NCC maintainers only allowed from within the RIPE NCC network"));
        }
    }

    @Test
    public void rest_api_update_from_within_ripe_network() throws IOException {
        ipRanges.setTrusted("127.0.0.1", "::1");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=emptypassword")
                .request()
                .post(Entity.entity(map(TEST_PERSON), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    // helper methods

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

}
