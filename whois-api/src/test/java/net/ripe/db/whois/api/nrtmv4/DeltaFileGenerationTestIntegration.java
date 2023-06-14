package net.ripe.db.whois.api.nrtmv4;

import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.api.AbstractNrtmIntegrationTest;
import net.ripe.db.whois.common.rpsl.DummifierNrtmV4;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Collections;

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
    public void should_get_delta_file() {
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

        final PublishableDeltaFile testDelta= getDeltasFromUpdateNotificationBySource("TEST", 0);
        assertThat(testDelta.getNrtmVersion(), is(4));
        assertThat(testDelta.getVersion(), is(2L));
        assertThat(testDelta.getChanges().size(), is(1));

        assertThat(testDelta.getChanges().get(0).getAction().toLowerCaseName(), is("add_modify"));
        assertThat(testDelta.getChanges().get(0).getObject().toString(), is(dummifierNrtmV4.dummify(updatedObject).toString()));
    }

    @Test
    public void should_get_delta_file_sequence_versions() {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile publishableNotificationFile = getNotificationFileBySource("TEST");
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

        final PublishableDeltaFile firstIterationDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        generateDeltas(Collections.singletonList(updatedObject));
        updateNotificationFileGenerator.generateFile();

        final PublishableDeltaFile secondIterationDelta = getDeltasFromUpdateNotificationBySource("TEST", 1);

        assertThat(publishableNotificationFile.getSnapshot().getVersion(), is(1L));
        assertThat(firstIterationDelta.getVersion(), is(2L));
        assertThat(secondIterationDelta.getVersion(), is(3L));

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
        final PublishableNotificationFile firsIteration = getNotificationFileBySource("TEST");
        assertThat(firsIteration.getDeltas().get(0).getUrl(), is(notNullValue()));
        assertThat(firsIteration.getSessionID(), is(notNullValue()));
        assertThat(firsIteration.getDeltas().get(0).getVersion(), is(notNullValue()));
        assertThat(firsIteration.getDeltas().get(0).getHash(), is(notNullValue()));
    }
    @Test
    public void delta_should_have_same_version_different_session_per_source() {
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
                "referral-by:   NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH")));
        updateNotificationFileGenerator.generateFile();
        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        final PublishableDeltaFile nonAuthDelta = getDeltasFromUpdateNotificationBySource("TEST-NONAUTH", 0);

        assertThat(testDelta.getType(), is(NrtmDocumentType.DELTA));
        assertThat(nonAuthDelta.getType(), is(NrtmDocumentType.DELTA));
        assertThat(testDelta.getVersion(), is(nonAuthDelta.getVersion()));
        assertThat(testDelta.getSource().getName(), is("TEST"));
        assertThat(nonAuthDelta.getSource().getName(), is("TEST-NONAUTH"));
        assertThat(testDelta.getSessionID(), is(not(nonAuthDelta.getSessionID())));

    }

    @Test
    public void should_get_delta_file_correct_order() {
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

        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        assertThat(testDelta.getChanges().size(), is(2));
        assertThat(testDelta.getVersion(), is(2L));

        final DeltaChange updateChange = testDelta.getChanges().get(0);
        assertThat(updateChange.getAction().toLowerCaseName(), is("add_modify"));
        assertThat(updateChange.getObject().toString(), is(dummifierNrtmV4.dummify(updatedObject).toString()));

        final DeltaChange deleteChange = testDelta.getChanges().get(1);
        assertThat(deleteChange.getAction().toLowerCaseName(), is("delete"));
        assertThat(deleteChange.getObjectType().toString(), is("INET6NUM"));
        assertThat(deleteChange.getPrimaryKey(), is("::/0"));
    }

    @Test
    public void multiple_delta_should_has_same_session_different_version() {
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
        final PublishableDeltaFile firstDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
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
        final PublishableDeltaFile secondDelta = getDeltasFromUpdateNotificationBySource("TEST", 1);
        assertThat(firstDelta.getVersion(), is(not(secondDelta.getVersion())));
        assertThat(firstDelta.getSessionID(), is(secondDelta.getSessionID()));
    }

    @Test
    public void delta_should_have_same_session_source_than_update_notification()  {
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
        final PublishableNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        assertThat(testDelta.getType(), is(NrtmDocumentType.DELTA));
        assertThat(testDelta.getVersion(), is(testUpdateNotification.getVersion()));
        assertThat(testDelta.getSource().getName(), is(testUpdateNotification.getSource().getName()));
        assertThat(testDelta.getSessionID(), is(testUpdateNotification.getSessionID()));
    }

    @Test
    public void snapshot_should_match_last_delta_version(){
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
        final PublishableNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 2);

        assertThat(testUpdateNotification.getSnapshot().getVersion(), is(testDelta.getVersion()));
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
                "referral-by:   NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH")));

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile publishableFile = getNotificationFileBySource("TEST-NONAUTH");
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
                "referral-by:   NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH")));

        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile firstIteration = getNotificationFileBySource("TEST-NONAUTH");
        assertThat(firstIteration.getDeltas().size(), is(1));
        assertThat(firstIteration.getDeltas().get(0).getVersion(), is(3L));
        assertThat(publishableFile.getSnapshot().getVersion(), is(not(firstIteration.getSnapshot().getVersion())));
    }
}
