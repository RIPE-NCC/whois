package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.nrtm4.domain.DeltaFileRecord;
import net.ripe.db.nrtm4.domain.UpdateNotificationFile;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.whois.common.rpsl.DummifierNrtmV4;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;


@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DeltaFileGenerationTestIntegration extends AbstractNrtmIntegrationTest {
    @Autowired
    DummifierNrtmV4 dummifierNrtmV4;

    @Test
    public void should_get_delta_file() throws JSONException, JsonProcessingException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final RpslObject updatedObject = RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        generateDeltas(Collections.singletonList(updatedObject));
        updateNotificationFileGenerator.generateFile();

        final String[] records = getDeltasFromUpdateNotificationBySource("TEST", 0);
        assertNrtmFileInfo(records[0], "delta", 2, "TEST");

        final List<DeltaFileRecord> deltaFileRecords = getDeltaChanges(records);

        assertThat(deltaFileRecords.size(), is(1));
        assertThat(deltaFileRecords.get(0).getAction(), is(DeltaFileRecord.Action.ADD_MODIFY));
        assertThat(deltaFileRecords.get(0).getObject().toString(), is(dummifierNrtmV4.dummify(updatedObject).toString()));
    }

    @Test
    public void should_get_delta_file_sequence_versions() throws JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile updateNotificationFile = getNotificationFileBySource("TEST");
        final RpslObject updatedObject = RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        generateDeltas(Collections.singletonList(updatedObject));
        updateNotificationFileGenerator.generateFile();

        final String[] firstIterationDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        generateDeltas(Collections.singletonList(updatedObject));
        updateNotificationFileGenerator.generateFile();

        final String[] secondIterationDelta = getDeltasFromUpdateNotificationBySource("TEST", 1);

        assertThat(updateNotificationFile.getSnapshot().getVersion(), is(1L));
        assertNrtmFileInfo(firstIterationDelta[0], "delta", 2, "TEST");
        assertNrtmFileInfo(secondIterationDelta[0], "delta", 3, "TEST");
    }

    @Test
    public void should_have_session_version_hash_value(){
        snapshotFileGenerator.createSnapshot();

        updateNotificationFileGenerator.generateFile();
        generateDeltas(Collections.singletonList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST")));

        updateNotificationFileGenerator.generateFile();
        final UpdateNotificationFile firsIteration = getNotificationFileBySource("TEST");
        assertThat(firsIteration.getDeltas().get(0).getUrl(), is(notNullValue()));
        assertThat(firsIteration.getSessionID(), is(notNullValue()));
        assertThat(firsIteration.getDeltas().get(0).getVersion(), is(notNullValue()));
        assertThat(firsIteration.getDeltas().get(0).getHash(), is(notNullValue()));
    }
    @Test
    public void delta_should_have_same_version_different_session_per_source() throws JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        generateDeltas(Lists.newArrayList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST"), RpslObject.parse("" +
                "mntner:        NONAUTH-OWNER-MNT\n" +
                "descr:         Non auth Owner Maintainer updated\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH")));
        updateNotificationFileGenerator.generateFile();
        final String[] testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        final String[] nonAuthDelta = getDeltasFromUpdateNotificationBySource("TEST-NONAUTH", 0);

        assertNrtmFileInfo(testDelta[0], "delta", 2, "TEST");
        assertNrtmFileInfo(nonAuthDelta[0], "delta", 2, "TEST-NONAUTH");

        assertThat(getNrtmVersionInfo(testDelta[0]).getSessionID(), is(not(getNrtmVersionInfo(nonAuthDelta[0]).getSessionID())));
    }

    @Test
    public void should_get_delta_file_correct_order() throws JSONException, JsonProcessingException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final RpslObject updatedObject = RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        databaseHelper.updateObject(updatedObject);
        databaseHelper.deleteObject(updatedObject);

        deltaFileGenerator.createDeltas();
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final String[] testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        final List<DeltaFileRecord> deltaFileRecords = getDeltaChanges(testDelta);

        assertThat(deltaFileRecords.size(), is(2));
        assertNrtmFileInfo(testDelta[0], "delta", 2, "TEST");

        assertThat(deltaFileRecords.get(0).getAction().toLowerCaseName(), is("add_modify"));
        assertThat(deltaFileRecords.get(0).getObject().toString(), is(dummifierNrtmV4.dummify(updatedObject).toString()));

        assertThat(deltaFileRecords.get(1).getAction().toLowerCaseName(), is("delete"));
        assertThat(deltaFileRecords.get(1).getObjectClass(), is("inet6num"));
        assertThat(deltaFileRecords.get(1).getPrimaryKey(), is("::/0"));
    }

    @Test
    public void should_get_correct_objectClass_in_delta_file() throws JSONException, JsonProcessingException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final RpslObject deleteObject = RpslObject.parse("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.deleteObject(deleteObject);

        deltaFileGenerator.createDeltas();
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final String[] testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        final List<DeltaFileRecord> deltaFileRecords = getDeltaChanges(testDelta);

        assertThat(deltaFileRecords.size(), is(1));
        assertNrtmFileInfo(testDelta[0], "delta", 2, "TEST");

        assertThat(deltaFileRecords.get(0).getAction().toLowerCaseName(), is("delete"));
        assertThat(deltaFileRecords.get(0).getObjectClass(), is("aut-num"));
        assertThat(deltaFileRecords.get(0).getPrimaryKey(), is("AS102"));
    }

    @Test
    public void multiple_delta_should_has_same_session_different_version() throws JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();
        generateDeltas(Collections.singletonList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST")));
        updateNotificationFileGenerator.generateFile();
        final String[] firstDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        generateDeltas(Collections.singletonList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test:Second update\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST")));
        updateNotificationFileGenerator.generateFile();
        final String[] secondDelta = getDeltasFromUpdateNotificationBySource("TEST", 1);
        assertThat(getNrtmVersionInfo(firstDelta[0]).getVersion(), is(not(getNrtmVersionInfo(secondDelta[0]).getVersion())));
        assertThat(getNrtmVersionInfo(firstDelta[0]).getSessionID(), is(getNrtmVersionInfo(secondDelta[0]).getSessionID()));
    }

    @Test
    public void delta_should_have_same_session_source_than_update_notification() throws JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();
        generateDeltas(Collections.singletonList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST")));
        updateNotificationFileGenerator.generateFile();
        final UpdateNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final String[] testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        final NrtmVersionRecord nrtmVersionFile = getNrtmVersionInfo(testDelta[0]);
        assertThat(nrtmVersionFile.getVersion(), is(testUpdateNotification.getVersion()));
        assertThat(nrtmVersionFile.getSource().getName(), is(testUpdateNotification.getSource().getName()));
        assertThat(nrtmVersionFile.getSessionID(), is(testUpdateNotification.getSessionID()));
    }

    @Test
    public void snapshot_should_match_last_delta_version() throws JSONException {
        final RpslObject updatedObject = RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for thread\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        generateDeltas(Collections.singletonList(updatedObject));
        generateDeltas(Collections.singletonList(updatedObject));
        generateDeltas(Collections.singletonList(updatedObject));

        snapshotFileGenerator.createSnapshot();


        updateNotificationFileGenerator.generateFile();
        final UpdateNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final String[] testDelta = getDeltasFromUpdateNotificationBySource("TEST", 2);

        assertThat(testUpdateNotification.getSnapshot().getVersion(), is(new JSONObject(testDelta[0]).getLong("version")));

    }

    @Test
    public void should_throw_exception_delta_file_not_found()  {
        final Response response = getResponseFromHttpsRequest("TEST-NONAUTH/nrtm-delta.1.TEST-NONAUTH" +
                ".60b9e8c4e4891411be.json", MediaType.APPLICATION_JSON);
        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("Requested Delta file does not exists"));
    }

    @Test
    public void should_delete_old_delta_files() {
        setTime(LocalDateTime.now().minusDays(2));

        snapshotFileGenerator.createSnapshot();
        generateDeltas(Lists.newArrayList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST"), RpslObject.parse("" +
                "mntner:        NONAUTH-OWNER-MNT\n" +
                "descr:         Non auth Owner Maintainer updated\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH")));

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile publishableFile = getNotificationFileBySource("TEST-NONAUTH");
        assertThat(publishableFile.getDeltas().size(), is(1));
        assertThat(publishableFile.getDeltas().get(0).getVersion(), is(2L));

        setTime(LocalDateTime.now());

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        generateDeltas(Lists.newArrayList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for test\n" +
                "country:        BR\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST"), RpslObject.parse("" +
                "mntner:        NONAUTH-OWNER-MNT\n" +
                "descr:         Non auth Owner Maintainer updated\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH")));

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile firstIteration = getNotificationFileBySource("TEST-NONAUTH");
        assertThat(firstIteration.getDeltas().size(), is(1));
        assertThat(firstIteration.getDeltas().get(0).getVersion(), is(3L));
        assertThat(publishableFile.getSnapshot().getVersion(), is(not(firstIteration.getSnapshot().getVersion())));
    }
}
