package net.ripe.db.whois.api.nrtmv4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.api.AbstractNrtmIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

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
public class SnapshotFileGenerationTestIntegration extends AbstractNrtmIntegrationTest {

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
    public void should_get_snapshot_file_test_source() throws IOException, JSONException {

        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
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
        snapshotFileGenerator.createSnapshots();
        updateNotificationFileGenerator.generateFile();

        setTime(LocalDateTime.now().plusDays(1).withHour(23));

        snapshotFileGenerator.createSnapshots();
        updateNotificationFileGenerator.generateFile();

        final Response response = getSnapshotFromUpdateNotificationBySource("TEST");

        final JSONObject testSnapshot = new JSONObject(decompress(response.readEntity(byte[].class)));
        assertThat(testSnapshot.getString("type"), is("snapshot"));
        assertThat(testSnapshot.getInt("version"), is(1));
    }

    @Test
    public void snapshot_should_have_same_session_source_than_update_notification() throws IOException, JSONException {
        snapshotFileGenerator.createSnapshots();
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

    private static void assertNrtmFileInfo(final JSONObject jsonObject, final String type, final int version) throws JSONException {
        assertThat(jsonObject.getInt("nrtm_version"), is(4));
        assertThat(jsonObject.getString("type"), is(type));
        assertThat(jsonObject.getString("source"), is("TEST"));
        assertThat(jsonObject.getInt("version"), is(version));
    }

    private String getSnapshotNameFromUpdateNotification(final PublishableNotificationFile notificationFile) {
        return notificationFile.getSnapshot().getUrl().split("/")[4];
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
