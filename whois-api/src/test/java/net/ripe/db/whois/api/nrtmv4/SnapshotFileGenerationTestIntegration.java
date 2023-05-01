package net.ripe.db.whois.api.nrtmv4;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.api.AbstractNrtmIntegrationTest;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.FileCopyUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    NrtmVersionInfoRepository nrtmVersionInfoRepository;
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

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST");
        final JSONObject jsonObject = new JSONObject(decompress(response.readEntity(byte[].class)));
        final JSONArray objects = jsonObject.getJSONArray("objects");

        final List<String> rpslKeys = Lists.newArrayList();

        for (int i = 0; i < objects.length(); i++) {
            rpslKeys.add(RpslObject.parse(objects.getString(i)).getKey().toString());
        }
        assertThat(rpslKeys.size(), is(0));
    }

    @Test
    public void should_have_session_version_hash_value(){
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile publishableNotificationFile = getNotificationFileBySource("TEST");

        assertThat(publishableNotificationFile.getSnapshot().getUrl(), is(notNullValue()));
        assertThat(publishableNotificationFile.getSnapshot().getVersion(), is(notNullValue()));
        assertThat(publishableNotificationFile.getSessionID(), is(notNullValue()));
        assertThat(publishableNotificationFile.getSnapshot().getHash(), is(notNullValue()));
    }
    @Test
    public void should_get_snapshot_file_test_source() throws IOException, JSONException {

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST");

        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString(HttpHeaders.CACHE_CONTROL), is("public, max-age=604800"));

        final JSONObject jsonObject = new JSONObject(decompress(response.readEntity(byte[].class)));
        assertNrtmFileInfo(jsonObject, "snapshot", 1);

        final JSONArray objects = jsonObject.getJSONArray("objects");
        final List<String> rpslKeys = Lists.newArrayList();

        for (int i = 0; i < objects.length(); i++) {
            rpslKeys.add(RpslObject.parse(objects.getString(i)).getKey().toString());
        }

        assertThat(rpslKeys.size(), is(7));
        assertThat(rpslKeys, containsInAnyOrder("::/0",
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

        final JSONObject jsonObject = new JSONObject(decompress(response.readEntity(byte[].class)));
        assertThat(jsonObject.getInt("nrtm_version"), is(4));
        assertThat(jsonObject.getString("type"), is("snapshot"));
        assertThat(jsonObject.getString("source"), is("TEST-NONAUTH"));
        assertThat(jsonObject.getInt("version"), is(1));

        final JSONArray objects = jsonObject.getJSONArray("objects");
        final List<String> rpslKeys = Lists.newArrayList();

        for (int i = 0; i < objects.length(); i++) {
            rpslKeys.add(RpslObject.parse(objects.getString(i)).getKey().toString());
        }

        assertThat(rpslKeys.size(), is(1));
        assertThat(rpslKeys.get(0), is("NONAUTH-OWNER-MNT"));
    }
    @Test
    public void snapshot_should_have_same_version_different_session_per_source() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final Response testResponse = getSnapshotFromUpdateNotificationBySource("TEST");
        final Response testNonAuthResponse = getSnapshotFromUpdateNotificationBySource("TEST-NONAUTH");

        final JSONObject testSnapshot = new JSONObject(decompress(testResponse.readEntity(byte[].class)));
        final JSONObject nonAuthSnapshot = new JSONObject(decompress(testNonAuthResponse.readEntity(byte[].class)));

        assertThat(testSnapshot.getInt("version"), is(nonAuthSnapshot.getInt("version")));
        assertThat(testSnapshot.getString("session_id"), is(not(nonAuthSnapshot.getString("session_id"))));
    }

    @Test
    public void should_generate_snapshot_after_one_day_with_same_session() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final Response firstSnapResponse = getSnapshotFromUpdateNotificationBySource("TEST");
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

        final Response secondSnapResponse = getSnapshotFromUpdateNotificationBySource("TEST");

        final JSONObject firstSnapshot = new JSONObject(decompress(firstSnapResponse.readEntity(byte[].class)));
        final JSONObject secondSnapshot = new JSONObject(decompress(secondSnapResponse.readEntity(byte[].class)));
        assertThat(firstSnapshot.getString("type"), is("snapshot"));
        assertThat(firstSnapshot.getInt("version"), is(1));

        assertThat(secondSnapshot.getString("type"), is("snapshot"));
        assertThat(secondSnapshot.getInt("version"), is(2));

        assertThat(firstSnapshot.getString("session_id"), is(secondSnapshot.getString("session_id")));

    }

    @Test
    public void should_not_generate_other_snapshot_after_one_day_without_changes() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        setTime(LocalDateTime.now().plusDays(1).withHour(23));

        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST");

        final JSONObject testSnapshot = new JSONObject(decompress(response.readEntity(byte[].class)));
        assertThat(testSnapshot.getString("type"), is("snapshot"));
        assertThat(testSnapshot.getInt("version"), is(1));
    }

    @Test
    public void snapshot_should_have_same_session_source_than_update_notification() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshot();
        updateNotificationFileGenerator.generateFile();

        final PublishableNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final Response snapResponse = getSnapshotFromUpdateNotificationBySource("TEST");

        final JSONObject testSnapshot = new JSONObject(decompress(snapResponse.readEntity(byte[].class)));

        assertThat(testSnapshot.getString("type"), is("snapshot"));
        assertThat(testSnapshot.getString("version"), is(String.valueOf(testUpdateNotification.getVersion())));
        assertThat(testSnapshot.getString("source"), is(testUpdateNotification.getSource().getName().toString()));
        assertThat(testSnapshot.getString("session_id"), is(String.valueOf(testUpdateNotification.getSessionID())));
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

        final PublishableNotificationFile notificationFile = getNotificationFileBySource("TEST");
        assertThat(notificationFile.getType(), is(NrtmDocumentType.NOTIFICATION));
        assertThat(notificationFile.getVersion(), is(5L));

        final JSONObject testSnapshot = new JSONObject(decompress(getSnapshotFromUpdateNotificationBySource("TEST").readEntity(byte[].class)));
        assertThat(testSnapshot.getString("type"), is("snapshot"));

        final JSONArray objects = testSnapshot.getJSONArray("objects");
        final List<RpslObject> rpslObjects = Lists.newArrayList();

        for (int i = 0; i < objects.length(); i++) {
            rpslObjects.add(RpslObject.parse(objects.getString(i)));
        }

        assertThat(rpslObjects.stream().map(rpslObject -> rpslObject.getKey().toString()).toList(), not(contains("ORG-TEST2-TEST")));
        assertThat(rpslObjects.stream().map(rpslObject -> rpslObject.getKey().toString()).toList(), hasItem("AS100 - AS200"));


        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        assertThat(testDelta.getNrtmVersion(), is(4));

        assertThat(testDelta.getChanges().get(0).getObject().toString(), is(rpslObjects.stream().filter( rpslObject -> testDelta.getChanges().get(0).getObject().getKey().equals(rpslObject.getKey())).findAny().get().toString()));
        assertThat(testDelta.getVersion(), is(testSnapshot.getLong("version")));
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

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource1 = nrtmVersionInfoRepository.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
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

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource = nrtmVersionInfoRepository.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
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

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource1 = nrtmVersionInfoRepository.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));

        setTime(LocalDateTime.now());
        snapshotFileGenerator.createSnapshot();

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource = nrtmVersionInfoRepository.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));
        assertThat(versionsBySource.get(CIString.ciString("TEST-NONAUTH")).get(0).created(), is(versionsBySource1.get(CIString.ciString("TEST-NONAUTH")).get(0).created()));
        assertThat(versionsBySource.get(CIString.ciString("TEST")).get(0).created(), is(versionsBySource1.get(CIString.ciString("TEST")).get(0).created()));
    }

    public static String decompress(byte[] compressed) throws IOException {
        try (Reader reader = new InputStreamReader(new GzipCompressorInputStream(new ByteArrayInputStream(compressed)))) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    private static void assertNrtmFileInfo(final JSONObject jsonObject, final String type, final int version) throws JSONException {
        assertThat(jsonObject.getInt("nrtm_version"), is(4));
        assertThat(jsonObject.getString("type"), is(type));
        assertThat(jsonObject.getString("source"), is("TEST"));
        assertThat(jsonObject.getInt("version"), is(version));
    }
}
