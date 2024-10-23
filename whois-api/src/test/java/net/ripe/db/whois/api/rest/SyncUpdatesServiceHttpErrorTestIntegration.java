package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.ClientErrorException;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class SyncUpdatesServiceHttpErrorTestIntegration extends AbstractIntegrationTest {

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

    @BeforeAll
    public static void setup() {
        System.setProperty("syncupdates.http.error", "true");
    }

    @AfterAll
    public static void teardown() {
        System.clearProperty("syncupdates.http.error");
    }

    @Test
    public void create_object_only_data_parameter_over_http() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final ClientErrorException clientErrorException = assertThrows(ClientErrorException.class, () -> {
            RestTest.target(getPort(), "whois/syncupdates/test?" +
                            "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                    .request()
                    .get(String.class);
        });

        assertThat(clientErrorException.getMessage(), is("HTTP 426 Upgrade Required"));
        assertThat(clientErrorException.getResponse().getStatus(), is(HttpStatus.UPGRADE_REQUIRED_426));
        assertThat(clientErrorException.getResponse().readEntity(String.class), containsString("Please switch to HTTPS to continue using HTTPS."));
    }
}
