package net.ripe.db.whois.api.nrtmv4;

import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.DeltaFileGenerator;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.api.AbstractNrtmIntegrationTest;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DeltaFileGenerationTestIntegration extends AbstractNrtmIntegrationTest {
    @Autowired
    DummifierNrtm dummifierNrtm;

    @Test
    public void should_get_delta_file() {
        snapshotFileGenerator.createSnapshots();
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

        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        assertThat(testDelta.getVersion(), is(2L));
        assertThat(testDelta.getChanges().size(), is(1));

        assertThat(testDelta.getChanges().get(0).getAction().toLowerCaseName(), is("add_modify"));
        assertThat(testDelta.getChanges().get(0).getObject().toString(), is(dummifierNrtm.dummify(4, updatedObject).toString()));
    }

    @Test
    public void delta_should_have_same_version_different_session_per_source() {
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
        updateNotificationFileGenerator.generateFile();

        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        assertThat(testDelta.getChanges().size(), is(2));
        assertThat(testDelta.getVersion(), is(2L));

        final DeltaChange updateChange = testDelta.getChanges().get(0);
        assertThat(updateChange.getAction().toLowerCaseName(), is("add_modify"));
        assertThat(updateChange.getObject().toString(), is(dummifierNrtm.dummify(4, updatedObject).toString()));

        final DeltaChange deleteChange = testDelta.getChanges().get(1);
        assertThat(deleteChange.getAction().toLowerCaseName(), is("delete"));
        assertThat(deleteChange.getObjectType().toString(), is("INET6NUM"));
        assertThat(deleteChange.getPrimaryKey(), is("::/0"));
    }

    @Test
    public void multiple_delta_should_has_same_session_different_version() {
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
        updateNotificationFileGenerator.generateFile();

        List<Thread> threads = new ArrayList<>();

        for(int deltaThreadsCount = 0; deltaThreadsCount < 3; deltaThreadsCount++) {
            final RpslObject updatedObject = RpslObject.parse("" +
                    "inet6num:       ::/0\n" +
                    "netname:        IANA-BLK\n" +
                    "descr:          The whole IPv6 address space:Updated for thread " + deltaThreadsCount + "\n" +
                    "country:        NL\n" +
                    "tech-c:         TP1-TEST\n" +
                    "admin-c:        TP1-TEST\n" +
                    "status:         OTHER\n" +
                    "mnt-by:         OWNER-MNT\n" +
                    "created:         2022-08-14T11:48:28Z\n" +
                    "last-modified:   2022-10-25T12:22:39Z\n" +
                    "source:         TEST");
            databaseHelper.updateObject(updatedObject);

            final Runnable task = new DeltaGeneratorRunnable(deltaFileGenerator);
            final Thread worker = new Thread(task);
            worker.setName(String.valueOf(deltaThreadsCount));
            threads.add(worker);
            worker.start();
        }

        snapshotFileGenerator.createSnapshots();

        final Instant end = Instant.now().plusSeconds(3L);
        int running = 0;
        do {
            for (final Thread thread:threads) {
                if(thread.isAlive()){
                    running ++;
                }
            }
        } while (running > 0 && Instant.now().isBefore(end));

        updateNotificationFileGenerator.generateFile();
        final PublishableNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 2);

        assertThat(testUpdateNotification.getSnapshot().getVersion(), is(testDelta.getVersion()));
    }

    @Test
    public void should_throw_exception_delta_file_not_found()  {
        final Response response = createResource("TEST-NONAUTH/nrtm-delta.1.TEST-NONAUTH.60b9e8c4e4891411be.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("Requested Delta file does not exists"));
    }

    private PublishableDeltaFile getDeltasFromUpdateNotificationBySource(final String sourceName, final int deltaPosition) {
        final PublishableNotificationFile updateNotificationResponse = createResource(sourceName + "/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableNotificationFile.class);
        return createResource(sourceName + "/" + getDeltaNameFromUpdateNotification(updateNotificationResponse, deltaPosition))
                .request(MediaType.APPLICATION_JSON)
                .get(PublishableDeltaFile.class);
    }

    private String getDeltaNameFromUpdateNotification(final PublishableNotificationFile notificationFile, final int deltaPosition) {
        return notificationFile.getDeltas().get(deltaPosition).getUrl().split("/")[4];
    }

    private void generateDeltas(final List<RpslObject> updatedObject){
        for (final RpslObject rpslObject : updatedObject) {
            databaseHelper.updateObject(rpslObject);
        }
        deltaFileGenerator.createDeltas();
    }

    private static class DeltaGeneratorRunnable implements Runnable {

        private DeltaFileGenerator deltaFileGenerator;

        DeltaGeneratorRunnable(final DeltaFileGenerator deltaFileGenerator){
            try {
                this.deltaFileGenerator = deltaFileGenerator;
            }catch(final Exception ex){
                Thread.currentThread().interrupt();
            }
        }
        @Override
        public void run() {
            deltaFileGenerator.createDeltas();
        }
    }
}
