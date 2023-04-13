package net.ripe.db.whois.api.nrtmv4;

import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.api.AbstractNrtmIntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;

import static net.ripe.db.whois.query.support.PatternMatcher.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UpdateNotificationFileGenerationTestIntegration extends AbstractNrtmIntegrationTest {

    @Test
    public void should_do_nothing_if_no_snapshot_exists()  {
        createNrtmSource();
        updateNotificationFileGenerator.generateFile();

        final Response response = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void should_not_create_new_file_if_no_changes()  {
        testDateTimeProvider.setTime(LocalDateTime.now().minusHours(1));
        snapshotFileGenerator.createSnapshot();

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firsIteration = getNotificationFileBySource("TEST");

        testDateTimeProvider.setTime(LocalDateTime.now());

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile secondIteration = getNotificationFileBySource("TEST");

        assertThat(firsIteration.getTimestamp(), is(secondIteration.getTimestamp()));
    }

    @Test
    public void should_create_file_if_no_changes_last_file_24_hours_old()  {
        testDateTimeProvider.setTime(LocalDateTime.now().minusDays(1));
        snapshotFileGenerator.createSnapshot();

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firsIteration = getNotificationFileBySource("TEST");

        final LocalDateTime timeNow = LocalDateTime.now();
        testDateTimeProvider.setTime(timeNow);

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile secondIteration = getNotificationFileBySource("TEST");

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

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = getNotificationFileBySource("TEST");


        assertThat(firstIteration.getSnapshot().getVersion(), is(1L));
        assertThat(firstIteration.getDeltas().size(), is(0));

        testDateTimeProvider.setTime(LocalDateTime.now().withHour(23));

        databaseHelper.updateObject(rpslObject);

        deltaFileGenerator.createDeltas();
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile secondIteration = getNotificationFileBySource("TEST");


        assertThat(secondIteration.getSnapshot().getVersion(), is(2L));
        assertThat(secondIteration.getDeltas().size(), is(1));
        assertThat(secondIteration.getDeltas().get(0).getVersion(), is(2L));

        assertThat(firstIteration.getSessionID(), is(secondIteration.getSessionID()));

        databaseHelper.deleteObject(rpslObject);

        deltaFileGenerator.createDeltas();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile thirdIteration = getNotificationFileBySource("TEST");

        assertThat(thirdIteration.getSnapshot().getVersion(), is(2L));
        assertThat(thirdIteration.getDeltas().size(), is(2));
        assertThat(thirdIteration.getDeltas().get(0).getVersion(), is(2L));
        assertThat(thirdIteration.getDeltas().get(1).getVersion(), is(3L));

        assertThat(secondIteration.getSessionID(), is(thirdIteration.getSessionID()));

    }

    @Test
    public void should_create_file_with_changes_both_sources()  {
        testDateTimeProvider.setTime(LocalDateTime.now().minusDays(1));

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile testIteration = getNotificationFileBySource("TEST");

        final PublishableNotificationFile testNonAuthIteration = getNotificationFileBySource("TEST-NONAUTH");

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
        snapshotFileGenerator.createSnapshot();

        databaseHelper.updateObject(rpslObject);

        deltaFileGenerator.createDeltas();
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(firstIteration.getDeltas().size(), is(1));
        assertThat(firstIteration.getDeltas().get(0).getUrl(), containsString("https"));
        assertThat(firstIteration.getSnapshot().getUrl(), containsString("https"));
    }

    @Test
    public void should_contain_notification_type_and_nrtm4_version(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(firstIteration.getType(), is(NrtmDocumentType.NOTIFICATION));
        assertThat(firstIteration.getNrtmVersion(), is(4));
    }

    @Test
    public void should_have_correct_timestamp_format(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(isValidDateFormat(firstIteration.getTimestamp()), is(true));
    }

    @Test
    public void should_have_correct_session_format(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(isValidSessionFormat(firstIteration.getSessionID()), is(true));
    }

    @Test
    public void should_have_sha_length_hexadecimal_hash_format() {
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
        snapshotFileGenerator.createSnapshot();

        databaseHelper.updateObject(rpslObject);

        snapshotFileGenerator.createSnapshot();
        deltaFileGenerator.createDeltas();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(firstIteration.getSnapshot().getHash(), matchesPattern("^[0-9a-fA-F]{64}$"));
        assertThat(firstIteration.getDeltas().get(0).getHash(), matchesPattern("^[0-9a-fA-F]{64}$"));
    }

    @Test
    public void should_throw_exception_invalid_source_notification_file()  {
        final Response response = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Invalid source"));
    }

    @Test
    public void should_throw_exception_notification_file_not_found() {
        createNrtmSource();
        final Response response = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("update-notification-file.json does not exists for source TEST"));
    }
    private boolean isValidDateFormat(final String date){
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
