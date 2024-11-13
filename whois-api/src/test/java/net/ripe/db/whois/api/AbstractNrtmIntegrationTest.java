package net.ripe.db.whois.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.domain.DeltaFileRecord;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.nrtm4.domain.UpdateNotificationFile;
import net.ripe.db.nrtm4.generator.DeltaFileGenerator;
import net.ripe.db.nrtm4.generator.NrtmKeyPairService;
import net.ripe.db.nrtm4.generator.SnapshotFileGenerator;
import net.ripe.db.nrtm4.generator.UpdateNotificationFileGenerator;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class AbstractNrtmIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    protected UpdateNotificationFileGenerator updateNotificationFileGenerator;

    @Autowired
    protected SnapshotFileGenerator snapshotFileGenerator;

    @Autowired
    protected NrtmKeyConfigDao nrtmKeyConfigDao;

    @Autowired
    protected DeltaFileGenerator deltaFileGenerator;

    @Autowired
    protected NrtmKeyPairService nrtmKeyPairService;

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

    protected WebTarget createResource(final String path) {
        return RestTest.target(getPort(), String.format("nrtmv4/%s", path));
    }

    protected UpdateNotificationFile getNotificationFileBySource(final String sourceName) {
        return getResponseFromHttpsRequest(sourceName + "/update-notification-file.json", MediaType.APPLICATION_JSON).readEntity(UpdateNotificationFile.class);
    }

    protected String getSnapshotNameFromUpdateNotification(final UpdateNotificationFile notificationFile) {
        return notificationFile.getSnapshot().getUrl().split("/")[4];
    }

    protected Response getSnapshotFromUpdateNotificationBySource(final String sourceName) throws JsonProcessingException {
        final Response updateNotificationResponse = getResponseFromHttpsRequest(sourceName + "/update-notification-file.json", MediaType.APPLICATION_JSON);
        final UpdateNotificationFile notificationFile = new ObjectMapper().readValue(updateNotificationResponse.readEntity(String.class),
                UpdateNotificationFile.class);
        return getResponseFromHttpsRequest(sourceName + "/" + getSnapshotNameFromUpdateNotification(notificationFile)
                , MediaType.APPLICATION_JSON);
    }

    protected String[] getDeltasFromUpdateNotificationBySource(final String sourceName, final int deltaPosition) {
        final UpdateNotificationFile updateNotificationResponse = getResponseFromHttpsRequest(sourceName +
                "/update-notification-file.json", MediaType.APPLICATION_JSON).readEntity(UpdateNotificationFile.class);

        final String response = getResponseFromHttpsRequest(sourceName + "/" + getDeltaNameFromUpdateNotification(updateNotificationResponse, deltaPosition), "application/json-seq").readEntity(String.class);
        return StringUtils.split( response, NrtmFileUtil.RECORD_SEPERATOR);
    }

    protected String getDeltaNameFromUpdateNotification(final UpdateNotificationFile notificationFile, final int deltaPosition) {
        return notificationFile.getDeltas().get(deltaPosition).getUrl().split("/")[4];
    }

    protected void createNrtmSource() {
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 1, "TEST");
        databaseHelper.getNrtmTemplate().update("INSERT INTO source (id, name) VALUES (?,?)", 2, "TEST-NONAUTH");
    }

    protected void generateDeltas(final List<RpslObject> updatedObject){
        for (final RpslObject rpslObject : updatedObject) {
            databaseHelper.updateObject(rpslObject);
        }
        deltaFileGenerator.createDeltas();
    }

    protected Response getResponseFromHttpsRequest(@Nullable final String path, final String mediaType) {
        return getWebTarget(path).request(mediaType).header(HttpHeader.X_FORWARDED_PROTO.asString(), HttpScheme.HTTPS.asString()).get(Response.class);
    }

    protected Response getResponseFromHttpRequest(@Nullable final String path) {
        return getWebTarget(path).request(MediaType.APPLICATION_JSON).header(HttpHeader.X_FORWARDED_PROTO.asString(), HttpScheme.HTTP.asString()).get(Response.class);
    }

    protected WebTarget getWebTarget(String path) {
        WebTarget webTarget = RestTest.target(getPort(), "nrtmv4/");
        if(path != null){
            webTarget = RestTest.target(getPort(), String.format("nrtmv4/%s", path));
        }
        return webTarget;
    }

    @NotNull
    protected static List<DeltaFileRecord> getDeltaChanges(final String[] records) throws JsonProcessingException {
        final List<DeltaFileRecord> deltaFileRecords = Lists.newArrayList();

        for (int i = 1; i < records.length; i++) {
            deltaFileRecords.add(new ObjectMapper().readValue(records[i].toString(), DeltaFileRecord.class));
        }
        return deltaFileRecords;
    }

    protected static void assertNrtmFileInfo(final String nrtmInfo, final String type, final int version, final String source) throws JSONException {
        final JSONObject jsonObject = new JSONObject(nrtmInfo);
        assertThat(jsonObject.getInt("nrtm_version"), is(4));
        assertThat(jsonObject.getString("type"), is(type));
        assertThat(jsonObject.getString("source"), is(source));
        assertThat(jsonObject.getInt("version"), is(version));
    }

    protected static NrtmVersionRecord getNrtmVersionInfo(final String nrtmInfo) throws JSONException {
        final JSONObject jsonObject = new JSONObject(nrtmInfo);

        return new NrtmVersionRecord(new NrtmSource(1, CIString.ciString(jsonObject.getString("source"))),
                jsonObject.getString("session_id"),
                jsonObject.getLong("version"),
                NrtmDocumentType.getDocumentType(jsonObject.getString("type")));
    }

}
