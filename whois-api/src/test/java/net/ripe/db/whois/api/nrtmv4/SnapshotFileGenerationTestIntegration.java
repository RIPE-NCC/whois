package net.ripe.db.whois.api.nrtmv4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoDao;
import net.ripe.db.nrtm4.domain.DeltaFileRecord;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.nrtm4.domain.SnapshotFileRecord;
import net.ripe.db.nrtm4.domain.UpdateNotificationFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.api.AbstractNrtmIntegrationTest;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SnapshotFileGenerationTestIntegration extends AbstractNrtmIntegrationTest {

    @Autowired
    NrtmVersionInfoDao nrtmVersionInfoDao;
    @Test
    public void should_get_all_source_links() {
        createNrtmSource();

        final String response = getResponseFromHttpsRequest(null, MediaType.TEXT_HTML).readEntity(String.class);

        assertThat(response, is("<html><header><title>NRTM Version 4</title></header><body><a href='https://nrtm.db.ripe.net/TEST/update-notification-file.json'>TEST</a><br/><a href='https://nrtm.db.ripe.net/TEST-NONAUTH/update-notification-file.json'>TEST-NONAUTH</a><br/><body></html>"));
    }

    @Test
    public void should_generate_snapshot_empty_database() throws IOException, JSONException {
        databaseHelper.setup();

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final String[] records = getSnapshotRecords("TEST");
        assertNrtmFileInfo(records[0], "snapshot", 1, "TEST");

        final List<SnapshotFileRecord> snapshotRecords = getSnapshotRecords(records);
        assertThat(snapshotRecords.size(), is(0));
    }

    @Test
    public void should_have_session_version_hash_value(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile updateNotificationFile = getNotificationFileBySource("TEST");

        assertThat(updateNotificationFile.getSnapshot().getUrl(), is(notNullValue()));
        assertThat(updateNotificationFile.getSnapshot().getVersion(), is(notNullValue()));
        assertThat(updateNotificationFile.getSessionID(), is(notNullValue()));
        assertThat(updateNotificationFile.getSnapshot().getHash(), is(notNullValue()));
    }
    @Test
    public void should_get_snapshot_file_test_source() throws IOException, JSONException {

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST");

        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString(HttpHeaders.CACHE_CONTROL), is("public, max-age=604800"));

        final String[] records = getSnapshotRecords(response.readEntity(byte[].class));
        assertNrtmFileInfo(records[0], "snapshot", 1, "TEST");

        final List<SnapshotFileRecord> snapshotRecords = getSnapshotRecords(records);

        assertThat(snapshotRecords.size(), is(7));
        assertThat(snapshotRecords.stream().map( record -> record.getObject().getKey().toString()).collect(Collectors.toSet()), containsInAnyOrder("::/0",
                "0.0.0.0 - 255.255.255.255",
                "AS100 - AS200",
                "AS102",
                "31.12.202.in-addr.arpa",
                "OWNER-MNT",
                "ORG-TEST1-TEST"));
    }

    @Test
    public void should_get_snapshot_file_test_non_auth_source() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST-NONAUTH");

        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString(HttpHeaders.CACHE_CONTROL), is("public, max-age=604800"));

        final String[] records = getSnapshotRecords(response.readEntity(byte[].class));
        assertNrtmFileInfo(records[0], "snapshot", 1, "TEST-NONAUTH");

        final List<SnapshotFileRecord> snapshotRecords = getSnapshotRecords(records);
        assertThat(snapshotRecords.size(), is(1));
        assertThat(snapshotRecords.get(0).getObject().getKey().toString(), is("NONAUTH-OWNER-MNT"));
    }
    @Test
    public void snapshot_should_have_same_version_different_session_per_source() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final NrtmVersionRecord testSnapshot = getNrtmVersionInfo(getSnapshotRecords("TEST")[0]);
        final NrtmVersionRecord nonAuthSnapshot = getNrtmVersionInfo(getSnapshotRecords("TEST-NONAUTH")[0]);

        assertThat(testSnapshot.getVersion(), is(nonAuthSnapshot.getVersion()));
        assertThat(testSnapshot.getSessionID(), is(not(nonAuthSnapshot.getSessionID())));
    }

    @Test
    public void should_generate_snapshot_after_one_day_with_same_session() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final NrtmVersionRecord firstSnapResponse = getNrtmVersionInfo(getSnapshotRecords("TEST")[0]);
        setTime(LocalDateTime.now().plusDays(1).withHour(23));

        final RpslObject updatedObject = RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          Test Object\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");
        databaseHelper.updateObject(updatedObject);

        deltaFileGenerator.createDeltas();
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final NrtmVersionRecord secondSnapResponse = getNrtmVersionInfo(getSnapshotRecords("TEST")[0]);

        assertThat(firstSnapResponse.getType().lowerCaseName(), is("snapshot"));
        assertThat(firstSnapResponse.getVersion(), is(1L));

        assertThat(secondSnapResponse.getType().lowerCaseName(), is("snapshot"));
        assertThat(secondSnapResponse.getVersion(), is(2L));

        assertThat(firstSnapResponse.getSessionID(), is(secondSnapResponse.getSessionID()));
    }

    @Test
    public void should_not_generate_other_snapshot_after_one_day_without_changes() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        setTime(LocalDateTime.now().plusDays(1).withHour(23));

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final NrtmVersionRecord firstSnapResponse = getNrtmVersionInfo(getSnapshotRecords("TEST")[0]);
        assertThat(firstSnapResponse.getType().lowerCaseName(), is("snapshot"));
        assertThat(firstSnapResponse.getVersion(), is(1L));
    }

    @Test
    public void snapshot_should_have_same_session_source_than_update_notification() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final NrtmVersionRecord snapResponse = getNrtmVersionInfo(getSnapshotRecords("TEST")[0]);

        assertThat(snapResponse.getType().lowerCaseName(), is("snapshot"));
        assertThat(snapResponse.getVersion(), is(testUpdateNotification.getVersion()));
        assertThat(snapResponse.getSource().getName(), is(testUpdateNotification.getSource().getName()));
        assertThat(snapResponse.getSessionID(), is(testUpdateNotification.getSessionID()));
    }

    @Test
    public void should_throw_exception_invalid_filename()  {
        final Response response = getResponseFromHttpsRequest("TEST/nrtm-pshot.1.TEST" +
                ".f7c94b039f9743fa4d6368b54e64bb0f", MediaType.APPLICATION_OCTET_STREAM);
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Invalid Nrtm filename"));

    }

    @Test
    public void should_throw_exception_snapshot_file_not_found()  {
        final Response response = getResponseFromHttpsRequest("TEST-NONAUTH/nrtm-snapshot.1.TEST-NONAUTH.4ef06e8c4e4891411be" +
                ".json.gz", MediaType.APPLICATION_OCTET_STREAM);
        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("Requested Snapshot file does not exists"));
    }

    @Test
    public void should_throw_exception_invalid_source_filename_combo()  {
        final Response response = getResponseFromHttpsRequest("TEST/nrtm-snapshot.1.TEST-NONAUTH.4e9e8c4e4891411be" +
                ".json.gz", MediaType.APPLICATION_OCTET_STREAM);
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Invalid source and filename combination"));
    }

    @Test
    public void snapshot_should_match_last_delta_version_while_creating_at_same_time() throws IOException, JSONException {
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

        final RpslObject ORG_TEST2 = RpslObject.parse("" +
                "organisation:  ORG-TEST2-TEST\n" +
                "org-name:      Test2 organisation\n" +
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

        snapshotFileGenerator.createSnapshot();

        databaseHelper.updateObject( "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for first time\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        deltaFileGenerator.createDeltas();

        Thread thread = new Thread(() -> {

                databaseHelper.updateObject(updatedObject);
                deltaFileGenerator.createDeltas();

                databaseHelper.addObject(ORG_TEST2);
                deltaFileGenerator.createDeltas();

                databaseHelper.deleteObject(RpslObject.parse("as-block:       AS100 - AS200\n" +
                        "descr:          ARIN ASN block\n" +
                        "org:            ORG-TEST1-TEST\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:         2022-08-14T11:48:28Z\n" +
                        "last-modified:   2022-10-25T12:22:39Z\n" +
                        "source:         TEST"));
                deltaFileGenerator.createDeltas();
        });

        thread.start();

        snapshotFileGenerator.createSnapshot();

        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        updateNotificationFileGenerator.generateFile();

        final UpdateNotificationFile notificationFile = getNotificationFileBySource("TEST");
        assertThat(notificationFile.getType(), is(NrtmDocumentType.NOTIFICATION));
        assertThat(notificationFile.getVersion(), is(5L));

        final String[] testSnapshot = getSnapshotRecords("TEST");
        assertThat(getNrtmVersionInfo(testSnapshot[0]).getType().lowerCaseName(), is("snapshot"));

        final List<SnapshotFileRecord> snapshotRecords = getSnapshotRecords(testSnapshot);

        assertThat(snapshotRecords.stream().map(record -> record.getObject().getKey().toString()).toList(), not(contains("ORG-TEST2-TEST")));
        assertThat(snapshotRecords.stream().map(record -> record.getObject().getKey().toString()).toList(), hasItem("AS100 - AS200"));

        final String[] testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        final List<DeltaFileRecord> deltaFileRecords = getDeltaChanges(testDelta);

        assertThat(deltaFileRecords.size(), is(1));
        assertThat(deltaFileRecords.get(0).getObject().toString(),
                is(snapshotRecords.stream().filter( record -> deltaFileRecords.get(0).getObject().getKey().equals(record.getObject().getKey())).findAny().get().getObject().toString()));

        assertThat(getNrtmVersionInfo(testDelta[0]).getVersion(), is(getNrtmVersionInfo(testSnapshot[0]).getVersion()));
    }

    @Test
    public void should_delete_old_snapshot_files()  {
        setTime(LocalDateTime.now().minusDays(2));
        snapshotFileGenerator.createSnapshot();

        generateDeltas(Lists.newArrayList(RpslObject.parse("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space:Updated for tesint\n" +
                "country:        AR\n" +
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

        snapshotFileGenerator.createSnapshot();

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource1 = nrtmVersionInfoDao.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));

        assertThat(versionsBySource1.get(CIString.ciString("TEST")).size(), is(2));
        assertThat(versionsBySource1.get(CIString.ciString("TEST-NONAUTH")).size(), is(2));
        assertThat(versionsBySource1.get(CIString.ciString("TEST-NONAUTH")).get(0).version(), is(2L));
        assertThat(versionsBySource1.get(CIString.ciString("TEST-NONAUTH")).get(1).version(), is(1L));
        assertThat(versionsBySource1.get(CIString.ciString("TEST")).get(0).version(), is(2L));
        assertThat(versionsBySource1.get(CIString.ciString("TEST")).get(1).version(), is(1L));

        setTime(LocalDateTime.now());

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

        snapshotFileGenerator.createSnapshot();

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource = nrtmVersionInfoDao.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));

        assertThat(versionsBySource.get(CIString.ciString("TEST")).size(), is(2));
        assertThat(versionsBySource.get(CIString.ciString("TEST-NONAUTH")).size(), is(2));
        assertThat(versionsBySource.get(CIString.ciString("TEST-NONAUTH")).get(0).version(), is(3L));
        assertThat(versionsBySource.get(CIString.ciString("TEST-NONAUTH")).get(1).version(), is(2L));
        assertThat(versionsBySource.get(CIString.ciString("TEST")).get(0).version(), is(3L));
        assertThat(versionsBySource.get(CIString.ciString("TEST")).get(1).version(), is(2L));
        assertThat(versionsBySource.get(CIString.ciString("TEST-NONAUTH")).get(0).created(), is(not(versionsBySource1.get(CIString.ciString("TEST-NONAUTH")).get(0).created())));
        assertThat(versionsBySource.get(CIString.ciString("TEST")).get(0).created(), is(not(versionsBySource1.get(CIString.ciString("TEST")).get(0).created())));
    }

    @Test
    public void should_not_delete_old_snapshot_files_if_only_one_exists()  {
        setTime(LocalDateTime.now().minusDays(2));
        snapshotFileGenerator.createSnapshot();

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource1 = nrtmVersionInfoDao.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));

        setTime(LocalDateTime.now());
        snapshotFileGenerator.createSnapshot();

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource = nrtmVersionInfoDao.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));
        assertThat(versionsBySource.get(CIString.ciString("TEST-NONAUTH")).get(0).created(), is(versionsBySource1.get(CIString.ciString("TEST-NONAUTH")).get(0).created()));
        assertThat(versionsBySource.get(CIString.ciString("TEST")).get(0).created(), is(versionsBySource1.get(CIString.ciString("TEST")).get(0).created()));
    }

    public static String decompress(byte[] compressed) throws IOException {
        try (Reader reader = new InputStreamReader(new GzipCompressorInputStream(new ByteArrayInputStream(compressed)))) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    public String[] getSnapshotRecords(byte[] compressed) throws IOException {
        return StringUtils.split( decompress(compressed), NrtmFileUtil.RECORD_SEPERATOR);
    }

    private String[] getSnapshotRecords(final String source) throws IOException {
        return getSnapshotRecords(getSnapshotFromUpdateNotificationBySource(source).readEntity(byte[].class));
    }


    @NotNull
    protected List<SnapshotFileRecord> getSnapshotRecords(final String[] records) throws JsonProcessingException {
        final List<SnapshotFileRecord> snapshotRecords = Lists.newArrayList();

        for (int i = 1; i < records.length; i++) {
            snapshotRecords.add(new ObjectMapper().readValue(records[i].toString(), SnapshotFileRecord.class));
        }
        return snapshotRecords;
    }
}
