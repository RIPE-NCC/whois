package net.ripe.db.whois.api.nrtmv4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.NrtmFileProcessor;
import net.ripe.db.nrtm4.dao.DeltaFileDao;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    NrtmFileProcessor nrtmFileProcessor;
    @Autowired
    DummifierNrtm dummifierNrtm;

    @Autowired
    TestDateTimeProvider dateTimeProvider;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    SnapshotFileRepository snapshotFileRepository;

    @Autowired
    DeltaFileDao deltaFileDao;

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
    }

    //SOURCE LINKS

    @Test
    public void should_get_all_source_links() {
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 1, "TEST");
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 2, "TEST-NONAUTH");

        final String response = RestTest.target(getPort(), "nrtmv4/")
                .request(MediaType.TEXT_HTML)
                .get(String.class);

        assertThat(response, is("<html><header><title>NRTM Version 4</title></header><body><a href='https://nrtm.db.ripe.net/TEST/update-notification-file.json'>TEST</a><br/><a href='https://nrtm.db.ripe.net/TEST-NONAUTH/update-notification-file.json'>TEST-NONAUTH</a><br/><body></html>"));
    }

    //SNAPSHOT
    @Test
    public void should_get_snapshot_file_test_source() throws IOException, JSONException {

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

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
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

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
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        final Response testResponse = getSnapshotFromUpdateNotificationBySource("TEST");
        final Response testNonAuthResponse = getSnapshotFromUpdateNotificationBySource("TEST-NONAUTH");

        final JSONObject testSnapshot = new JSONObject(decompress(testResponse.readEntity(byte[].class)));
        final JSONObject nonAuthSnapshot = new JSONObject(decompress(testNonAuthResponse.readEntity(byte[].class)));

        assertThat(testSnapshot.getInt("version"), is(nonAuthSnapshot.getInt("version")));
        assertThat(testSnapshot.getString("session_id"), is(not(nonAuthSnapshot.getString("session_id"))));
    }

    @Test
    public void should_generate_snapshot_after_one_day() throws IOException, JSONException {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
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

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST");

        final JSONObject testSnapshot = new JSONObject(decompress(response.readEntity(byte[].class)));
        assertThat(testSnapshot.getInt("version"), is(2));
    }

    @Test
    public void should_not_generate_other_snapshot_after_one_day_without_changes() throws IOException, JSONException {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        setTime(LocalDateTime.now().plusDays(1).withHour(23));
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST");

        final JSONObject testSnapshot = new JSONObject(decompress(response.readEntity(byte[].class)));
        assertThat(testSnapshot.getInt("version"), is(1));
    }
    
    // UPDATE NOTIFICATION FILE
    @Test
    public void should_get_update_notification_file_per_source() throws JsonProcessingException {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        final PublishableNotificationFile testPublishableNotificationFile = getNotificationFileBySource("TEST");
        final PublishableNotificationFile nonAuthPublishableNotificationFile = getNotificationFileBySource("TEST-NONAUTH");

        assertThat(testPublishableNotificationFile.getSource().getName(), is("TEST"));
        assertThat(nonAuthPublishableNotificationFile.getSource().getName(), is("TEST-NONAUTH"));
    }

    @Test
    public void snapshot_should_have_same_session_source_than_update_notification() throws IOException, JSONException {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        final PublishableNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final Response snapResponse = getSnapshotFromUpdateNotificationBySource("TEST");

        final JSONObject testSnapshot = new JSONObject(decompress(snapResponse.readEntity(byte[].class)));

        assertThat(testSnapshot.getString("version"), is(String.valueOf(testUpdateNotification.getVersion())));
        assertThat(testSnapshot.getString("source"), is(testUpdateNotification.getSource().getName().toString()));
        assertThat(testSnapshot.getString("session_id"), is(String.valueOf(testUpdateNotification.getSessionID())));
    }

    @Test
    public void delta_should_have_same_session_source_than_update_notification() throws JsonProcessingException {
        generateDelta();
        final PublishableNotificationFile testUpdateNotification = getNotificationFileBySource("TEST");
        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);

        assertThat(testDelta.getVersion(), is(testUpdateNotification.getVersion()));
        assertThat(testDelta.getSource().getName(), is(testUpdateNotification.getSource().getName()));
        assertThat(testDelta.getSessionID(), is(testUpdateNotification.getSessionID()));
    }


    // DELTAS
    @Test
    public void should_get_delta_file() throws JsonProcessingException  {
        final RpslObject updatedObject = generateDelta();
        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        assertThat(testDelta.getVersion(), is(2L));
        assertThat(testDelta.getChanges().size(), is(1));

        assertThat(testDelta.getChanges().get(0).getAction().toLowerCaseName(), is("add_modify"));
        assertThat(testDelta.getChanges().get(0).getObject().toString(), is(dummifierNrtm.dummify(4, updatedObject).toString()));
    }

    @Test
    public void delta_should_have_same_version_different_session_per_source() throws JsonProcessingException {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

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

        final RpslObject updatedNonAuthObject = RpslObject.parse("" +
                "mntner:        NONAUTH-OWNER-MNT\n" +
                "descr:         Non auth Owner Maintainer updated\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        NONAUTH-OWNER-MNT\n" +
                "referral-by:   NONAUTH-OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST-NONAUTH");
        databaseHelper.updateObject(updatedNonAuthObject);

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        final PublishableDeltaFile testDelta = getDeltasFromUpdateNotificationBySource("TEST", 0);
        final PublishableDeltaFile nonAuthDelta = getDeltasFromUpdateNotificationBySource("TEST-NONAUTH", 0);

        assertThat(testDelta.getVersion(), is(nonAuthDelta.getVersion()));
        assertThat(testDelta.getSource().getName(), is("TEST"));
        assertThat(nonAuthDelta.getSource().getName(), is("TEST-NONAUTH"));
        assertThat(testDelta.getSessionID(), is(not(nonAuthDelta.getSessionID())));

    }

    @Test
    public void should_get_delta_file_correct_order() throws JsonProcessingException {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

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

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

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
    public void should_throw_exception_invalid_filename()  {
        final Response response = createResource("TEST/nrtm-pshot.1.TEST.f7c94b039f9743fa4d6368b54e64bb0f")
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .get(Response.class);
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Invalid Nrtm filename"));

    }

    @Test
    public void should_throw_exception_snapshot_file_not_found()  {
        final Response response = createResource("TEST-NONAUTH/nrtm-snapshot.1.TEST-NONAUTH.4ef06e8c4e4891411be.json.gz")
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .get(Response.class);
        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("Requested Snapshot file does not exists"));
    }

    @Test
    public void should_throw_exception_invalid_source_filename_combo()  {
        final Response response = createResource("TEST/nrtm-snapshot.1.TEST-NONAUTH.4e9e8c4e4891411be.json.gz")
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .get(Response.class);
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), is("Invalid source and filename combination"));
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
    public void should_throw_exception_delta_file_not_found()  {
        final Response response = createResource("TEST-NONAUTH/nrtm-delta.1.TEST-NONAUTH.60b9e8c4e4891411be.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("Requested Delta file does not exists"));
    }

    @Test
    public void should_throw_exception_notification_file_not_found() {
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 1, "TEST");
        final Response response = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is("update-notification-file.json does not exists for source TEST"));
    }

    public static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }
    protected WebTarget createResource(final String path) {
        return RestTest.target(getPort(), String.format("nrtmv4/%s", path));
    }

    private static void assertNrtmFileInfo(final JSONObject jsonObject, final String type, final int version) throws JSONException {
        assertThat(jsonObject.getInt("nrtm_version"), is(4));
        assertThat(jsonObject.getString("type"), is(type));
        assertThat(jsonObject.getString("source"), is("TEST"));
        assertThat(jsonObject.getInt("version"), is(version));
    }

    private String getSnapshotNameFromUpdateNotification(final PublishableNotificationFile notificationFile) {
        final String[] splits = notificationFile.getSnapshot().getUrl().split("/");
        return snapshotFileRepository.getByName(splits[4]).get().name();
    }

    private String getDeltaNameFromUpdateNotification(final PublishableNotificationFile notificationFile, final int deltaPosition) {
        final String[] splits = notificationFile.getDeltas().get(deltaPosition).getUrl().split("/");
        return deltaFileDao.getByName(splits[4]).get().name();
    }

    private RpslObject generateDelta(){
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

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

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        return updatedObject;
    }

    private PublishableNotificationFile getNotificationFileBySource(final String sourceName) throws JsonProcessingException {
        final Response response = createResource(sourceName + "/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        return new ObjectMapper().readValue(response.readEntity(String.class), PublishableNotificationFile.class);
    }
    private PublishableDeltaFile getDeltasFromUpdateNotificationBySource(final String sourceName, final int deltaPosition) throws JsonProcessingException {
        final Response updateNotificationResponse = createResource(sourceName + "/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        final PublishableNotificationFile notificationFile = new ObjectMapper().readValue(updateNotificationResponse.readEntity(String.class),
                PublishableNotificationFile.class);
        final Response deltaResponse = createResource(sourceName + "/" + getDeltaNameFromUpdateNotification(notificationFile, deltaPosition))
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        return new ObjectMapper().readValue(deltaResponse.readEntity(String.class), PublishableDeltaFile.class);
    }

    private Response getSnapshotFromUpdateNotificationBySource(final String sourceName) throws JsonProcessingException {
        final Response updateNotificationResponse = createResource(sourceName + "/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        final PublishableNotificationFile notificationFile = new ObjectMapper().readValue(updateNotificationResponse.readEntity(String.class),
                PublishableNotificationFile.class);
        return createResource(sourceName + "/" + getSnapshotNameFromUpdateNotification(notificationFile))
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
    }

}
