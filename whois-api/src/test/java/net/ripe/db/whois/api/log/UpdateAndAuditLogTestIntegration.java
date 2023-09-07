package net.ripe.db.whois.api.log;

import com.google.common.net.HttpHeaders;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.support.FileHelper;
import net.ripe.db.whois.update.mail.MailSenderStub;
import net.ripe.db.whois.update.support.TestUpdateLog;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.rpsl.RpslObjectFilter.buildGenericObject;
import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class UpdateAndAuditLogTestIntegration extends AbstractIntegrationTest {
    private static final String PASSWORD = "team-red4321";
    private static final String OVERRIDE_PASSWORD = "team-red1234";

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:        OWNER-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$GUKzBg/F$PoCZBbhTNxCKM3K9VF8y60\n" + // #team-red4321
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:        Test Person\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TP1-TEST\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    @Value("${dir.update.audit.log}")
    String auditLog;

    @Autowired TestUpdateLog updateLog;
    @Autowired AuthServiceClient authServiceClient;
    @Autowired MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired MailSenderStub mailSenderStub;
    @Autowired private WhoisObjectMapper whoisObjectMapper;

    @Autowired
    private RestClient restClient;

    @BeforeEach
    public void setup() throws Exception {
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T13:00:00"));
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);

        ReflectionTestUtils.setField(restClient, "restApiUrl", String.format("http://localhost:%d/whois", getPort()));
        ReflectionTestUtils.setField(restClient, "sourceName", "TEST");
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanupAuditLogDirectory();
    }

    @Test
    public void create_gets_logged_override() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("personadmin", OVERRIDE_PASSWORD, ObjectType.values()));
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");

        RestTest.target(getPort(), "whois/TEST/person")
                .queryParam("override", SyncUpdateUtils.encode("personadmin," + OVERRIDE_PASSWORD + ",my reason"))
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, secondPerson), MediaType.APPLICATION_XML), WhoisResources.class);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("<![CDATA[POST /whois/TEST/person?override=personadmin,FILTERED,my%2Breason"));
        assertThat(audit, not(containsString(OVERRIDE_PASSWORD)));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         Test Person"));
        assertThat(msgIn, not(containsString(OVERRIDE_PASSWORD)));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(ack, not(containsString(OVERRIDE_PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I1> AUTH OVERRIDE - WhoisRestApi(127.0.0.1)"));
        assertThat(updateLog.getMessage(0), not(containsString(OVERRIDE_PASSWORD)));
    }

    @Test
    public void rest_create_gets_logged() {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", PASSWORD)
                .create(secondPerson);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("Header: X-Forwarded-For=10.20.30.40"));
        assertThat(audit, containsString("<message><![CDATA[POST /whois/TEST/person?password=FILTERED"));
        assertThat(audit, not(containsString(PASSWORD)));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         Test Person"));
        assertThat(msgIn, not(containsString(PASSWORD)));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(ack, not(containsString(PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - WhoisRestApi(10.20.30.40)"));
        assertThat(updateLog.getMessage(0), not(containsString(PASSWORD)));
    }

    @Test
    public void rest_update_gets_logged() {
        final RpslObject updatedPerson = buildGenericObject(TEST_PERSON, "remarks: i will be back");
        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", PASSWORD)
                .update(updatedPerson);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("Header: X-Forwarded-For=10.20.30.40"));
        assertThat(audit, containsString("<![CDATA[PUT /whois/TEST/person/TP1-TEST?password=FILTERED"));
        assertThat(audit, not(containsString(PASSWORD)));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         Test Person"));
        assertThat(msgIn, not(containsString(PASSWORD)));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Modify SUCCEEDED: [person] TP1-TEST   Test Person"));
        assertThat(ack, not(containsString(PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD MODIFY person\\s+TP1-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - WhoisRestApi(10.20.30.40)"));
        assertThat(updateLog.getMessage(0), not(containsString(PASSWORD)));
    }

    @Test
    public void rest_delete_gets_logged() {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        databaseHelper.addObject(secondPerson);
        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addParam("password", PASSWORD)
                .delete(secondPerson);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("Header: X-Forwarded-For=10.20.30.40"));
        assertThat(audit, containsString("<![CDATA[DELETE /whois/TEST/person/TP2-TEST?password=FILTERED"));
        assertThat(audit, not(containsString(PASSWORD)));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         Test Person"));
        assertThat(msgIn, not(containsString(PASSWORD)));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Delete SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(ack, not(containsString(PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD DELETE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - WhoisRestApi(10.20.30.40)"));
        assertThat(updateLog.getMessage(0), not(containsString(PASSWORD)));
    }

    @Test
    public void rest_delete_nonexistant_object_gets_logged() {
        final RpslObject nonexistantPerson = buildGenericObject(TEST_PERSON, "nic-hdl: ZYZ-TEST");

        try {
            restClient.request()
                    .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                    .addParam("password", PASSWORD)
                    .delete(nonexistantPerson);
            fail();
        } catch (RestClientException e) {
            assertThat(e.getStatus(), is(404));
            assertThat(e.getErrorMessages(), contains(new ErrorMessage(new Message(Messages.Type.ERROR, "Not Found"))));
        }

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_10.20.30.40_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<![CDATA[DELETE /whois/TEST/person/ZYZ-TEST?password=FILTERED"));
        assertThat(audit, containsString("<![CDATA[Caught class org.springframework.dao.EmptyResultDataAccessException for ZYZ-TEST: Incorrect result size: expected 1, actual 0]]>"));
        assertThat(audit, not(containsString(PASSWORD)));
    }

    @Test
    public void syncupdate_gets_logged_override() throws Exception {
        databaseHelper.insertUser(User.createWithPlainTextPassword("personadmin", OVERRIDE_PASSWORD, ObjectType.values()));
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(secondPerson + "override: personadmin," + OVERRIDE_PASSWORD) + "&NEW=yes")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "127.0.0.1")
                .get(String.class);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("Header: X-Forwarded-For=127.0.0.1"));
        assertThat(audit, containsString("<![CDATA[GET /whois/syncupdates/test?DATA"));
        assertThat(audit, not(containsString(OVERRIDE_PASSWORD)));
        assertThat(audit, containsString("override%3A+personadmin,FILTERED"));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("REQUEST FROM:127.0.0.1"));
        assertThat(msgIn, containsString("NEW=yes"));
        assertThat(msgIn, containsString("DATA="));
        assertThat(msgIn, containsString("Test Person"));
        assertThat(msgIn, not(containsString(OVERRIDE_PASSWORD)));
        assertThat(msgIn, containsString("override:personadmin,FILTERED"));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(ack, not(containsString(OVERRIDE_PASSWORD)));

        final String msgOut = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/003.msg-out.txt.gz"));
        assertThat(msgOut, containsString("SUMMARY OF UPDATE:"));
        assertThat(msgOut, containsString("DETAILED EXPLANATION:"));
        assertThat(msgOut, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(msgOut, not(containsString(OVERRIDE_PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I1> AUTH OVERRIDE - SyncUpdate(127.0.0.1)"));
        assertThat(updateLog.getMessage(0), not(containsString(PASSWORD)));
    }

    @Test
    public void syncupdate_post_gets_logged_override() throws Exception {
        databaseHelper.insertUser(User.createWithPlainTextPassword("personadmin", OVERRIDE_PASSWORD, ObjectType.values()));
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");

        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "127.0.0.1")
                .post(Entity.entity("DATA=" +
                                        SyncUpdateUtils.encode(secondPerson + "override: personadmin," + OVERRIDE_PASSWORD + ",reason") + "&NEW=yes",
                                MediaType.valueOf("application/x-www-form-urlencoded")),
                        String.class);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("Header: X-Forwarded-For=127.0.0.1"));
        assertThat(audit, containsString("<![CDATA[POST /whois/syncupdates/test"));
        assertThat(audit, not(containsString(OVERRIDE_PASSWORD)));
        assertThat(audit, containsString("OverrideCredential{personadmin,FILTERED,reason}"));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("REQUEST FROM:127.0.0.1"));
        assertThat(msgIn, containsString("NEW=yes"));
        assertThat(msgIn, containsString("DATA="));
        assertThat(msgIn, containsString("Test Person"));
        assertThat(msgIn, not(containsString(OVERRIDE_PASSWORD)));
        assertThat(msgIn, containsString("override:personadmin,FILTERED,reason"));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(ack, not(containsString(OVERRIDE_PASSWORD)));

        final String msgOut = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_127.0.0.1_100/003.msg-out.txt.gz"));
        assertThat(msgOut, containsString("SUMMARY OF UPDATE:"));
        assertThat(msgOut, containsString("DETAILED EXPLANATION:"));
        assertThat(msgOut, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(msgOut, not(containsString(OVERRIDE_PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I1> AUTH OVERRIDE - SyncUpdate(127.0.0.1)"));
        assertThat(updateLog.getMessage(0), not(containsString(PASSWORD)));
    }

    @Test
    public void rest_create_with_sso_auth_gets_logged() {
        restClient.request()
                .addParam("password", "team-red4321")
                .update(new RpslObjectBuilder(OWNER_MNT).append(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")).get());
        updateLog.reset();

        final RpslObject person = buildGenericObject(TEST_PERSON, "nic-hdl: ZYZ-TEST");

        restClient.request()
                .addHeader(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .addCookie(new Cookie("crowd.token_key", "valid-token"))
                .create(person);

        assertThat(updateLog.getMessages(), hasSize(1));
        System.err.println(updateLog.getMessage(0));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+ZYZ-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH SSO - WhoisRestApi(10.20.30.40)"));
    }

    @Test
    public void syncupdates_create_gets_logged() throws Exception {
        final RpslObject secondPerson = buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST");
        RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(secondPerson + "\npassword: " + PASSWORD) + "&NEW=yes")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .get(String.class);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("Header: X-Forwarded-For=10.20.30.40"));
        assertThat(audit, containsString("<![CDATA[GET /whois/syncupdates/test?DATA"));
        assertThat(audit, not(containsString(PASSWORD)));
        assertThat(audit, containsString("password%3AFILTERED"));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("REQUEST FROM:10.20.30.40"));
        assertThat(msgIn, containsString("NEW=yes"));
        assertThat(msgIn, containsString("DATA="));
        assertThat(msgIn, containsString("Test Person"));
        assertThat(msgIn, not(containsString(PASSWORD)));
        assertThat(msgIn, containsString("password:FILTERED"));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(ack, not(containsString(PASSWORD)));

        final String msgOut = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_100/003.msg-out.txt.gz"));
        assertThat(msgOut, containsString("SUMMARY OF UPDATE:"));
        assertThat(msgOut, containsString("DETAILED EXPLANATION:"));
        assertThat(msgOut, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(msgOut, not(containsString(PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - SyncUpdate(10.20.30.40)"));
        assertThat(updateLog.getMessage(0), not(containsString(PASSWORD)));
    }


    @Test
    public void syncupdates_create_with_sso_auth_gets_logged() {
        restClient.request()
                .addParam("password", "team-red4321")
                .update(new RpslObjectBuilder(OWNER_MNT).append(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")).get());
        updateLog.reset();

        final RpslObject person = buildGenericObject(TEST_PERSON, "nic-hdl: ZYZ-TEST");

        RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(person.toString()) + "&NEW=yes")
                .request()
                .cookie(new Cookie("crowd.token_key", "valid-token"))
                .header(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .get(String.class);

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+ZYZ-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH SSO - SyncUpdate(10.20.30.40)"));
    }

    @Test
    public void syncupdates_invalid_data_gets_logged() {
        RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=invalid")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .get(String.class);

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.syncupdate_10.20.30.40_100/000.audit.xml.gz"));

        assertThat(audit, containsString("<![CDATA[GET /whois/syncupdates/test?DATA=invalid"));
    }

    @Test
    public void mailupdate_gets_logged() throws Exception {
        final String response = mailUpdatesTestSupport.insert("NEW", buildGenericObject(TEST_PERSON, "nic-hdl: TP2-TEST").toString() + "\npassword: " + PASSWORD);

        final MimeMessage message = mailSenderStub.getMessage(response);

        assertThat(message.getContent().toString(), containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));

        final String logDirectory = String.format("%s/20010204/130000.%s/", auditLog, getUpdateMessageId(message));

        waitForFileToBeWritten(new File(logDirectory + "000.audit.xml.gz"));

        final String audit = FileHelper.fetchGzip(new File(logDirectory + "000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("Test Person"));
        assertThat(audit, not(containsString(PASSWORD)));

        final String msgIn = FileHelper.fetchGzip(new File(logDirectory + "001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("Subject: NEW"));
        assertThat(msgIn, containsString("Test Person"));
        assertThat(msgIn, not(containsString(PASSWORD)));
        assertThat(msgIn, containsString("password:FILTERED"));

        final String ack = FileHelper.fetchGzip(new File(logDirectory + "002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(ack, not(containsString(PASSWORD)));

        final String msgOut = FileHelper.fetchGzip(new File(logDirectory + "003.msg-out.txt.gz"));
        assertThat(msgOut, containsString("SUMMARY OF UPDATE:"));
        assertThat(msgOut, containsString("DETAILED EXPLANATION:"));
        assertThat(msgOut, containsString("Create SUCCEEDED: [person] TP2-TEST   Test Person"));
        assertThat(msgOut, not(containsString(PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(1));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+TP2-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I0> AUTH PWD - Mail"));
        assertThat(updateLog.getMessage(0), not(containsString(PASSWORD)));
    }

    @Test
    public void multiple_updates_get_logged() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("personadmin", OVERRIDE_PASSWORD, ObjectType.values()));

        ActionRequest newPerson = new ActionRequest(RpslObject.parse(
                "person:        New Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       NTP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST"), Action.CREATE);

        ActionRequest newMnt = new ActionRequest(RpslObject.parse(
                "person:        Other New Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       ONTP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST"), Action.CREATE);

        WhoisResources whoisResources = whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, newPerson, newMnt);

        RestTest.target(getPort(), "whois/references/TEST")
                .queryParam("override", SyncUpdateUtils.encode("personadmin," + OVERRIDE_PASSWORD + ",some_app"))
                .request()
                .put(Entity.entity(whoisResources, MediaType.APPLICATION_XML));

        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("<![CDATA[PUT /whois/references/TEST?override=personadmin,FILTERED,some_app"));
        assertThat(audit, containsString("<![CDATA[person:         New Test Person"));
        assertThat(audit, containsString("<![CDATA[person:         Other New Test Person"));
        assertThat(audit, not(containsString(OVERRIDE_PASSWORD)));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         New Test Person"));
        assertThat(msgIn, containsString("person:         Other New Test Person"));
        assertThat(msgIn, not(containsString(OVERRIDE_PASSWORD)));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] NTP1-TEST   New Test Person"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] ONTP1-TEST   Other New Test Person"));
        assertThat(ack, not(containsString(OVERRIDE_PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(2));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+NTP1-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(1), stringMatchesRegexp(".*UPD CREATE person\\s+ONTP1-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I1> AUTH OVERRIDE - WhoisRestApi(127.0.0.1)"));
        assertThat(updateLog.getMessage(1), containsString("<E0,W0,I1> AUTH OVERRIDE - WhoisRestApi(127.0.0.1)"));
        assertThat(updateLog.getMessage(0), not(containsString(OVERRIDE_PASSWORD)));
        assertThat(updateLog.getMessage(1), not(containsString(OVERRIDE_PASSWORD)));
    }

    @Test
    public void multiple_updates_fails_get_logged() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("personadmin", OVERRIDE_PASSWORD, ObjectType.values()));

        ActionRequest newPerson = new ActionRequest(RpslObject.parse(
                "person:        New Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       NTP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST"), Action.CREATE);

        ActionRequest newMnt = new ActionRequest(RpslObject.parse(
                "person:        Other New Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       ONTP1-TEST\n" +
                "source:        TEST"), Action.CREATE);

        WhoisResources whoisResources = whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, newPerson, newMnt);

        Response override = RestTest.target(getPort(), "whois/references/TEST")
                .queryParam("override", SyncUpdateUtils.encode("personadmin," + OVERRIDE_PASSWORD + ",some_app"))
                .request()
                .put(Entity.entity(whoisResources, MediaType.APPLICATION_XML));

        System.out.println(override.getStatus());
        final String audit = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        assertThat(audit, containsString("<![CDATA[PUT /whois/references/TEST?override=personadmin,FILTERED,some_app"));
        assertThat(audit, containsString("<![CDATA[person:         New Test Person"));
        assertThat(audit, containsString("<![CDATA[person:         Other New Test Person"));
        assertThat(audit, not(containsString(OVERRIDE_PASSWORD)));

        final String msgIn = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/001.msg-in.txt.gz"));
        assertThat(msgIn, containsString("person:         New Test Person"));
        assertThat(msgIn, containsString("person:         Other New Test Person"));
        assertThat(msgIn, not(containsString(OVERRIDE_PASSWORD)));

        final String ack = FileHelper.fetchGzip(new File(auditLog + "/20010204/130000.rest_127.0.0.1_100/002.ack.txt.gz"));
        assertThat(ack, containsString("Create SUCCEEDED: [person] NTP1-TEST   New Test Person"));
        assertThat(ack, containsString("Create FAILED: [person] ONTP1-TEST   Other New Test Person"));
        assertThat(ack, not(containsString(OVERRIDE_PASSWORD)));

        assertThat(updateLog.getMessages(), hasSize(2));
        assertThat(updateLog.getMessage(0), stringMatchesRegexp(".*UPD CREATE person\\s+NTP1-TEST\\s+\\(1\\) SUCCESS\\s+:.*"));
        assertThat(updateLog.getMessage(1), stringMatchesRegexp(".*UPD CREATE person\\s+ONTP1-TEST\\s+\\(1\\) FAILED\\s+:.*"));
        assertThat(updateLog.getMessage(0), containsString("<E0,W0,I1> AUTH OVERRIDE - WhoisRestApi(127.0.0.1)"));
        assertThat(updateLog.getMessage(1), containsString("<E1,W0,I0> AUTH OVERRIDE - WhoisRestApi(127.0.0.1)"));
        assertThat(updateLog.getMessage(0), not(containsString(OVERRIDE_PASSWORD)));
        assertThat(updateLog.getMessage(1), not(containsString(OVERRIDE_PASSWORD)));
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

    private String getUpdateMessageId(final MimeMessage message) throws IOException, MessagingException {
        final Pattern pattern = Pattern.compile("(?im)^>  Message-ID: <(.+?)(@.*)?>$");
        final Matcher matcher = pattern.matcher(message.getContent().toString());
        if (!matcher.find()) {
            throw new IllegalArgumentException("No Message-ID found in content?");
        } else {
            return matcher.group(1);
        }
    }

    private void waitForFileToBeWritten(final File file) {
        Awaitility.waitAtMost(5L, TimeUnit.SECONDS).until(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return Long.valueOf(file.length());
            }
        }, is(not(0L)));
    }
}
