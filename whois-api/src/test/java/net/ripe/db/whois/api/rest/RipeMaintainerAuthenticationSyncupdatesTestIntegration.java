package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.client.NotifierCallback;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class RipeMaintainerAuthenticationSyncupdatesTestIntegration extends AbstractIntegrationTest {

    @Autowired IpRanges ipRanges;

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
    public void sync_update_from_outside_ripe_network() throws Exception {
        ipRanges.setTrusted("53.67.0.1");

        String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(RPSL_PERSON_WITH_RIPE_MAINTAINER) + "&NEW=yes", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network"));
    }

    @Test
    public void sync_update_from_within_ripe_network() throws Exception {
        ipRanges.setTrusted("127.0.0.1", "::1");

        String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(RPSL_PERSON_WITH_RIPE_MAINTAINER) + "&NEW=yes", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [person] TP1-TEST   TEST Person"));
        assertThat(response, not(containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network")));
    }
}
