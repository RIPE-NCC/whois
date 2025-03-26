package net.ripe.db.whois.api.nrtmv4;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.UpdateNotificationFile;
import net.ripe.db.whois.api.AbstractNrtmIntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UpdateNotificationFileGenerationTestIntegration extends AbstractNrtmIntegrationTest {

    @Test
    public void should_do_nothing_if_no_snapshot_exists()  {
        createNrtmSource();
        nrtmKeyPairService.generateActiveKeyPair();

        updateNotificationFileGenerator.generateFile();

        final Response response = getResponseFromHttpsRequest("TEST/update-notification-file.json",
                MediaType.APPLICATION_JSON);

        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void should_not_create_new_file_if_no_changes()  {
        setTime(LocalDateTime.now().minusHours(1));
        snapshotFileGenerator.createSnapshot();

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firsIteration = getNotificationFileBySource("TEST");

        setTime(LocalDateTime.now());

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile secondIteration = getNotificationFileBySource("TEST");

        assertThat(firsIteration.getTimestamp(), is(secondIteration.getTimestamp()));
    }

    @Test
    public void should_not_create_new_file_if_no_changes_in_and_next_key_same()  {
        setTime(LocalDateTime.now().minusHours(1));
        snapshotFileGenerator.createSnapshot();
        nrtmKeyPairService.generateKeyRecord(false);

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firsIteration = getNotificationFileBySource("TEST");

        setTime(LocalDateTime.now());

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile secondIteration = getNotificationFileBySource("TEST");

        assertThat(firsIteration.getTimestamp(), is(secondIteration.getTimestamp()));
        assertThat(firsIteration.getNextSigningKey(), is(secondIteration.getNextSigningKey()));
    }

    @Test
    public void should_create_new_file_if_next_key_available()  {
        setTime(LocalDateTime.now().minusHours(1));
        snapshotFileGenerator.createSnapshot();

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firsIteration = getNotificationFileBySource("TEST");

        setTime(LocalDateTime.now());

        nrtmKeyPairService.generateKeyRecord(false);
        addPublicKeyinPemFormat(nrtmKeyPairService.getNextkeyPair().id());

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile secondIteration = getNotificationFileBySource("TEST");

        assertThat(firsIteration.getTimestamp(), is(not(secondIteration.getTimestamp())));
        assertThat(firsIteration.getVersion(), is(secondIteration.getVersion()));
        assertThat(secondIteration.getNextSigningKey(), notNullValue());
        assertThat(firsIteration.getNextSigningKey(), nullValue());
    }

    @Test
    public void should_create_new_file_if_key_is_rotated()  {
        setTime(LocalDateTime.now().minusHours(1));
        snapshotFileGenerator.createSnapshot();

        setTime(LocalDateTime.now().plusHours(1));

        nrtmKeyPairService.generateKeyRecord(false);
        addPublicKeyinPemFormat(nrtmKeyPairService.getNextkeyPair().id());

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firsIteration = getNotificationFileBySource("TEST");

        nrtmKeyPairService.forceRotateKey();
        setTime(LocalDateTime.now().plusHours(2));

        updateNotificationFileGenerator.generateFile();
        final UpdateNotificationFile secondIteration = getNotificationFileBySource("TEST");

        assertThat(firsIteration.getTimestamp(), is(not(secondIteration.getTimestamp())));
        assertThat(firsIteration.getVersion(), is(secondIteration.getVersion()));
        assertThat(secondIteration.getNextSigningKey(), nullValue());
        assertThat(firsIteration.getNextSigningKey(), notNullValue());
    }

    @Test
    public void should_create_file_if_no_changes_last_file_24_hours_old()  {
        setTime(LocalDateTime.now().minusDays(1));
        snapshotFileGenerator.createSnapshot();

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firsIteration = getNotificationFileBySource("TEST");

        final LocalDateTime timeNow = LocalDateTime.now();
        setTime(timeNow);

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile secondIteration = getNotificationFileBySource("TEST");

        assertThat(firsIteration.getTimestamp(), not(secondIteration.getTimestamp()));
    }

    @Test
    public void should_create_file_with_latest_delta_older_snapshot_version() throws ParseException, JsonProcessingException {
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


        setTime(LocalDateTime.now().minusDays(1));

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firstIteration = getNotificationFileBySource("TEST");


        assertThat(firstIteration.getSnapshot().getVersion(), is(1L));
        assertThat(firstIteration.getDeltas().size(), is(0));

        setTime(LocalDateTime.now().withHour(23));

        databaseHelper.updateObject(rpslObject);

        deltaFileGenerator.createDeltas();
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile secondIteration = getNotificationFileBySource("TEST");


        assertThat(secondIteration.getSnapshot().getVersion(), is(2L));
        assertThat(secondIteration.getDeltas().size(), is(1));
        assertThat(secondIteration.getDeltas().get(0).getVersion(), is(2L));

        assertThat(firstIteration.getSessionID(), is(secondIteration.getSessionID()));

        databaseHelper.deleteObject(rpslObject);

        deltaFileGenerator.createDeltas();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile thirdIteration = getNotificationFileBySource("TEST");

        assertThat(thirdIteration.getSnapshot().getVersion(), is(2L));
        assertThat(thirdIteration.getDeltas().size(), is(2));
        assertThat(thirdIteration.getDeltas().get(0).getVersion(), is(2L));
        assertThat(thirdIteration.getDeltas().get(1).getVersion(), is(3L));

        assertThat(secondIteration.getSessionID(), is(thirdIteration.getSessionID()));

    }

    @Test
    public void should_create_file_with_changes_both_sources()  {
        setTime(LocalDateTime.now().minusDays(1));

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile testIteration = getNotificationFileBySource("TEST");

        final UpdateNotificationFile testNonAuthIteration = getNotificationFileBySource("TEST-NONAUTH");

        assertThat(testIteration.getSource().getName(), is("TEST"));
        assertThat(testNonAuthIteration.getSource().getName(), is("TEST-NONAUTH"));
        assertThat(testNonAuthIteration.getNextSigningKey(), is(nullValue()));
        assertThat(testIteration.getNextSigningKey(), is(nullValue()));

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

        setTime(LocalDateTime.now().minusDays(1));
        snapshotFileGenerator.createSnapshot();

        databaseHelper.updateObject(rpslObject);

        deltaFileGenerator.createDeltas();
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(firstIteration.getDeltas().size(), is(1));
        assertThat(firstIteration.getDeltas().getFirst().getUrl(), containsString("nrtm-delta"));
        assertThat(firstIteration.getSnapshot().getUrl(), containsString("nrtm-snapshot"));
    }

    @Test
    public void should_contain_notification_type_and_nrtm4_version(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(firstIteration.getType(), is(NrtmDocumentType.NOTIFICATION));
        assertThat(firstIteration.getNrtmVersion(), is(4));
    }

    @Test
    public void should_have_correct_timestamp_format(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(isValidDateFormat(firstIteration.getTimestamp()), is(true));
    }

    @Test
    public void should_have_correct_session_format(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firstIteration = getNotificationFileBySource("TEST");

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

        setTime(LocalDateTime.now().minusDays(1));
        snapshotFileGenerator.createSnapshot();

        databaseHelper.updateObject(rpslObject);

        snapshotFileGenerator.createSnapshot();
        deltaFileGenerator.createDeltas();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firstIteration = getNotificationFileBySource("TEST");

        assertThat(firstIteration.getSnapshot().getHash(), matchesPattern("^[0-9a-fA-F]{64}$"));
        assertThat(firstIteration.getDeltas().get(0).getHash(), matchesPattern("^[0-9a-fA-F]{64}$"));
    }

    @Test
    public void should_throw_exception_invalid_source_notification_file()  {
        final Response response = getResponseFromHttpsRequest("TEST/update-notification-file.jose",
                MediaType.APPLICATION_JSON);
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Invalid source"));
    }

    @Test
    public void should_throw_exception_notification_file_not_found() {
        createNrtmSource();
        final Response response = getResponseFromHttpsRequest("TEST/update-notification-file.jose",
                MediaType.APPLICATION_JSON);

        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("update-notification-file does not exists for source TEST"));
    }

    @Test
    public void should_throw_405_when_method_does_not_exist() {
        final Response response = getWebTarget("?infvt=kefne")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeader.X_FORWARDED_PROTO.asString(), HttpScheme.HTTPS.asString())
                    .post(jakarta.ws.rs.client.Entity.entity("", MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(HttpStatus.METHOD_NOT_ALLOWED_405));
    }

    /* Helper */
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
