package net.ripe.db.whois.api.log;

import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.FileHelper;
import org.hamcrest.Matchers;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

import static net.ripe.db.whois.common.rpsl.RpslObjectFilter.buildGenericObject;
import static org.junit.Assert.assertThat;

public class AuditLogTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:        OWNER-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:        OWNER-MNT\n" +
            "referral-by:   OWNER-MNT\n" +
            "changed:       dbtest@ripe.net 20120101\n" +
            "source:        TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:        Test Person\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TP1-TEST\n" +
            "mnt-by:        OWNER-MNT\n" +
            "changed:       dbtest@ripe.net 20120101\n" +
            "source:        TEST\n");

    @Value("${dir.update.audit.log}")
    private String auditLog;

    private RestClient restClient;

    @Before
    public void setup() throws Exception {
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);

        restClient = new RestClient(String.format("http://localhost:%d/whois", getPort()), "TEST");
    }

    @Test
    public void update_passes_x_forwarded_for() {
        final RpslObject updatedPerson = buildGenericObject(TEST_PERSON, "remarks: i will be back");

        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", "test")
                .update(updatedPerson);

        String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/170000.rest_10.20.30.40_0/000.audit.xml.gz"));

        assertThat(audit, Matchers.containsString("<message><![CDATA[Header: X-Forwarded-For=10.20.30.40]]></message>"));
    }

    @Test
    public void delete_passes_x_forwarded_for() {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        databaseHelper.addObject(secondPerson);

        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", "test")
                .delete(secondPerson);

        String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/170000.rest_10.20.30.40_0/000.audit.xml.gz"));

        assertThat(audit, Matchers.containsString("<message><![CDATA[Header: X-Forwarded-For=10.20.30.40]]></message>"));
    }

    @Test
    public void create_passes_x_forwarded_for() {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");

        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", "test")
                .create(secondPerson);

        String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/170000.rest_10.20.30.40_0/000.audit.xml.gz"));

        assertThat(audit, Matchers.containsString("<message><![CDATA[Header: X-Forwarded-For=10.20.30.40]]></message>"));
    }
}
