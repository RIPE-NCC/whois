package net.ripe.db.whois.api.nrtmv4;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.dao.DeltaFileDao;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.DeltaFileVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    NrtmFileProcessor nrtmFileProcessor;
    @Autowired
    DummifierNrtm dummifierNrtm;
    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    SnapshotFileRepository snapshotFileRepository;
    @Autowired
    DeltaFileDao deltaFileDao;
    @Autowired
    TestDateTimeProvider dateTimeProvider;

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
    public void should_get_snapshot_file() throws IOException, JSONException {

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        Optional<SnapshotFile> fileOptional = snapshotFileRepository.getLastSnapshot(sourceRepository.getWhoisSource().get());

        final Response response = createResource("TEST/" + fileOptional.get().name())
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .get(Response.class);

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
    public void should_get_all_source_links() {
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 1, "TEST");
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 2, "TEST-NONAUTH");

        final String response = RestTest.target(getPort(), "nrtmv4/")
                .request(MediaType.TEXT_HTML)
                .get(String.class);

        assertThat(response, is("<html><header><title>NRTM Version 4</title></header><body><a href='https://nrtm.db.ripe.net/TEST/update-notification-file.json'>TEST</a><br/><a href='https://nrtm.db.ripe.net/TEST-NONAUTH/update-notification-file.json'>TEST-NONAUTH</a><br/><body></html>"));
    }

    @Test
    public void should_get_update_notification_file() {
        insertUpdateNotificationFile();
        final Response response = createResource("TEST/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString(HttpHeaders.CACHE_CONTROL), is("public, max-age=60"));
        assertThat(response.readEntity(String.class), containsString("\"source\":\"TEST\""));

        final Response responseNonAuth = createResource("TEST-NONAUTH/update-notification-file.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        assertThat(responseNonAuth.getStatus(), is(200));
        assertThat(responseNonAuth.getHeaderString(HttpHeaders.CACHE_CONTROL), is("public, max-age=60"));
        assertThat(responseNonAuth.readEntity(String.class), containsString("\"source\":\"TEST-NONAUTH\""));
    }

    @Test
    public void should_get_delta_file() throws JSONException {

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

        Optional<SnapshotFile> snapshotFile = snapshotFileRepository.getLastSnapshot(sourceRepository.getWhoisSource().get());
        final NrtmVersionInfo snapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.get().versionId());

        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

        final List<DeltaFileVersionInfo> deltaFileVersion = deltaFileDao.getDeltasForNotification(snapshotVersion, LocalDateTime.MIN);
        final Response response = createResource("TEST/" + deltaFileVersion.get(0).deltaFile().name())
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertThat(response.getStatus(), is(200));
        assertThat(response.getHeaderString(HttpHeaders.CACHE_CONTROL), is("public, max-age=604800"));

        final JSONObject jsonObject = new JSONObject(response.readEntity(String.class));
        assertNrtmFileInfo(jsonObject, "delta", 2);

        final JSONArray changes = jsonObject.getJSONArray("changes");
        assertThat(changes.length(), is(1));

        final JSONObject deltaChanges = new JSONObject(changes.get(0).toString());
        assertThat(deltaChanges.getString("action"), is("add_modify"));
        assertThat(RpslObject.parse(deltaChanges.getString("object")).toString(), is(dummifierNrtm.dummify(4, updatedObject).toString()));
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

    private void insertUpdateNotificationFile() {
         databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 1, "TEST");
         databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 2, "TEST-NONAUTH");

        final String versionSql = """
                    INSERT INTO version_info (id, source_id, version, session_id, type, last_serial_id, created)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
        databaseHelper.getNrtmTemplate().update(versionSql, 1,1,1,1,"nrtm-notification",1, JdbcRpslObjectOperations.now(dateTimeProvider));
        databaseHelper.getNrtmTemplate().update(versionSql, 2,2,1,1,"nrtm-notification",1, JdbcRpslObjectOperations.now(dateTimeProvider));

        final String payload = "{\"nrtm_version\":4,\"timestamp\":\"2023-03-13T12:31:08Z\",\"type\":\"snapshot\",\"source\":\"TEST\",\"session_id\":\"4e0c9366-0eb2-42be-bc20-f66d11791d49\",\"version\":1,\"snapshot\":{\"version\":1,\"url\":\"https://nrtm.ripe.net//4e0c9366-0eb2-42be-bc20-f66d11791d49/nrtm-snapshot.1.RIPE.abb5672a6f3f533ce8caf76b0a3fe995.json.gz\",\"hash\":\"95ff848531a610f94fc585bf3ae654925f2faae320e2502343eb5cc43aa5c820\"}}";
        databaseHelper.getNrtmTemplate().update("INSERT INTO notification_file (version_id, created, payload) VALUES (?, ?, ?)", 1, dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC), payload);

        final String payloadNonAuth = "{\"nrtm_version\":4,\"timestamp\":\"2023-03-13T12:31:08Z\",\"type\":\"snapshot\",\"source\":\"TEST-NONAUTH\",\"session_id\":\"7f42be43-a1fd-48e7-947f-91c852cf13f6\",\"version\":1,\"snapshot\":{\"version\":1,\"url\":\"https://nrtm.ripe.net//7f42be43-a1fd-48e7-947f-91c852cf13f6/nrtm-snapshot.1.RIPE-NONAUTH.9a6ca0a3e1b68eb65d4a2d277c3e5a96.json.gz\",\"hash\":\"8140b2d3a3ec4cb6a14c1ca19f7cbca468adcc172ad3e313aed74bd0d1036838\"}}";
        databaseHelper.getNrtmTemplate().update("INSERT INTO notification_file (version_id, created, payload) VALUES (?, ?, ?)",2, dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC), payloadNonAuth);
    }
}
