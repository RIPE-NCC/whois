package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class SyncUpdatesServiceHttpsTestIntegration extends AbstractIntegrationTest {

    private static final String MNTNER_TEST_MNTNER = "" +
            "mntner:        mntner-mnt\n" +
            "descr:         description\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "notify:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$TTjmcwVq$zvT9UcvASZDQJeK8u9sNU.    # emptypassword\n" +
            "mnt-by:        mntner-mnt\n" +
            "source:        TEST";

    private static final String PERSON_ANY1_TEST = "" +
            "person:        Test Person\n" +
            "nic-hdl:       TP1-TEST\n" +
            "source:        TEST";

    @Test
    public void create_object_only_data_parameter_over_http() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
        assertThat(response, containsString(
            "This Syncupdates request used insecure HTTP, which will be removed\n" +
                    "            in a future release. Please switch to HTTPS."));
    }
}
