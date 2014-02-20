package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Flag;
import net.ripe.db.whois.api.rest.domain.Flags;
import net.ripe.db.whois.api.rest.domain.InverseAttributes;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.QueryStrings;
import net.ripe.db.whois.api.rest.domain.Source;
import net.ripe.db.whois.api.rest.domain.Sources;
import net.ripe.db.whois.api.rest.domain.TypeFilters;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisTag;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class WhoisRestServiceTestIntegration extends AbstractIntegrationTest {

    private static final String VERSION_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "changed:   noreply@ripe.net 20120101\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");

    private static final RpslObject PASSWORD_ONLY_MNT = RpslObject.parse(
            "mntner:      PASSWORD-ONLY-MNT\n" +
            "descr:       Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:      PASSWORD-ONLY-MNT\n" +
            "referral-by: PASSWORD-ONLY-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");

    private static final RpslObject SSO_ONLY_MNT = RpslObject.parse("" +
            "mntner:         SSO-ONLY-MNT\n" +
            "descr:          Maintainer\n" +
            "admin-c:        TP1-TEST\n" +
            "auth:           SSO person@net.net\n" +
            "mnt-by:         SSO-ONLY-MNT\n" +
            "referral-by:    SSO-ONLY-MNT\n" +
            "upd-to:         noreply@ripe.net\n" +
            "changed:        noreply@ripe.net\n" +
            "source:         TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "changed:   dbtest@ripe.net 20120101\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_ROLE = RpslObject.parse("" +
            "role:      Test Role\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TR1-TEST\n" +
            "admin-c:   TR1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "changed:   dbtest@ripe.net 20120101\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_IRT = RpslObject.parse("" +
            "irt:          irt-test\n" +
            "address:      RIPE NCC\n" +
            "e-mail:       noreply@ripe.net\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "auth:         MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:       OWNER-MNT\n" +
            "changed:      dbtest@ripe.net 20120101\n" +
            "source:       TEST\n");

    @Autowired
    private WhoisObjectServerMapper whoisObjectMapper;
    @Autowired
    private MaintenanceMode maintenanceMode;

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        maintenanceMode.set("FULL,FULL");
    }

    // lookup

    @Test
    public void lookup_downloader_test() throws Exception {
        Path path = Files.createTempFile("downloader_test", "");
        Downloader downloader = new Downloader();
        downloader.downloadTo(LoggerFactory.getLogger("downloader_test"), new URL(String.format("http://localhost:%d/whois/test/mntner/owner-mnt", getPort())), path);
        final String result = new String(Files.readAllBytes(path));
        assertThat(result, containsString("OWNER-MNT"));
        assertThat(result, endsWith("</whois-resources>"));
    }

    @Test
    public void lookup_without_accepts_header() throws Exception {
        final String query = DummyWhoisClient.query(getPort(), "GET /whois/test/mntner/owner-mnt HTTP/1.1\nHost: localhost\nConnection: close\n");

        assertThat(query, containsString("HTTP/1.1 200 OK"));
        assertThat(query, containsString("<whois-resources xmlns"));
    }

    @Test
    public void lookup_with_empty_accepts_header() throws Exception {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/owner-mnt")
                .request()
                .get(WhoisResources.class);

        final RpslObject object = whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));

        assertThat(object, is(RpslObject.parse("" +
                "mntner:         OWNER-MNT\n" +
                "descr:          Owner Maintainer\n" +
                "admin-c:        TP1-TEST\n" +
                "auth:           MD5-PW\n" +
                "auth:           SSO\n" +
                "mnt-by:         OWNER-MNT\n" +
                "referral-by:    OWNER-MNT\n" +
                "source:         TEST")));
    }

    @Test
    public void lookup_inet6num_without_prefix_length() {
        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        try {
            RestTest.target(getPort(), "whois/test/inet6num/2001:2002:2003::").request().get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void lookup_inet6num_with_prefix_length() {
        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inet6num/2001:2002:2003::/48").request().get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("2001:2002:2003::/48"));
    }

    @Test
    public void lookup_person() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_person_unfiltered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST?unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101"),
                new Attribute("source", "TEST", null, null, null)));
    }

    @Test
    public void lookup_xml_text_not_contains_empty_xmlns() {
        final String whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
        assertThat(whoisResources, not(containsString("xmlns=\"\"")));
    }

    @Test
    public void lookup_xml_text_not_contains_root_level_locator() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);
        assertThat(whoisResources.getLink(), nullValue());
    }

    @Test
    public void lookup_inet6num() throws Exception {
        final RpslObject inet6num = RpslObject.parse("" +
                "inet6num: 2001::/48\n" +
                "netname: RIPE-NCC\n" +
                "descr: some description\n" +
                "country: DK\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "status: ASSIGNED\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: org@ripe.net 20120505\n" +
                "source: TEST\n");
        databaseHelper.addObject(inet6num);
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inet6num/2001::/48").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("inet6num", "2001::/48"),
                new Attribute("netname", "RIPE-NCC"),
                new Attribute("descr", "some description"),
                new Attribute("country", "DK"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("status", "ASSIGNED"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    // TODO: add lookup test for route6
    @Test
    public void lookup_route() throws Exception {
        final RpslObject route = RpslObject.parse("" +
                "route:           193.254.30.0/24\n" +
                "descr:           Test route\n" +
                "origin:          AS12726\n" +
                "mnt-by:          OWNER-MNT\n" +
                "changed:         ripe@test.net 20091015\n" +
                "source:          TEST\n");
        databaseHelper.addObject(route);
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/route/193.254.30.0/24AS12726").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink().getHref(), is("http://rest-test.db.ripe.net/test/route/193.254.30.0/24AS12726"));

        final List<Attribute> primaryKey = whoisObject.getPrimaryKey();
        assertThat(primaryKey, hasSize(2));
        assertThat(primaryKey, contains(new Attribute("route", "193.254.30.0/24"),
                new Attribute("origin", "AS12726")));

        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("route", "193.254.30.0/24"),
                new Attribute("descr", "Test route"),
                new Attribute("origin", "AS12726", null, "aut-num", new Link("locator", "http://rest-test.db.ripe.net/test/aut-num/AS12726")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Test
    public void lookup_person_json() throws Exception {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void lookup_correct_object_json() {
        final String whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(whoisResources, not(containsString("errormessages")));
        assertThat(whoisResources, containsString("{\"objects\":{\"object\":[ {\n  \"type\" : \"person\","));
        assertThat(whoisResources, containsString("\"tags\" : { }"));
        assertThat(whoisResources, containsString("" +
                "\"terms-and-conditions\" : {\n" +
                "\"type\" : \"locator\",\n" +
                "\"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                "}"));
    }

    @Test
    public void lookup_role_accept_json() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/role/TR1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("role", "Test Role"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TR1-TEST"),
                new Attribute("admin-c", "TR1-TEST", null, "role", new Link("locator", "http://rest-test.db.ripe.net/test/role/TR1-TEST")),
                new Attribute("abuse-mailbox", "abuse@test.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_person_accept_json() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void lookup_object_json_extension() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/person/TP1-TEST.json")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void lookup_object_not_found() {
        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void lookup_object_wrong_source() {
        try {
            RestTest.target(getPort(), "whois/test-grs/person/TP1-TEST").request().get(String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void lookup_mntner_does_not_have_referenced_type_in_sso() throws Exception {
        databaseHelper.addObject(""+
                "mntner:         MNT-TEST" +"\n" +
                "descr:          test\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           SSO test@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "referral-by:    OWNER-MNT\n" +
                "changed:        asd@as.com\n" +
                "source:         TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/mntner/MNT-TEST?password=test&unfiltered")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        Attribute expected = new Attribute("auth", "SSO test@ripe.net", null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void grs_lookup_object_wrong_source() {
        try {
            RestTest.target(getPort(), "whois/pez/person/PP1-TEST").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Invalid source '%s'", "pez");
        }
    }

    @Test
    public void grs_lookup_found() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-GRS\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test-grs/aut-num/AS102").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey(), contains(new Attribute("aut-num", "AS102")));
        assertThat(whoisResources.getWhoisObjects().get(0).getSource(), is(new Source("test-grs")));
    }

    @Test
    public void lookup_autnum_includes_tags() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        Map<RpslObject, RpslObjectUpdateInfo> updateInfos = databaseHelper.addObjects(Lists.newArrayList(autnum));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "unref", "28");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "foobar", "description");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "other", "other stuff");

        final WhoisResources whoisResources = RestTest.target(getPort(),
                "whois/TEST/aut-num/AS102")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "description"),
                new WhoisTag("other", "other stuff"),
                new WhoisTag("unref", "28")));
    }

    @Test
    public void lookup_mntner() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "SSO", "Filtered", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_unfiltered_param() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final String unfiltered = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered").request().get(String.class);
        assertThat(unfiltered, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String unfilteredEquals = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=").request().get(String.class);
        assertThat(unfilteredEquals, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String unfilteredEqualsTrue = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=true").request().get(String.class);
        assertThat(unfilteredEqualsTrue, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String unfilteredEqualsFalse = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=false").request().get(String.class);
        assertThat(unfilteredEqualsFalse, not(containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\"")));

        final String withOtherParameters = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=true&pretty=false").request().get(String.class);
        assertThat(withOtherParameters, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String filteredByDefault = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(String.class);
        assertThat(filteredByDefault, not(containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\"")));
    }

    @Test
    public void lookup_mntner_without_password_and_unfiltered_param_is_filtered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "SSO", "Filtered", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101", null, null, null),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_mntner_correct_password_and_unfiltered_param_is_unfiltered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test&unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101", null, null, null),
                new Attribute("source", "TEST", null, null, null)));
    }

    @Test
    public void lookup_mntner_correct_password_without_unfiltered_param_is_filtered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_mntner_incorrect_password_without_unfiltered_param_is_filtered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=incorrect").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the incorrect password
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "SSO", "Filtered", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_mntner_multiple_passwords_and_unfiltered_param_is_unfiltered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=incorrect&password=test&unfiltered").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the incorrect password
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101", null, null, null),
                new Attribute("source", "TEST", null, null, null)));
    }

    @Test
    public void lookup_mntner_multiple_auth_attributes_and_unfiltered_param_is_unfiltered() {
        databaseHelper.addObject(
                "mntner:      AUTH-MNT\n" +
                "descr:       Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        MD5-PW $1$5XCg9Q1W$O7g9bgeJPkpea2CkBGnz/0 #test1\n" +
                "auth:        MD5-PW $1$ZjlXZmWO$VKyuYp146Vx5b1.398zgH/ #test2\n" +
                "mnt-by:      AUTH-MNT\n" +
                "referral-by: AUTH-MNT\n" +
                "changed:     dbtest@ripe.net 20120101\n" +
                "source:      TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/AUTH-MNT?password=incorrect&password=test&unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "AUTH-MNT"),
                new Attribute("descr", "Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null),
                new Attribute("auth", "MD5-PW $1$5XCg9Q1W$O7g9bgeJPkpea2CkBGnz/0", "test1", null, null),
                new Attribute("auth", "MD5-PW $1$ZjlXZmWO$VKyuYp146Vx5b1.398zgH/", "test2", null, null),
                new Attribute("mnt-by", "AUTH-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/AUTH-MNT")),
                new Attribute("referral-by", "AUTH-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/AUTH-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101", null, null, null),
                new Attribute("source", "TEST", null, null, null)));
    }

    @Test
    public void lookup_mntner_with_valid_crowd_token_without_unfiltered_param_is_filtered() {
        final WhoisResources whoisResources =
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "valid-token")
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "SSO", "Filtered", null, null),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_mntner_with_valid_crowd_token_and_unfiltered_param_is_unfiltered() {
        final WhoisResources whoisResources =
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?unfiltered")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "valid-token")
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null),
                new Attribute("auth", "SSO person@net.net"),
                new Attribute("source", "TEST")));
    }

    @Test
    public void lookup_mntner_with_inactive_crowd_token_without_unfiltered_param_is_filtered() {
        final WhoisResources whoisResources =
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "inactive-correctuser-token")
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "SSO", "Filtered", null, null),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Ignore("TODO: [ES] inactive sso token can be used for authentication")
    @Test
    public void lookup_mntner_with_inactive_crowd_token_and_unfiltered_param_is_filtered() {
        final WhoisResources whoisResources =
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?unfiltered")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "inactive-correctuser-token")
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "SSO", "Filtered", null, null),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

//    @Ignore("TODO")
//    @Test
//    public void lookup_mntner_authenticate_using_sso_credential_before_password() {
//        RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test")
//                .request(MediaType.APPLICATION_XML)
//                .cookie("crowd.token_key", "valid-token")
//                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(OWNER_MNT)), MediaType.APPLICATION_XML))        // TODO: update, not lookup
//                .readEntity(String.class);
//        //TODO assert that log shows: [AuthenticationModule] authenticating OWNER-MNT with credential SsoCredentialValidator
//    }

    @Test
    public void lookup_irt_correct_password_and_unfiltered_param_is_unfiltered() {
        databaseHelper.addObject(TEST_IRT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/irt/irt-test?password=test&unfiltered").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the incorrect password
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("irt", "irt-test"),
                new Attribute("address", "RIPE NCC"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101", null, null, null),
                new Attribute("source", "TEST")));
    }

    @Test
    public void lookup_irt_incorrect_password_and_unfiltered_param_is_filtered() {
        databaseHelper.addObject(TEST_IRT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/irt/irt-test?password=incorrect").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the incorrect password
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("irt", "irt-test"),
                new Attribute("address", "RIPE NCC"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_mntner_xml_text() {
        final String result = RestTest.target(getPort(), "whois/test/mntner/owner-mnt.xml")
                .request()
                .get(String.class);

        // TODO: [AH] ending </whois-resources> should be on newline
        assertThat(result, is(
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "  <objects>\n" +
                "    <object type=\"mntner\">\n" +
                "      <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\" />\n" +
                "      <source id=\"test\" />\n" +
                "      <primary-key>\n" +
                "        <attribute name=\"mntner\" value=\"OWNER-MNT\" />\n" +
                "      </primary-key>\n" +
                "      <attributes>\n" +
                "        <attribute name=\"mntner\" value=\"OWNER-MNT\" />\n" +
                "        <attribute name=\"descr\" value=\"Owner Maintainer\" />\n" +
                "        <attribute name=\"admin-c\" value=\"TP1-TEST\" referenced-type=\"person\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"auth\" value=\"MD5-PW\" comment=\"Filtered\" />\n" +
                "        <attribute name=\"auth\" value=\"SSO\" comment=\"Filtered\" />\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"referral-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST\" comment=\"Filtered\" />\n" +
                "      </attributes>\n" +
                "      <tags />\n" +
                "    </object>\n" +
                "  </objects>\n" +
                "  <terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\" />\n" +
                "</whois-resources>"));
    }

    @Test
    public void lookup_mntner_json_text() {
        final String result = RestTest.target(getPort(), "whois/test/mntner/owner-mnt.json")
                .request()
                .get(String.class);

        assertThat(result, is(
                "{\"objects\":{\"object\":[ {\n" +
                        "  \"type\" : \"mntner\",\n" +
                        "  \"link\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "  },\n" +
                        "  \"source\" : {\n" +
                        "    \"id\" : \"test\"\n" +
                        "  },\n" +
                        "  \"primary-key\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"mntner\",\n" +
                        "      \"value\" : \"OWNER-MNT\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"attributes\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"mntner\",\n" +
                        "      \"value\" : \"OWNER-MNT\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"descr\",\n" +
                        "      \"value\" : \"Owner Maintainer\"\n" +
                        "    }, {\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                        "      },\n" +
                        "      \"name\" : \"admin-c\",\n" +
                        "      \"value\" : \"TP1-TEST\",\n" +
                        "      \"referenced-type\" : \"person\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"auth\",\n" +
                        "      \"value\" : \"MD5-PW\",\n" +
                        "      \"comment\" : \"Filtered\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"auth\",\n" +
                        "      \"value\" : \"SSO\",\n" +
                        "      \"comment\" : \"Filtered\"\n" +
                        "    }, {\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "      },\n" +
                        "      \"name\" : \"mnt-by\",\n" +
                        "      \"value\" : \"OWNER-MNT\",\n" +
                        "      \"referenced-type\" : \"mntner\"\n" +
                        "    }, {\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "      },\n" +
                        "      \"name\" : \"referral-by\",\n" +
                        "      \"value\" : \"OWNER-MNT\",\n" +
                        "      \"referenced-type\" : \"mntner\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"source\",\n" +
                        "      \"value\" : \"TEST\",\n" +
                        "      \"comment\" : \"Filtered\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"tags\" : { }\n" +
                        "} ]\n" +
                        "},\n" +
                        "\"terms-and-conditions\" : {\n" +
                        "\"type\" : \"locator\",\n" +
                        "\"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "}\n" +
                        "}"
        ));
    }

    @Test
    public void lookup_person_json_text() {
        final String result = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        assertThat(result, is(
                "{\"objects\":{\"object\":[ {\n" +
                        "  \"type\" : \"person\",\n" +
                        "  \"link\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                        "  },\n" +
                        "  \"source\" : {\n" +
                        "    \"id\" : \"test\"\n" +
                        "  },\n" +
                        "  \"primary-key\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"nic-hdl\",\n" +
                        "      \"value\" : \"TP1-TEST\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"attributes\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"person\",\n" +
                        "      \"value\" : \"Test Person\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"address\",\n" +
                        "      \"value\" : \"Singel 258\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"phone\",\n" +
                        "      \"value\" : \"+31 6 12345678\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"nic-hdl\",\n" +
                        "      \"value\" : \"TP1-TEST\"\n" +
                        "    }, {\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "      },\n" +
                        "      \"name\" : \"mnt-by\",\n" +
                        "      \"value\" : \"OWNER-MNT\",\n" +
                        "      \"referenced-type\" : \"mntner\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"source\",\n" +
                        "      \"value\" : \"TEST\",\n" +
                        "      \"comment\" : \"Filtered\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"tags\" : { }\n" +
                        "} ]\n" +
                        "},\n" +
                        "\"terms-and-conditions\" : {\n" +
                        "\"type\" : \"locator\",\n" +
                        "\"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "}\n" +
                        "}"));
    }

    @Test
    public void grs_lookup_autnum_json_text() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-GRS\n");

        final String result = RestTest.target(getPort(), "whois/test-grs/aut-num/AS102.json").request().get(String.class);

        assertThat(result, is(
                "{\"objects\":{\"object\":[ {\n" +
                        "  \"type\" : \"aut-num\",\n" +
                        "  \"link\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://rest-test.db.ripe.net/test-grs/aut-num/AS102\"\n" +
                        "  },\n" +
                        "  \"source\" : {\n" +
                        "    \"id\" : \"test-grs\"\n" +
                        "  },\n" +
                        "  \"primary-key\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"aut-num\",\n" +
                        "      \"value\" : \"AS102\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"attributes\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"aut-num\",\n" +
                        "      \"value\" : \"AS102\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"as-name\",\n" +
                        "      \"value\" : \"End-User-2\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"descr\",\n" +
                        "      \"value\" : \"description\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"admin-c\",\n" +
                        "      \"value\" : \"DUMY-RIPE\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"tech-c\",\n" +
                        "      \"value\" : \"DUMY-RIPE\"\n" +
                        "    }, {\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test-grs/mntner/OWNER-MNT\"\n" +
                        "      },\n" +
                        "      \"name\" : \"mnt-by\",\n" +
                        "      \"value\" : \"OWNER-MNT\",\n" +
                        "      \"referenced-type\" : \"mntner\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"source\",\n" +
                        "      \"value\" : \"TEST-GRS\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"****************************\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"* THIS OBJECT IS MODIFIED\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"* Please note that all data that is generally regarded as personal\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"* data has been removed from this object.\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"* To view the original object, please query the RIPE Database at:\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"* http://www.ripe.net/whois\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"****************************\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"tags\" : { }\n" +
                        "} ]\n" +
                        "},\n" +
                        "\"terms-and-conditions\" : {\n" +
                        "\"type\" : \"locator\",\n" +
                        "\"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "}\n" +
                        "}"
        ));
    }

    @Test
    public void grs_lookup_autnum_xml_text() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-GRS\n");

        final String result = RestTest.target(getPort(), "whois/test-grs/aut-num/AS102.xml").request().get(String.class);

        assertThat(result, is(
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "  <objects>\n" +
                "    <object type=\"aut-num\">\n" +
                "      <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test-grs/aut-num/AS102\" />\n" +
                "      <source id=\"test-grs\" />\n" +
                "      <primary-key>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\" />\n" +
                "      </primary-key>\n" +
                "      <attributes>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\" />\n" +
                "        <attribute name=\"as-name\" value=\"End-User-2\" />\n" +
                "        <attribute name=\"descr\" value=\"description\" />\n" +
                "        <attribute name=\"admin-c\" value=\"DUMY-RIPE\" />\n" +
                "        <attribute name=\"tech-c\" value=\"DUMY-RIPE\" />\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test-grs/mntner/OWNER-MNT\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST-GRS\" />\n" +
                "        <attribute name=\"remarks\" value=\"****************************\" />\n" +
                "        <attribute name=\"remarks\" value=\"* THIS OBJECT IS MODIFIED\" />\n" +
                "        <attribute name=\"remarks\" value=\"* Please note that all data that is generally regarded as personal\" />\n" +
                "        <attribute name=\"remarks\" value=\"* data has been removed from this object.\" />\n" +
                "        <attribute name=\"remarks\" value=\"* To view the original object, please query the RIPE Database at:\" />\n" +
                "        <attribute name=\"remarks\" value=\"* http://www.ripe.net/whois\" />\n" +
                "        <attribute name=\"remarks\" value=\"****************************\" />\n" +
                "      </attributes>\n" +
                "      <tags />\n" +
                "    </object>\n" +
                "  </objects>\n" +
                "  <terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\" />\n" +
                "</whois-resources>"));
    }

    @Test
    public void lookup_xml_response_doesnt_contain_invalid_values() {
        databaseHelper.addObject("" +
                "mntner:      TEST-MNT\n" +
                "descr:       escape invalid values like \uDC00Brat\u001b$B!l\u001b <b> <!-- &#x0;\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:      TEST-MNT\n" +
                "referral-by: TEST-MNT\n" +
                "changed:     dbtest@ripe.net 20120101\n" +
                "source:      TEST");

        final String response = RestTest.target(getPort(), "whois/test/mntner/TEST-MNT")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, not(containsString("\u001b")));
        assertThat(response, not(containsString("<b>")));
        assertThat(response, not(containsString("&#x0;")));
        assertThat(response, not(containsString("<!--")));
    }

    // create

    @Test
    public void create_succeeds() throws Exception {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("http://localhost:%s/test/person?password=test", getPort())));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);

        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("remarks", "remark"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void create_invalid_source_in_request_body() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31-1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "changed: noreply@ripe.net 20120101\n" +
                "remarks: remark\n" +
                "source:  NONE\n");
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(rpslObject)), MediaType.APPLICATION_XML), String.class);
            fail("expected request to fail");
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Unrecognized source: %s", "NONE");
        }
    }

    @Test
    public void create_invalid_reference() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity("<whois-resources>\n" +
                            "    <objects>\n" +
                            "        <object type=\"person\">\n" +
                            "            <source id=\"RIPE\"/>\n" +
                            "            <attributes>\n" +
                            "                <attribute name=\"person\" value=\"Pauleth Palthen\"/>\n" +
                            "                <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                            "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                            "                <attribute name=\"e-mail\" value=\"noreply@ripe.net\"/>\n" +
                            "                <attribute name=\"admin-c\" value=\"INVALID\"/>\n" +
                            "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                            "                <attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>\n" +
                            "                <attribute name=\"changed\" value=\"ppalse@ripe.net 20101228\"/>\n" +
                            "                <attribute name=\"source\" value=\"RIPE\"/>\n" +
                            "            </attributes>\n" +
                            "        </object>\n" +
                            "    </objects>\n" +
                            "</whois-resources>", MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = mapClientException(e);
            assertErrorCount(whoisResources, 2);
            assertErrorMessage(whoisResources, 0, "Error", "Unrecognized source: %s", "RIPE");
            assertErrorMessage(whoisResources, 1, "Error", "\"%s\" is not valid for this object type", "admin-c");
        }
    }

    @Test(expected = BadRequestException.class)
    public void create_bad_input_empty_objects_element() {
        RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity("<whois-resources>\n<objects/>\n</whois-resources>", MediaType.APPLICATION_XML), String.class);
    }

    @Test(expected = BadRequestException.class)
    public void create_bad_input_no_objects_element() {
        RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity("<whois-resources/>", MediaType.APPLICATION_XML), String.class);
    }

    @Test(expected = BadRequestException.class)
    public void create_bad_input_empty_body() {
        RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_XML), String.class);
    }

    @Test
    public void create_multiple_passwords() {
        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=invalid&password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void create_invalid_password() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=invalid")
                    .request()
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void create_no_password() {
        try {
            RestTest.target(getPort(), "whois/test/person")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void create_already_exists() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(OWNER_MNT)), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e1) {
            assertThat(e1.getResponse().getStatus(), is(Response.Status.CONFLICT.getStatusCode()));
            assertOnlyErrorMessage(e1, "Error", "Enforced new keyword specified, but the object already exists in the database");
        }
    }

    @Test
    public void create_delete_method_not_allowed() {
        try {
            RestTest.target(getPort(), "whois/test/person")
                    .request()
                    .delete(String.class);
            fail();
        } catch (NotAllowedException e) {
            // expected
        }
    }

    @Test
    public void create_get_resource_not_found() {
        try {
            RestTest.target(getPort(), "whois/test")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }

    @Test
    public void create_person_xml_text() {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_JSON), String.class);

        assertThat(response, is(String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                "<link xlink:type=\"locator\" xlink:href=\"http://localhost:%s/test/person?password=test\"/>" +
                "<objects>" +
                "<object type=\"person\">" +
                "<link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/PP1-TEST\"/>" +
                "<source id=\"test\"/>" +
                "<primary-key>" +
                "<attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>" +
                "</primary-key>" +
                "<attributes>" +
                "<attribute name=\"person\" value=\"Pauleth Palthen\"/>" +
                "<attribute name=\"address\" value=\"Singel 258\"/>" +
                "<attribute name=\"phone\" value=\"+31-1234567890\"/>" +
                "<attribute name=\"e-mail\" value=\"noreply@ripe.net\"/>" +
                "<attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">" +
                "<link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"/>" +
                "</attribute>" +
                "<attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>" +
                "<attribute name=\"changed\" value=\"noreply@ripe.net 20120101\"/>" +
                "<attribute name=\"remarks\" value=\"remark\"/>" +
                "<attribute name=\"source\" value=\"TEST\"/>" +
                "</attributes>" +
                "<tags/>" +
                "</object>" +
                "</objects>" +
                "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>" +
                "</whois-resources>",getPort())));
    }

    @Test
    public void create_person_json_text() {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_JSON), String.class);

        assertThat(response, not(containsString("\"errormessages\"")));
        assertThat(response, is(String.format("" +
                        "{\n" +
                        "  \"link\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://localhost:%s/test/person?password=test\"\n" +
                        "  },\n" +
                        "  \"objects\" : {\n" +
                        "    \"object\" : [ {\n" +
                        "      \"type\" : \"person\",\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/person/PP1-TEST\"\n" +
                        "      },\n" +
                        "      \"source\" : {\n" +
                        "        \"id\" : \"test\"\n" +
                        "      },\n" +
                        "      \"primary-key\" : {\n" +
                        "        \"attribute\" : [ {\n" +
                        "          \"name\" : \"nic-hdl\",\n" +
                        "          \"value\" : \"PP1-TEST\"\n" +
                        "        } ]\n" +
                        "      },\n" +
                        "      \"attributes\" : {\n" +
                        "        \"attribute\" : [ {\n" +
                        "          \"name\" : \"person\",\n" +
                        "          \"value\" : \"Pauleth Palthen\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"address\",\n" +
                        "          \"value\" : \"Singel 258\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"phone\",\n" +
                        "          \"value\" : \"+31-1234567890\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"e-mail\",\n" +
                        "          \"value\" : \"noreply@ripe.net\"\n" +
                        "        }, {\n" +
                        "          \"link\" : {\n" +
                        "            \"type\" : \"locator\",\n" +
                        "            \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "          },\n" +
                        "          \"name\" : \"mnt-by\",\n" +
                        "          \"value\" : \"OWNER-MNT\",\n" +
                        "          \"referenced-type\" : \"mntner\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"nic-hdl\",\n" +
                        "          \"value\" : \"PP1-TEST\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"changed\",\n" +
                        "          \"value\" : \"noreply@ripe.net 20120101\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"remarks\",\n" +
                        "          \"value\" : \"remark\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"source\",\n" +
                        "          \"value\" : \"TEST\"\n" +
                        "        } ]\n" +
                        "      },\n" +
                        "      \"tags\" : {\n" +
                        "        \"tag\" : [ ]\n" +
                        "      }\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"terms-and-conditions\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "  }\n" +
                        "}", getPort())));
    }

    @Test
    public void create_utf8_character_encoding() {
        final RpslObject person = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   test \u03A3 and \u00DF characters\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "changed:   noreply@ripe.net 20120101\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(person)), MediaType.APPLICATION_XML), WhoisResources.class);

        // UTF-8 characters are mapped to latin1. Characters outside the latin1 charset are substituted by '?'
        final WhoisObject responseObject = whoisResources.getWhoisObjects().get(0);
        assertThat(responseObject.getAttributes().get(1).getValue(), is("test ? and \u00DF characters"));
    }

    @Test
    public void create_self_referencing_maintainer_password_auth_only() {

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner?password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PASSWORD_ONLY_MNT)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getType(), is("mntner"));
        assertThat(object.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/mntner/PASSWORD-ONLY-MNT")));
        assertThat(object.getPrimaryKey(), contains(new Attribute("mntner", "PASSWORD-ONLY-MNT")));
    }

    @Test
    public void create_self_referencing_maintainer_sso_auth_only() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(SSO_ONLY_MNT)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));
        assertThat(databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-ONLY-MNT").findAttributes(AttributeType.AUTH),
                containsInAnyOrder(
                        new RpslAttribute(AttributeType.AUTH, "SSO 906635c2-0405-429a-800b-0602bd716124")));
    }

    @Test
    public void create_self_referencing_maintainer_sso_auth_only_invalid_username() throws Exception {
        try {
            final RpslObject updatedObject = new RpslObjectBuilder(SSO_ONLY_MNT)
                    .replaceAttribute(
                            new RpslAttribute(AttributeType.AUTH, "SSO person@net.net"),
                            new RpslAttribute(AttributeType.AUTH, "SSO in@valid.net")).sort().get();

            RestTest.target(getPort(), "whois/test/mntner")
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = mapClientException(e);
            assertErrorCount(whoisResources, 1);
            assertErrorMessage(whoisResources, 0, "Error", "No RIPE NCC Access Account found for %s", "in@valid.net");
        }
    }

    @Test
    public void create_self_referencing_maintainer_sso_auth_only_invalid_token() {
        try {
            RestTest.target(getPort(), "whois/test/mntner")
                .request()
                .cookie("crowd.token_key", "invalid")
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(SSO_ONLY_MNT)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = mapClientException(e);
            assertErrorCount(whoisResources, 2);
            assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "mntner", "SSO-ONLY-MNT", "mnt-by", "SSO-ONLY-MNT");
            assertErrorMessage(whoisResources, 1, "Info", "RIPE NCC Access token ignored");
        }
    }

    @Test
    public void create_self_referencing_maintainer_password_auth_only_with_invalid_sso_username() {
        final RpslObject updatedObject = new RpslObjectBuilder(PASSWORD_ONLY_MNT).addAttribute(new RpslAttribute(AttributeType.AUTH, "SSO in@valid.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner?password=test")
                    .request()
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "No RIPE NCC Access Account found for %s", "in@valid.net");
        }
    }

    // delete

    @Test
    public void delete_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test").request().delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_with_reason_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test&reason=not_needed_no_more").request().delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException expected) {}
    }

    @Test
    public void delete_person_with_crowd_token_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Ignore("TODO: bad request on delete")
    @Test
    public void delete_self_referencing_maintainer_with_crowd_token_succeeds() throws Exception {
        databaseHelper.addObject(SSO_ONLY_MNT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/SSO-ONLY-MNT")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));

        try {
            databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-ONLY-MNT");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_nonexistant() {
        try {
            RestTest.target(getPort(), "whois/test/person/NON-EXISTANT").request().delete(String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void delete_referenced_from_other_objects() {
        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test").request().delete(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Object [%s] %s is referenced from other objects", "person", "TP1-TEST");
        }
    }

    @Test
    public void delete_invalid_password() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=invalid").request().delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void delete_no_password() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    // update

    @Test
    public void update_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("remarks", "updated"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void update_noop() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertErrorCount(whoisResources, 1);
        assertErrorMessage(whoisResources, 0, "Warning", "Submitted object identical to database object");

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void update_noop_with_override() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?override=agoston,zoh,reason")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertErrorCount(whoisResources, 2);
        assertErrorMessage(whoisResources, 0, "Warning", "Submitted object identical to database object");
        assertErrorMessage(whoisResources, 1, "Info", "Authorisation override used");

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void update_spaces_in_password_succeeds() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:      OWNER2-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$NitG3QQZnA4z6zp1o.qmm/ # ' spaces '\n" +
                "mnt-by:      OWNER2-MNT\n" +
                "referral-by: OWNER2-MNT\n" +
                "changed:     dbtest@ripe.net 20120101\n" +
                "source:      TEST"));

        final String response = RestTest.target(getPort(), "whois/test/mntner/OWNER2-MNT?password=%20spaces%20")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(
                        "<whois-resources>\n" +
                        "    <objects>\n" +
                        "        <object type=\"mntner\">\n" +
                        "            <source id=\"TEST\"/>\n" +
                        "            <attributes>\n" +
                        "                <attribute name=\"mntner\" value=\"OWNER2-MNT\"/>\n" +
                        "                <attribute name=\"descr\" value=\"Owner Maintainer\"/>\n" +
                        "                <attribute name=\"admin-c\" value=\"TP1-TEST\"/>\n" +
                        "                <attribute name=\"upd-to\" value=\"noreply@ripe.net\"/>\n" +
                        "                <attribute name=\"auth\" value=\"MD5-PW $1$d9fKeTr2$NitG3QQZnA4z6zp1o.qmm/\"/>\n" +
                        "                <attribute name=\"remarks\" value=\"updated\"/>\n" +
                        "                <attribute name=\"mnt-by\" value=\"OWNER2-MNT\"/>\n" +
                        "                <attribute name=\"referral-by\" value=\"OWNER2-MNT\"/>\n" +
                        "                <attribute name=\"changed\" value=\"dbtest@ripe.net 20120102\"/>\n" +
                        "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                        "            </attributes>\n" +
                        "        </object>\n" +
                        "    </objects>\n" +
                        "</whois-resources>", MediaType.APPLICATION_XML), String.class);

        assertThat(response, containsString("<attribute name=\"remarks\" value=\"updated\"/>"));
        assertThat(response, not(containsString("errormessages")));
    }

    @Test
    public void update_json_request_and_response_content() {
        final String update = "" +
                "{\n" +
                "  \"objects\": {\n" +
                "    \"object\": [\n" +
                "      {\n" +
                "        \"source\": {\n" +
                "          \"id\": \"test\"\n" +
                "        },\n" +
                "        \"attributes\": {\n" +
                "          \"attribute\": [\n" +
                "            {\n" +
                "              \"name\": \"mntner\",\n" +
                "              \"value\": \"OWNER-MNT\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"descr\",\n" +
                "              \"value\": \"description\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"admin-c\",\n" +
                "              \"value\": \"TP1-TEST\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"upd-to\",\n" +
                "              \"value\": \"noreply@ripe.net\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"auth\",\n" +
                "              \"value\": \"MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n\\/cqk\\/\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"mnt-by\",\n" +
                "              \"value\": \"OWNER-MNT\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"referral-by\",\n" +
                "              \"value\": \"OWNER-MNT\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"changed\",\n" +
                "              \"value\": \"dbtest@ripe.net 20120101\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"source\",\n" +
                "              \"value\": \"TEST\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        final String response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(update, MediaType.APPLICATION_JSON), String.class);

        assertThat(response, is(String.format(
                "{\n" +
                        "  \"link\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://localhost:%s/test/mntner/OWNER-MNT?password=test\"\n" +
                        "  },\n" +
                        "  \"objects\" : {\n" +
                        "    \"object\" : [ {\n" +
                        "      \"type\" : \"mntner\",\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "      },\n" +
                        "      \"source\" : {\n" +
                        "        \"id\" : \"test\"\n" +
                        "      },\n" +
                        "      \"primary-key\" : {\n" +
                        "        \"attribute\" : [ {\n" +
                        "          \"name\" : \"mntner\",\n" +
                        "          \"value\" : \"OWNER-MNT\"\n" +
                        "        } ]\n" +
                        "      },\n" +
                        "      \"attributes\" : {\n" +
                        "        \"attribute\" : [ {\n" +
                        "          \"name\" : \"mntner\",\n" +
                        "          \"value\" : \"OWNER-MNT\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"descr\",\n" +
                        "          \"value\" : \"description\"\n" +
                        "        }, {\n" +
                        "          \"link\" : {\n" +
                        "            \"type\" : \"locator\",\n" +
                        "            \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                        "          },\n" +
                        "          \"name\" : \"admin-c\",\n" +
                        "          \"value\" : \"TP1-TEST\",\n" +
                        "          \"referenced-type\" : \"person\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"upd-to\",\n" +
                        "          \"value\" : \"noreply@ripe.net\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"auth\",\n" +
                        "          \"value\" : \"MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/\"\n" +
                        "        }, {\n" +
                        "          \"link\" : {\n" +
                        "            \"type\" : \"locator\",\n" +
                        "            \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "          },\n" +
                        "          \"name\" : \"mnt-by\",\n" +
                        "          \"value\" : \"OWNER-MNT\",\n" +
                        "          \"referenced-type\" : \"mntner\"\n" +
                        "        }, {\n" +
                        "          \"link\" : {\n" +
                        "            \"type\" : \"locator\",\n" +
                        "            \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "          },\n" +
                        "          \"name\" : \"referral-by\",\n" +
                        "          \"value\" : \"OWNER-MNT\",\n" +
                        "          \"referenced-type\" : \"mntner\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"changed\",\n" +
                        "          \"value\" : \"dbtest@ripe.net 20120101\"\n" +
                        "        }, {\n" +
                        "          \"name\" : \"source\",\n" +
                        "          \"value\" : \"TEST\"\n" +
                        "        } ]\n" +
                        "      },\n" +
                        "      \"tags\" : {\n" +
                        "        \"tag\" : [ ]\n" +
                        "      }\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"terms-and-conditions\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "  }\n" +
                        "}", getPort())));
    }

    @Test
    public void update_path_vs_object_mismatch_objecttype() throws Exception {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/mntner/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Object type and key specified in URI (%s: %s) do not match the WhoisResources contents", "mntner", "PP1-TEST");
        }
    }

    @Test
    public void update_path_vs_object_mismatch_key() throws Exception {
        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Object type and key specified in URI (%s: %s) do not match the WhoisResources contents", "mntner", "OWNER-MNT");
        }
    }

    @Test
    public void update_without_query_params() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void update_post_not_allowed() {
        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAllowedException ignored) {
            // expected
        }
    }

    @Test
    public void update_missing_mandatory_fields() {
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).removeAttributeType(AttributeType.CHANGED).get();

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Mandatory attribute \"%s\" is missing", "changed");
        }
    }

    @Test
    public void update_person_with_crowd_token_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "valid-token")
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("remarks", "updated")));
    }

    @Test
    public void update_maintainer_with_crowd_token_succeeds() {
        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).addAttribute(new RpslAttribute(AttributeType.REMARKS, "updated")).get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "valid-token")
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("remarks", "updated")));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null)));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));
        assertThat(databaseHelper.lookupObject(ObjectType.MNTNER, "OWNER-MNT").findAttributes(AttributeType.AUTH),
                containsInAnyOrder(
                        new RpslAttribute(AttributeType.AUTH, "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test"),
                        new RpslAttribute(AttributeType.AUTH, "SSO 906635c2-0405-429a-800b-0602bd716124")));
    }

    @Test
    public void update_maintainer_with_invalid_sso_username_fails() {
        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).replaceAttribute(
                new RpslAttribute(AttributeType.AUTH, "SSO person@net.net"),
                new RpslAttribute(AttributeType.AUTH, "SSO in@valid.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "valid-token")
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "No RIPE NCC Access Account found for %s", "in@valid.net");
        }
    }

    @Test
    public void update_person_with_invalid_crowd_token_fails() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "invalid-token")
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = mapClientException(e);
            assertErrorCount(whoisResources, 2);
            assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
            assertErrorMessage(whoisResources, 1, "Info", "RIPE NCC Access token ignored");
        }
    }

    @Test
    public void update_person_with_incorrect_crowd_token_user_fails() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "inactive-incorrectuser-token")
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = mapClientException(e);
            assertErrorCount(whoisResources, 1);
            assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void update_comment_is_noop_and_returns_old_object() {
        assertThat(TEST_PERSON.findAttributes(AttributeType.REMARKS), hasSize(0));
        final RpslObjectBuilder builder = new RpslObjectBuilder(TEST_PERSON);
        final RpslAttribute remarks = new RpslAttribute(AttributeType.REMARKS, "updated # comment");
        builder.addAttribute(remarks);

        RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(builder.sort().get())), MediaType.APPLICATION_XML), WhoisResources.class);

        builder.replaceAttribute(remarks, new RpslAttribute(AttributeType.REMARKS, "updated # new comment"));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(builder.sort().get())), MediaType.APPLICATION_XML), WhoisResources.class);

        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), hasItem(new Attribute("remarks", "updated", "comment", null, null)));
    }

    @Test
    public void update_with_override_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));

        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?override=agoston,zoh,reason")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);

        assertErrorCount(whoisResources, 1);
        assertErrorMessage(whoisResources, 0, "Info", "Authorisation override used");

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("remarks", "updated"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    // versions

    @Test
    public void versions_returns_xml() throws IOException {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisVersions whoisVersions = whoisResources.getVersions();
        assertThat(whoisVersions.getType(), is("aut-num"));
        assertThat(whoisVersions.getKey(), is("AS102"));
        assertThat(whoisVersions.getVersions(), hasSize(1));
        final WhoisVersion whoisVersion = whoisVersions.getVersions().get(0);
        assertThat(whoisVersion, is(new WhoisVersion("ADD/UPD", whoisVersion.getDate(), 1)));
    }

    @Test
    public void versions_deleted() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);
        databaseHelper.updateObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(3));
        assertThat(versions.get(0).getDeletedDate(), is(not(nullValue())));
        assertThat(versions.get(0).getOperation(), is(nullValue()));
        assertThat(versions.get(0).getDate(), is(nullValue()));
        assertThat(versions.get(0).getRevision(), is(nullValue()));

        assertThat(versions.get(1).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(1).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(1).getRevision(), is(1));
        assertThat(versions.get(1).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));

        assertThat(versions.get(2).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(2).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(2).getRevision(), is(2));
        assertThat(versions.get(2).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
    }

    @Test
    public void versions_deleted_versions_json() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);
        databaseHelper.addObject(autnum);
        databaseHelper.updateObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(3));
        assertThat(versions.get(0).getDeletedDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
        assertThat(versions.get(0).getOperation(), is(nullValue()));
        assertThat(versions.get(0).getDate(), is(nullValue()));
        assertThat(versions.get(0).getRevision(), is(nullValue()));

        assertThat(versions.get(1).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(1).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(1).getRevision(), is(1));
        assertThat(versions.get(1).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));

        assertThat(versions.get(2).getDeletedDate(), is(nullValue()));
        assertThat(versions.get(2).getOperation(), is("ADD/UPD"));
        assertThat(versions.get(2).getRevision(), is(2));
        assertThat(versions.get(2).getDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
    }

    @Test
    public void versions_last_version_deleted() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final List<WhoisVersion> versions = whoisResources.getVersions().getVersions();
        assertThat(versions, hasSize(1));
        assertThat(versions.get(0).getDeletedDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
        assertThat(versions.get(0).getOperation(), is(nullValue()));
        assertThat(versions.get(0).getDate(), is(nullValue()));
        assertThat(versions.get(0).getRevision(), is(nullValue()));
    }

    @Test
    public void versions_no_versions_found() throws IOException {
        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102/versions")
                    .request(MediaType.APPLICATION_XML)
                    .get(String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void version_nonexistant_version() throws IOException {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/2")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void version_wrong_object_type() throws IOException {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");

        try {
            RestTest.target(getPort(), "whois/test/inetnum/AS102/versions/1")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void version_returns_xml() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));
        final List<Attribute> attributes = object.getAttributes();
        final List<RpslAttribute> originalAttributes = autnum.getAttributes();
        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i).getCleanValue().toString(), is(attributes.get(i).getValue()));
        }
    }

    @Test
    public void version_returns_json() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));

        final List<Attribute> attributes = object.getAttributes();
        final List<RpslAttribute> originalAttributes = autnum.getAttributes();
        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i).getCleanValue().toString(), is(attributes.get(i).getValue()));
        }
    }

    @Test
    public void version_not_showing_deleted_version() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(autnum);

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    // response format

    @Test
    public void lookup_accept_application_xml() {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, containsString("<?xml version='1.0' encoding='UTF-8'?>"));
        assertThat(response, containsString("<whois-resources"));
    }

    @Test
    public void lookup_accept_application_json() {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"type\""));
        assertThat(response, containsString("\"href\""));
    }

    @Test
    public void lookup_json_extension() throws Exception {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST.json")
                .request()
                .get(String.class);
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"type\""));
        assertThat(response, containsString("\"href\""));
    }

    @Test
    public void lookup_unfiltered_queryparameter() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final String response = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=").request().get(String.class);
        assertThat(response, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String noEqualSign = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered").request().get(String.class);
        assertThat(noEqualSign, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String withOtherParameters = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=true&pretty=false").request().get(String.class);
        assertThat(withOtherParameters, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String filtered = RestTest.target(getPort(), "whois/test/person/PP1-TEST?pretty=false").request().get(String.class);
        assertThat(filtered, not(containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\"")));
    }

    // search

    @Test
    public void search() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));
        assertThat(whoisResources.getService().getName(), is(WhoisRestService.SERVICE_SEARCH));

        final WhoisObject autnum = whoisResources.getWhoisObjects().get(0);
        assertThat(autnum.getType(), is("aut-num"));
        assertThat(autnum.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(autnum.getPrimaryKey().get(0).getValue(), is("AS102"));

        assertThat(autnum.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));

        final WhoisObject person = whoisResources.getWhoisObjects().get(1);
        assertThat(person.getType(), is("person"));
        assertThat(person.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
        assertThat(person.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")));

        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void search_accept_json() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void search_json_extension() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search.json?query-string=OWNER-MNT&source=TEST")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        final WhoisObject whoisObject0 = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject0.getPrimaryKey().get(0).getValue(), is("OWNER-MNT"));

        final WhoisObject whoisObject1 = whoisResources.getWhoisObjects().get(1);
        assertThat(whoisObject1.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));

        final String result = RestTest.target(getPort(), "whois/search?query-string=OWNER-MNT&source=TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(result, containsString("" +
                "\"parameters\" : {\n" +
                "  \"inverse-lookup\" : { },\n" +
                "  \"type-filters\" : { },\n" +
                "  \"flags\" : { },\n" +
                "  \"query-strings\" : {\n" +
                "    \"query-string\" : [ {\n" +
                "      \"value\" : \"OWNER-MNT\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"sources\" : {\n" +
                "    \"source\" : [ {\n" +
                "      \"id\" : \"TEST\"\n" +
                "    } ]\n" +
                "  }\n" +
                "},"));
    }

    @Test
    public void search_with_long_options() {
        databaseHelper.addObject("" +
                "person:    Lo Person\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "nic-hdl:   LP1-TEST\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=LP1-TEST&source=TEST&flags=no-filtering&flags=rB")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final List<Flag> flags = whoisResources.getParameters().getFlags().getFlags();
        assertThat(flags, hasSize(2));
        assertThat(flags.get(0).getValue(), is("no-referenced"));
        assertThat(flags.get(1).getValue(), is("no-filtering"));
    }

    @Test
    public void search_with_short_and_long_options_together() {
        databaseHelper.addObject("" +
                "person:    Lo Person\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "nic-hdl:   LP1-TEST\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n");

        try {
            RestTest.target(getPort(), "whois/search?query-string=LP1-TEST&source=TEST&flags=show-tag-inforG")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Invalid search flag '%s' (in parameter '%s')", "h", "show-tag-inforG");
        }
    }

    @Test
    public void search_space_with_dash_invalid_flag() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20%2D10.1.1.1&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Flags are not allowed in 'query-string'");
        }
    }

    @Test
    public void search_invalid_flag() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=LP1-TEST&source=TEST&flags=q")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Disallowed search flag '%s'", "q");
        }
    }

    @Test
    public void search_tags_in_response() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        Map<RpslObject, RpslObjectUpdateInfo> updateInfos = databaseHelper.addObjects(Lists.newArrayList(autnum));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "unref", "28");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "foobar", "description");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "other", "other stuff");

        final WhoisResources whoisResources = RestTest.target(getPort(),
                "whois/TEST/aut-num/AS102?include-tag=foobar&include-tag=unref")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "description"),
                new WhoisTag("other", "other stuff"),
                new WhoisTag("unref", "28")));
    }

    @Test
    public void search_tags_in_json_response() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        Map<RpslObject, RpslObjectUpdateInfo> updateInfos = databaseHelper.addObjects(Lists.newArrayList(autnum));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "unref", "28");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "foobar", "description");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "other", "other stuff");

        final String result = RestTest.target(getPort(),
                "whois/TEST/aut-num/AS102?include-tag=foobar&include-tag=unref")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(result, containsString("" +
                "\"tags\" : {\n" +
                "    \"tag\" : [ {\n" +
                "      \"id\" : \"foobar\",\n" +
                "      \"data\" : \"description\"\n" +
                "    }, {\n" +
                "      \"id\" : \"other\",\n" +
                "      \"data\" : \"other stuff\"\n" +
                "    }, {\n" +
                "      \"id\" : \"unref\",\n" +
                "      \"data\" : \"28\"\n" +
                "    } ]\n" +
                "  }"));

        final WhoisResources whoisResources = RestTest.target(getPort(),
                "whois/TEST/aut-num/AS102?include-tag=foobar&include-tag=unref")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "description"),
                new WhoisTag("other", "other stuff"),
                new WhoisTag("unref", "28")));
    }

    @Test
    public void search_include_tag_param() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        Map<RpslObject, RpslObjectUpdateInfo> updateInfos = databaseHelper.addObjects(Lists.newArrayList(autnum));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "unref", "28");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "foobar", "description");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "other", "other stuff");

        final WhoisResources whoisResources = RestTest.target(getPort(),
                "whois/search?source=TEST&query-string=AS102&include-tag=foobar&include-tag=unref")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "description"),
                new WhoisTag("other", "other stuff"),
                new WhoisTag("unref", "28")));
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void search_include_tag_param_no_results() {
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n"));

        try {
            RestTest.target(getPort(),
                    "whois/search?source=TEST&query-string=AS102&include-tag=foobar")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void search_include_and_exclude_tags_params_no_results() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        Map<RpslObject, RpslObjectUpdateInfo> updateInfos = databaseHelper.addObjects(Lists.newArrayList(autnum));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "unref", "28");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "foobar", "foobar");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "other", "other stuff");

        try {
            RestTest.target(getPort(),
                    "whois/search?source=TEST&query-string=AS102&exclude-tag=foobar&include-tag=unref&include-tag=other")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void search_include_and_exclude_tags_params() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        Map<RpslObject, RpslObjectUpdateInfo> updateInfos = databaseHelper.addObjects(Lists.newArrayList(autnum));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "unref", "28");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", updateInfos.get(autnum).getObjectId(), "foobar", "foobar");

        final WhoisResources whoisResources = RestTest.target(getPort(),
                "whois/search?source=TEST&query-string=AS102&exclude-tag=other&include-tag=unref&include-tag=foobar")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "foobar"),
                new WhoisTag("unref", "28")));
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void search_no_sources_given() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void search_no_querystring_given() {
        try {
            RestTest.target(getPort(), "whois/search?source=TEST")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException ignored) {
            assertOnlyErrorMessage(ignored, "Error", "Query param 'query-string' cannot be empty");
        }
    }

    @Test
    public void search_invalid_source() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=AS102&source=INVALID")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Invalid source '%s'", "INVALID");
            assertThat(e.getResponse().getHeaders().get("Content-Type"), contains((Object) "application/xml"));
        }
    }

    @Test
    public void search_multiple_sources() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&source=RIPE")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Invalid source '%s'", "RIPE");
        }
    }

    @Test
    public void search_with_type_filter() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST&type-filter=aut-num,as-block")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        WhoisObject aut_num = whoisResources.getWhoisObjects().get(0);
        WhoisObject person = whoisResources.getWhoisObjects().get(1);

        assertThat(person.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(aut_num.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(aut_num.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void search_with_type_filter_json() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String str = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST&type-filter=aut-num&type-filter=as-block")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(str, containsString("" +
                "\"type-filters\" : {\n" +
                "    \"type-filter\" : [ {\n" +
                "      \"id\" : \"aut-num\"\n" +
                "    }, {\n" +
                "      \"id\" : \"as-block\"\n" +
                "    } ]\n" +
                "  },"));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST&type-filter=aut-num&type-filter=as-block")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertThat(whoisResources.getParameters().getTypeFilters().getTypeFilters(), hasSize(2));
    }

    @Test
    public void search_inverse() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&inverse-attribute=admin-c,tech-c")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(4));
        WhoisObject aut_num = whoisResources.getWhoisObjects().get(0);
        assertThat(aut_num.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(aut_num.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));

        WhoisObject person = whoisResources.getWhoisObjects().get(1);
        assertThat(person.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
        WhoisObject mntner = whoisResources.getWhoisObjects().get(2);
        assertThat(mntner.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")));
        assertThat(mntner.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "SSO", "Filtered", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));

        WhoisObject person2 = whoisResources.getWhoisObjects().get(3);
        assertThat(person2.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(person2.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Ignore("TODO: error on parsing query is not handled -> response is plaintext error, not xml")
    @Test
    public void search_invalid_query_flags() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=denis+walker&flags=resource")
                    .request(MediaType.APPLICATION_XML)
                    .get(String.class);
        } catch (BadRequestException e) {
            final WhoisResources response = mapClientException(e);

            assertThat(response.getErrorMessages(), hasSize(1));
        }
    }

    @Test
    public void search_inverse_json() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String result = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&inverse-attribute=admin-c,tech-c")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(result, containsString("" +
                "\"inverse-lookup\" : {\n" +
                "    \"inverse-attribute\" : [ {\n" +
                "      \"value\" : \"admin-c,tech-c\"\n" +
                "    } ]\n" +
                "  },"));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&inverse-attribute=admin-c,tech-c")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        final List<WhoisObject> whoisObjects = whoisResources.getWhoisObjects();
        assertThat(whoisObjects, hasSize(4));
    }

    @Test
    public void search_flags() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&flags=BrCx")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void search_flags_json() {
        final String str = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&flags=BrCx")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertThat(str, containsString("" +
                "\"flags\" : {\n" +
                "    \"flag\" : [ {\n" +
                "      \"value\" : \"no-filtering\"\n" +
                "    }, {\n" +
                "      \"value\" : \"no-referenced\"\n" +
                "    }, {\n" +
                "      \"value\" : \"no-irt\"\n" +
                "    }, {\n" +
                "      \"value\" : \"exact\"\n" +
                "    } ]\n" +
                "  },"));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&flags=BrCx")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertThat(whoisResources.getParameters().getFlags().getFlags(), hasSize(4));
    }

    @Test
    public void search_hierarchical_flags() {
        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=2001:2002:2003:2004::5&flags=Lr")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        whoisResources = RestTest.target(getPort(), "whois/search?query-string=2001:2002::/32&flags=M&flags=r")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void search_invalid_flags() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST&flags=kq")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertOnlyErrorMessage(e, "Error", "Disallowed search flag '%s'", "persistent-connection");
        }
    }

    @Test
    public void search_grs() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-GRS\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST-GRS")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        WhoisObject aut_num = whoisResources.getWhoisObjects().get(0);
        assertThat(aut_num.getLink(), is(new Link("locator", "http://rest-test.db.ripe.net/test-grs/aut-num/AS102")));
        assertThat(aut_num.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "DUMY-RIPE"),
                new Attribute("tech-c", "DUMY-RIPE"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test-grs/mntner/OWNER-MNT")),
                new Attribute("source", "TEST-GRS"),
                new Attribute("remarks", "****************************"),
                new Attribute("remarks", "* THIS OBJECT IS MODIFIED"),
                new Attribute("remarks", "* Please note that all data that is generally regarded as personal"),
                new Attribute("remarks", "* data has been removed from this object."),
                new Attribute("remarks", "* To view the original object, please query the RIPE Database at:"),
                new Attribute("remarks", "* http://www.ripe.net/whois"),
                new Attribute("remarks", "****************************")
        ));
    }

    @Test
    public void search_parameters_are_returned() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "" +
                "whois/search?inverse-attribute=person" +
                "&type-filter=aut-num" +
                "&source=test" +
                "&flags=rB" +
                "&query-string=TP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));

        final Parameters parameters = whoisResources.getParameters();
        final Flags flags = parameters.getFlags();
        assertThat(flags.getFlags().get(0).getValue(), is("no-referenced"));
        assertThat(flags.getFlags().get(1).getValue(), is("no-filtering"));
        final InverseAttributes inverseAttributes = parameters.getInverseLookup();
        assertThat(inverseAttributes.getInverseAttributes().get(0).getValue(), is("person"));
        final TypeFilters typeFilters = parameters.getTypeFilters();
        assertThat(typeFilters.getTypeFilters().get(0).getId(), is("aut-num"));
        final Sources sources = parameters.getSources();
        assertThat(sources.getSources().get(0).getId(), is("test"));
        final QueryStrings queryStrings = parameters.getQueryStrings();
        assertThat(queryStrings.getQueryStrings().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void search_not_found() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=NONEXISTANT&source=TEST")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            assertThat(e.getResponse().readEntity(String.class), not(containsString("Caused by:")));
        }
    }

    @Test
    public void search_streaming_puts_xlink_into_root_element_and_nowhere_else() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String whoisResources = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(whoisResources, containsString("<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">"));
        assertThat(whoisResources, containsString("<object type=\"aut-num\">"));
        assertThat(whoisResources, containsString("<objects>"));
    }

    @Test
    public void search_non_streaming_puts_xlink_into_root_element_and_nowhere_else() throws Exception {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);

        final String whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102/versions/1")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(whoisResources, containsString("<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">"));
        assertThat(whoisResources, containsString("<object type=\"aut-num\" version=\"1\">"));
        assertThat(whoisResources, containsString("<objects>"));
    }

    @Test
    public void search_multiple_objects_json_format() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String response = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(response, is(
                "{\"service\" : {\n" +
                        "  \"name\" : \"search\"\n" +
                        "},\n" +
                        "\"parameters\" : {\n" +
                        "  \"inverse-lookup\" : { },\n" +
                        "  \"type-filters\" : { },\n" +
                        "  \"flags\" : { },\n" +
                        "  \"query-strings\" : {\n" +
                        "    \"query-string\" : [ {\n" +
                        "      \"value\" : \"AS102\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"sources\" : {\n" +
                        "    \"source\" : [ {\n" +
                        "      \"id\" : \"TEST\"\n" +
                        "    } ]\n" +
                        "  }\n" +
                        "},\n" +
                        "\"objects\" : {\n" +
                        "  \"object\" : [ {\n" +
                        "    \"type\" : \"aut-num\",\n" +
                        "    \"link\" : {\n" +
                        "      \"type\" : \"locator\",\n" +
                        "      \"href\" : \"http://rest-test.db.ripe.net/test/aut-num/AS102\"\n" +
                        "    },\n" +
                        "    \"source\" : {\n" +
                        "      \"id\" : \"test\"\n" +
                        "    },\n" +
                        "    \"primary-key\" : {\n" +
                        "      \"attribute\" : [ {\n" +
                        "        \"name\" : \"aut-num\",\n" +
                        "        \"value\" : \"AS102\"\n" +
                        "      } ]\n" +
                        "    },\n" +
                        "    \"attributes\" : {\n" +
                        "      \"attribute\" : [ {\n" +
                        "        \"name\" : \"aut-num\",\n" +
                        "        \"value\" : \"AS102\"\n" +
                        "      }, {\n" +
                        "        \"name\" : \"as-name\",\n" +
                        "        \"value\" : \"End-User-2\"\n" +
                        "      }, {\n" +
                        "        \"name\" : \"descr\",\n" +
                        "        \"value\" : \"description\"\n" +
                        "      }, {\n" +
                        "        \"link\" : {\n" +
                        "          \"type\" : \"locator\",\n" +
                        "          \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                        "        },\n" +
                        "        \"name\" : \"admin-c\",\n" +
                        "        \"value\" : \"TP1-TEST\",\n" +
                        "        \"referenced-type\" : \"person\"\n" +
                        "      }, {\n" +
                        "        \"link\" : {\n" +
                        "          \"type\" : \"locator\",\n" +
                        "          \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                        "        },\n" +
                        "        \"name\" : \"tech-c\",\n" +
                        "        \"value\" : \"TP1-TEST\",\n" +
                        "        \"referenced-type\" : \"person\"\n" +
                        "      }, {\n" +
                        "        \"link\" : {\n" +
                        "          \"type\" : \"locator\",\n" +
                        "          \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "        },\n" +
                        "        \"name\" : \"mnt-by\",\n" +
                        "        \"value\" : \"OWNER-MNT\",\n" +
                        "        \"referenced-type\" : \"mntner\"\n" +
                        "      }, {\n" +
                        "        \"name\" : \"source\",\n" +
                        "        \"value\" : \"TEST\"\n" +
                        "      } ]\n" +
                        "    },\n" +
                        "    \"tags\" : { }\n" +
                        "  }, {\n" +
                        "    \"type\" : \"person\",\n" +
                        "    \"link\" : {\n" +
                        "      \"type\" : \"locator\",\n" +
                        "      \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                        "    },\n" +
                        "    \"source\" : {\n" +
                        "      \"id\" : \"test\"\n" +
                        "    },\n" +
                        "    \"primary-key\" : {\n" +
                        "      \"attribute\" : [ {\n" +
                        "        \"name\" : \"nic-hdl\",\n" +
                        "        \"value\" : \"TP1-TEST\"\n" +
                        "      } ]\n" +
                        "    },\n" +
                        "    \"attributes\" : {\n" +
                        "      \"attribute\" : [ {\n" +
                        "        \"name\" : \"person\",\n" +
                        "        \"value\" : \"Test Person\"\n" +
                        "      }, {\n" +
                        "        \"name\" : \"address\",\n" +
                        "        \"value\" : \"Singel 258\"\n" +
                        "      }, {\n" +
                        "        \"name\" : \"phone\",\n" +
                        "        \"value\" : \"+31 6 12345678\"\n" +
                        "      }, {\n" +
                        "        \"name\" : \"nic-hdl\",\n" +
                        "        \"value\" : \"TP1-TEST\"\n" +
                        "      }, {\n" +
                        "        \"link\" : {\n" +
                        "          \"type\" : \"locator\",\n" +
                        "          \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "        },\n" +
                        "        \"name\" : \"mnt-by\",\n" +
                        "        \"value\" : \"OWNER-MNT\",\n" +
                        "        \"referenced-type\" : \"mntner\"\n" +
                        "      }, {\n" +
                        "        \"name\" : \"source\",\n" +
                        "        \"value\" : \"TEST\",\n" +
                        "        \"comment\" : \"Filtered\"\n" +
                        "      } ]\n" +
                        "    },\n" +
                        "    \"tags\" : { }\n" +
                        "  } ]\n" +
                        "},\n" +
                        "\"terms-and-conditions\" : {\n" +
                        "  \"type\" : \"locator\",\n" +
                        "  \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "}\n" +
                        "}"));
    }

    @Test
    public void search_multiple_objects_xml_format() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String response = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, is(
                "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "  <service name=\"search\" />\n" +
                "  <parameters>\n" +
                "    <inverse-lookup />\n" +
                "    <type-filters />\n" +
                "    <flags />\n" +
                "    <query-strings>\n" +
                "      <query-string value=\"AS102\" />\n" +
                "    </query-strings>\n" +
                "    <sources>\n" +
                "      <source id=\"TEST\" />\n" +
                "    </sources>\n" +
                "  </parameters>\n" +
                "  <objects>\n" +
                "    <object type=\"aut-num\">\n" +
                "      <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/aut-num/AS102\" />\n" +
                "      <source id=\"test\" />\n" +
                "      <primary-key>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\" />\n" +
                "      </primary-key>\n" +
                "      <attributes>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\" />\n" +
                "        <attribute name=\"as-name\" value=\"End-User-2\" />\n" +
                "        <attribute name=\"descr\" value=\"description\" />\n" +
                "        <attribute name=\"admin-c\" value=\"TP1-TEST\" referenced-type=\"person\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"tech-c\" value=\"TP1-TEST\" referenced-type=\"person\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST\" />\n" +
                "      </attributes>\n" +
                "      <tags />\n" +
                "    </object>\n" +
                "    <object type=\"person\">\n" +
                "      <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\" />\n" +
                "      <source id=\"test\" />\n" +
                "      <primary-key>\n" +
                "        <attribute name=\"nic-hdl\" value=\"TP1-TEST\" />\n" +
                "      </primary-key>\n" +
                "      <attributes>\n" +
                "        <attribute name=\"person\" value=\"Test Person\" />\n" +
                "        <attribute name=\"address\" value=\"Singel 258\" />\n" +
                "        <attribute name=\"phone\" value=\"+31 6 12345678\" />\n" +
                "        <attribute name=\"nic-hdl\" value=\"TP1-TEST\" />\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\" />\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST\" comment=\"Filtered\" />\n" +
                "      </attributes>\n" +
                "      <tags />\n" +
                "    </object>\n" +
                "  </objects>\n" +
                "  <terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\" />\n" +
                "</whois-resources>"));
    }

    @Test
    public void search_not_contains_empty_xmlns() {
        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final String whoisResources = RestTest.target(getPort(), "whois/search?query-string=2001:2002:2003:2004::5")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(whoisResources, not(containsString("xmlns=\"\"")));
    }

    @Test
    public void search_sso_auth_filtered() {
        databaseHelper.addObject("" +
                "mntner: TEST-MNT\n" +
                "mnt-by:TEST-MNT\n" +
                "auth: SSO test@ripe.net\n" +
                "source: TEST");

        final String response = RestTest.target(getPort(), "whois/search?query-string=TEST-MNT&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, containsString("<attribute name=\"auth\" value=\"SSO\" comment=\"Filtered\" />"));
    }

    // response format

    @Test
    public void lookup_xsi_attributes_not_in_root_level_link() {
        final String whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST")
                .request(MediaType.APPLICATION_XML_TYPE).get(String.class);
        assertThat(whoisResources, not(containsString("xsi:type")));
        assertThat(whoisResources, not(containsString("xmlns:xsi")));
    }

    @Test
    public void non_ascii_characters_are_preserved() {
        assertThat(RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("" +
                        "{ \"objects\": {\n" +
                        "   \"object\": [ {\n" +
                        "    \"source\": { \"id\": \"RIPE\" }, \n" +
                        "    \"attributes\": {\n" +
                        "       \"attribute\": [\n" +
                        "        { \"name\": \"person\", \"value\": \"Pauleth Palthen\" },\n" +
                        "        { \"name\": \"address\", \"value\": \"Flughafenstraße 109/a\" },\n" +
                        "        { \"name\": \"phone\", \"value\": \"+31-2-1234567\" },\n" +
                        "        { \"name\": \"e-mail\", \"value\": \"noreply@ripe.net\" },\n" +
                        "        { \"name\": \"mnt-by\", \"value\": \"OWNER-MNT\" },\n" +
                        "        { \"name\": \"nic-hdl\", \"value\": \"PP1-TEST\" },\n" +
                        "        { \"name\": \"changed\", \"value\": \"noreply@ripe.net\" },\n" +
                        "        { \"name\": \"remarks\", \"value\": \"created\" },\n" +
                        "        { \"name\": \"source\", \"value\": \"TEST\" }\n" +
                        "        ] }\n" +
                        "    }] \n" +
                        "}}", MediaType.APPLICATION_JSON), String.class), containsString("Flughafenstraße 109/a"));

        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class), containsString("Flughafenstraße 109/a"));

        assertThat(RestTest.target(getPort(), "whois/search?query-string=PP1-TEST&source=TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class), containsString("Flughafenstraße 109/a"));

        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity("" +
                        "{ \"objects\": {\n" +
                        "   \"object\": [ {\n" +
                        "    \"source\": { \"id\": \"RIPE\" }, \n" +
                        "    \"attributes\": {\n" +
                        "       \"attribute\": [\n" +
                        "        { \"name\": \"person\", \"value\": \"Pauleth Palthen\" },\n" +
                        "        { \"name\": \"address\", \"value\": \"Flughafenstraße 109/a\" },\n" +
                        "        { \"name\": \"phone\", \"value\": \"+31-2-1234567\" },\n" +
                        "        { \"name\": \"e-mail\", \"value\": \"noreply@ripe.net\" },\n" +
                        "        { \"name\": \"mnt-by\", \"value\": \"OWNER-MNT\" },\n" +
                        "        { \"name\": \"nic-hdl\", \"value\": \"PP1-TEST\" },\n" +
                        "        { \"name\": \"changed\", \"value\": \"noreply@ripe.net\" },\n" +
                        "        { \"name\": \"remarks\", \"value\": \"created\" },\n" +
                        "        { \"name\": \"source\", \"value\": \"TEST\" }\n" +
                        "        ] }\n" +
                        "    }] \n" +
                        "}}", MediaType.APPLICATION_JSON), String.class), containsString("Flughafenstraße 109/a"));
    }

    @Test
    public void error_message_not_included_in_successful_lookup() throws Exception {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), String.class);

        assertThat(response, not(containsString("errormessages")));
    }

    @Test
    public void error_message_not_included_in_successful_search() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String response = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, not(containsString("errormessage")));
    }

    // maintenance mode

    // TODO: [AH] also test origin, i.e. maintenanceMode.set("NONE,READONLY")

    @Test(expected = ServiceUnavailableException.class)
    public void maintenance_mode_readonly_update() {
        maintenanceMode.set("READONLY,READONLY");
        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), String.class);
    }

    @Test
    public void maintenance_mode_readonly_query() {
        maintenanceMode.set("READONLY,READONLY");
        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test(expected = ServiceUnavailableException.class)
    public void maintenance_mode_none_update() {
        maintenanceMode.set("NONE,NONE");
        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), String.class);
    }

    @Test(expected = ServiceUnavailableException.class)
    public void maintenance_mode_none_query() {
        maintenanceMode.set("NONE,NONE");
        RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);
    }

    // helper methods

    private static WhoisResources mapClientException(final ClientErrorException e) {
        return e.getResponse().readEntity(WhoisResources.class);
    }

    static void assertOnlyErrorMessage(final ClientErrorException e, final String severity, final String text, final String... argument) {
        WhoisResources whoisResources = mapClientException(e);
        assertErrorCount(whoisResources, 1);
        assertErrorMessage(whoisResources, 0, severity, text, argument);
    }

    static void assertErrorMessage(final WhoisResources whoisResources, final int number, final String severity, final String text, final String... argument) {
        assertEquals(text, whoisResources.getErrorMessages().get(number).getText());
        assertThat(whoisResources.getErrorMessages().get(number).getSeverity(), is(severity));
        if (argument.length > 0) {
            assertThat(whoisResources.getErrorMessages().get(number).getArgs(), hasSize(argument.length));
            for (int i = 0; i < argument.length; i++) {
                assertThat(whoisResources.getErrorMessages().get(number).getArgs().get(i).getValue(), is(argument[i]));
            }
        }
    }

    static void assertErrorCount(final WhoisResources whoisResources, final int count) {
        assertThat(whoisResources.getErrorMessages(), hasSize(count));
    }
}
