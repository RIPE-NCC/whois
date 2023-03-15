package net.ripe.db.whois.api.nrtmv4;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.AbstractNrtm4IntegrationBase;
import net.ripe.db.nrtm4.NrtmFileProcessor;
import net.ripe.db.nrtm4.SnapshotFileGenerator;
import net.ripe.db.nrtm4.dao.DeltaFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.api.rdap.AbstractRdapIntegrationTest;
import net.ripe.db.whois.api.rdap.domain.Action;
import net.ripe.db.whois.api.rdap.domain.Autnum;
import net.ripe.db.whois.api.rdap.domain.Domain;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Event;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.Nameserver;
import net.ripe.db.whois.api.rdap.domain.Notice;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.Remark;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.api.rdap.domain.SearchResult;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.support.TestWhoisLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static net.ripe.db.whois.common.support.DateMatcher.isBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
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
    DeltaFileRepository deltaFileRepository;

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

        final Response response = createResource("snapshot/" + fileOptional.get().name())
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .get(Response.class);

        assertThat(response.getStatus(), is(200));

        JSONObject jsonObject = new JSONObject(decompress(response.readEntity(byte[].class)));
        assertThat(jsonObject.getInt("nrtm_version"), is(4));
        assertThat(jsonObject.getString("type"), is("snapshot"));
        assertThat(jsonObject.getString("source"), is("TEST"));
        assertThat(jsonObject.getInt("version"), is(1));

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

        final List<DeltaFile> deltaFile = deltaFileRepository.getDeltasForNotification(snapshotVersion, 0);

        final Response response = createResource("delta/" + deltaFile.get(0).name())
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertThat(response.getStatus(), is(200));

        JSONObject jsonObject = new JSONObject(new String(response.readEntity(byte[].class)));
        assertThat(jsonObject.getInt("nrtm_version"), is(4));
        assertThat(jsonObject.getString("type"), is("delta"));
        assertThat(jsonObject.getString("source"), is("TEST"));
        assertThat(jsonObject.getInt("version"), is(2));

        final JSONArray objects = jsonObject.getJSONArray("changes");
        assertThat(objects.length(), is(1));

        JSONObject deltaChanges = new JSONObject(objects.get(0).toString());
        assertThat(deltaChanges.getString("action"), is("add_modify"));
        assertThat(RpslObject.parse(deltaChanges.getString("object")).toString(), is(dummifierNrtm.dummify(4, updatedObject).toString()));
    }

    @Test
    public void should_throw_exception_invalid_snapshot_filename()  {
        final Response response = createResource("snapshot/invalidfilename")
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .get(Response.class);
        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void should_throw_exception_snapshot_file_not_found()  {
        final Response response = createResource("snapshot/nrtm-snapshot.1.TEST-NONAUTH.4ef060b9e8c4e4891411be.json.gz")
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .get(Response.class);
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void should_throw_exception_invalid_delta_filename()  {
        final Response response = createResource("delta/invalidfilename")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void should_throw_exception_delta_file_not_found()  {
        final Response response = createResource("delta/nrtm-delta.1.TEST-NONAUTH.4ef060b9e8c4e4891411be.json")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertThat(response.getStatus(), is(404));
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
}
