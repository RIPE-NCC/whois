package net.ripe.db.whois.api.nrtmv4;

import net.ripe.db.nrtm4.NrtmFileProcessor;
import net.ripe.db.nrtm4.UpdateNotificationFileGenerator;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UpdateNotificationFileGenerationTestIntegration extends AbstractIntegrationTest {

    @Autowired
    UpdateNotificationFileGenerator updateNotificationFileGenerator;

    @Autowired
    NrtmFileProcessor nrtmFileProcessor;

    @Autowired
    SourceRepository sourceRepository;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("" +
                "person:        Test Person\n" +
                "nic-hdl:       TP1-TEST\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z");
        databaseHelper.addObject("" +
                "mntner:        OWNER-MNT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        OWNER-MNT\n" +
                "referral-by:   OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.updateObject("" +
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "person:        Test Person2\n" +
                "address:       Test Address\n" +
                "phone:         +61-1234-1234\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       TP2-TEST\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "role:          First Role\n" +
                "address:       Singel 258\n" +
                "e-mail:        dbtest@ripe.net\n" +
                "admin-c:       PP1-TEST\n" +
                "tech-c:        PP1-TEST\n" +
                "nic-hdl:       FR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "domain:        31.12.202.in-addr.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "notify:        notify@test.net.au\n" +
                "nserver:       ns1.test.com.au 10.0.0.1\n" +
                "nserver:       ns2.test.com.au 2001:10::2\n" +
                "ds-rdata:      52151 1 1 13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "ds-rdata:      17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78\n" +
                "ds-rdata:      17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "organisation:  ORG-TEST1-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "admin-c:       PP1-TEST\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:       AS100 - AS200\n" +
                "descr:          ARIN ASN block\n" +
                "org:            ORG-TEST1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        databaseHelper.addObject("" +
                "inetnum:        0.0.0.0 - 255.255.255.255\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv4 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        databaseHelper.addObject("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
    }

    @Test
    public void should_do_nothing_if_no_snapshot_exists()  {
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 1, "TEST");
        updateNotificationFileGenerator.generateFile();

        final Response response = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void should_not_create_new_file_if_no_changes()  {
        testDateTimeProvider.setTime(LocalDateTime.now().minusHours(1));
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firsIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        testDateTimeProvider.setTime(LocalDateTime.now());

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile secondIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(firsIteration.getTimestamp(), is(secondIteration.getTimestamp()));
    }

    @Test
    public void should_create_file_if_no_changes_last_file_24_hours_old()  {
        testDateTimeProvider.setTime(LocalDateTime.now().minusDays(1));
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firsIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        final LocalDateTime timeNow = LocalDateTime.now();
        testDateTimeProvider.setTime(timeNow);

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile secondIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(firsIteration.getTimestamp(), not(secondIteration.getTimestamp()));
    }

    @Test
    public void should_create_file_with_latest_delta_older_snapshot_version()  {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space: Modified\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");


        testDateTimeProvider.setTime(LocalDateTime.now().minusDays(1));

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();


        final PublishableNotificationFile firstIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);


        assertThat(firstIteration.getSnapshot().getVersion(), is(1L));
        assertThat(firstIteration.getDeltas().size(), is(0));

        testDateTimeProvider.setTime(LocalDateTime.now().withHour(23));

        databaseHelper.updateObject(rpslObject);

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile secondIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);


        assertThat(secondIteration.getSnapshot().getVersion(), is(2L));
        assertThat(secondIteration.getDeltas().size(), is(1));
        assertThat(secondIteration.getDeltas().get(0).getVersion(), is(2L));

        assertThat(firstIteration.getSessionID(), is(secondIteration.getSessionID()));

        databaseHelper.deleteObject(rpslObject);

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile thirdIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(thirdIteration.getSnapshot().getVersion(), is(2L));
        assertThat(thirdIteration.getDeltas().size(), is(2));
        assertThat(thirdIteration.getDeltas().get(0).getVersion(), is(2L));
        assertThat(thirdIteration.getDeltas().get(1).getVersion(), is(3L));

        assertThat(thirdIteration.getVersion(), is(thirdIteration.getDeltas().get(1).getVersion()));

        assertThat(secondIteration.getSessionID(), is(thirdIteration.getSessionID()));

    }

    @Test
    public void should_create_file_with_changes_both_sources()  {
        databaseHelper.addObject("" +
                "mntner:        NONAUTH-OWNER-MNT\n" +
                "descr:         Non auth Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        NONAUTH-OWNER-MNT\n" +
                "referral-by:   NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH");
        testDateTimeProvider.setTime(LocalDateTime.now().minusDays(1));

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile testIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        final PublishableNotificationFile testNonAuthIteration = createResource("TEST-NONAUTH/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(testIteration.getSource().getName(), is("TEST"));
        assertThat(testNonAuthIteration.getSource().getName(), is("TEST-NONAUTH"));

        assertThat(testIteration.getSessionID(), is(not(testNonAuthIteration.getSessionID())));

    }
    @Test
    public void should_contain_snapshot_delta_url(){
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space: Modified\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");

        testDateTimeProvider.setTime(LocalDateTime.now().minusDays(1));
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        databaseHelper.updateObject(rpslObject);

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(firstIteration.getDeltas().size(), is(1));
        assertThat(firstIteration.getDeltas().get(0).getUrl(), notNullValue());
        assertThat(firstIteration.getSnapshot().getUrl(), notNullValue());
    }

    @Test
    public void should_contain_notification_type_and_nrtm4_version(){
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(firstIteration.getType(), is(NrtmDocumentType.NOTIFICATION));
        assertThat(firstIteration.getNrtmVersion(), is(4));
    }

    @Test
    public void should_have_correct_timestamp_format(){
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(isValidDateFormat(firstIteration.getTimestamp()), is(true));
    }

    @Test
    public void should_have_correct_session_format(){
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);

        assertThat(isValidSessionFormat(firstIteration.getSessionID()), is(true));
    }
    protected WebTarget createResource(final String path) {
        return RestTest.target(getPort(), String.format("nrtmv4/%s", path));
    }

    private boolean isValidDateFormat(final String date){
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:dd'Z'");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
        } catch (final ParseException e){
            return false;
        }
        return true;
    }

    private boolean isValidSessionFormat(final String sessionId){
        try {
            UUID.fromString(sessionId);
        } catch (final IllegalArgumentException e){
            return false;
        }
        return true;
    }
}