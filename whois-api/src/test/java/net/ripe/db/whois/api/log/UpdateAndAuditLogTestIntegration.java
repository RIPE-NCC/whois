package net.ripe.db.whois.api.log;

import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.api.rest.RestClientUtils;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.support.FileHelper;
import net.ripe.db.whois.update.support.TestUpdateLog;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;

import static net.ripe.db.whois.common.rpsl.RpslObjectFilter.buildGenericObject;
import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class UpdateAndAuditLogTestIntegration extends AbstractIntegrationTest {

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
            "source:        TEST");

    @Value("${dir.update.audit.log}")
    String auditLog;

    @Autowired TestUpdateLog updateLog;
    @Autowired CrowdClient crowdClient;

    private RestClient restClient;

    @Before
    public void setup() throws Exception {
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T13:00:00"));
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);

        restClient = new RestClient(String.format("http://localhost:%d/whois", getPort()), "TEST");
    }

    @After
    public void tearDown() throws Exception {
        cleanupAuditLogDirectory();
    }

    @Test
    public void create_gets_logged() {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", "test")
                .create(secondPerson);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("<message><![CDATA[Header: X-Forwarded-For=10.20.30.40]]></message>"));
        assertThat(audit, containsString("<message><![CDATA[/whois/TEST/person?password=test]]></message>"));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         Test Person"));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - WhoisRestApi(10.20.30.40)"));
    }

    @Test
    public void update_gets_logged() {
        final RpslObject updatedPerson = buildGenericObject(TEST_PERSON, "remarks: i will be back");
        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", "test")
                .update(updatedPerson);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("<message><![CDATA[Header: X-Forwarded-For=10.20.30.40]]></message>"));
        assertThat(audit, containsString("<message><![CDATA[/whois/TEST/person/TP1-TEST?password=test]]></message>"));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         Test Person"));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/002.ack.txt.gz"));
        assertThat(ack, containsString("Modify SUCCEEDED: [person] TP1-TEST   Test Person"));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD MODIFY person\\s+TP1-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - WhoisRestApi(10.20.30.40)"));
    }

    @Test
    public void delete_gets_logged() {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        databaseHelper.addObject(secondPerson);
        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", "test")
                .delete(secondPerson);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("<message><![CDATA[Header: X-Forwarded-For=10.20.30.40]]></message>"));
        assertThat(audit, containsString("<message><![CDATA[/whois/TEST/person/TP2-TEST?password=test]]></message>"));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         Test Person"));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_0/002.ack.txt.gz"));
        assertThat(ack, containsString("Delete SUCCEEDED: [person] TP2-TEST   Test Person"));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD DELETE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - WhoisRestApi(10.20.30.40)"));
    }
    
    @Test
    public void syncupdate_gets_logged() throws Exception {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=" + RestClientUtils.encode(secondPerson + "\npassword: test") + "&NEW=yes")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .get(String.class);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_0/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("<message><![CDATA[Header: X-Forwarded-For=10.20.30.40]]></message>"));
        assertThat(audit, containsString("<message><![CDATA[/whois/syncupdates/test?DATA"));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_0/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("REQUEST FROM:10.20.30.40"));
        assertThat(msgIn, containsString("NEW=yes"));
        assertThat(msgIn, containsString("DATA="));
        assertThat(msgIn, containsString("Test Person"));
        assertThat(msgIn, containsString("password: test"));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_0/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));

        final String msgOut = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_0/003.msg-out.txt.gz"));
        assertThat(msgOut, containsString("SUMMARY OF UPDATE:"));
        assertThat(msgOut, containsString("DETAILED EXPLANATION:"));
        assertThat(msgOut, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - SyncUpdate(10.20.30.40)"));
    }

    @Ignore("to be implemented")
    @Test
    public void mailupdate_gets_logged() throws Exception {
        // TODO: test mailupdate gets logged
    }

    // helper methods

    private void cleanupAuditLogDirectory() throws IOException {
        for (File next : new File(auditLog).listFiles()) {
            if (next.isDirectory()) {
                FileUtils.deleteDirectory(next);
            } else {
                next.delete();
            }
        }
    }

}
