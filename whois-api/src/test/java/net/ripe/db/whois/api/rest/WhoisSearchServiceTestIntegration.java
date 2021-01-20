package net.ripe.db.whois.api.rest;

import com.google.common.base.Strings;
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
import net.ripe.db.whois.api.rest.domain.Sources;
import net.ripe.db.whois.api.rest.domain.TypeFilters;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisTag;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class WhoisSearchServiceTestIntegration extends AbstractIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";

    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST\n");

    private static final RpslObject RIPE_NCC_HM_MNT = RpslObject.parse("" +
            "mntner:      RIPE-NCC-HM-MNT\n" +
            "descr:       Hostmaster\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$tnG/zrDw$nps8tg76q4jgg5zg5o6os. # hm\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      RIPE-NCC-HM-MNT\n" +
            "source:      TEST\n");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_ROLE = RpslObject.parse("" +
            "role:      Test Role\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TR1-TEST\n" +
            "admin-c:   TR1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_OTHER_ORGANISATION = RpslObject.parse("" +
            "organisation:   ORG-TO1-TEST\n" +
            "org-name:       Test Organisation\n" +
            "status:         OTHER\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n");

    private static final RpslObject TEST_LIR_ORGANISATION = RpslObject.parse("" +
            "organisation:   ORG-TO2-TEST\n" +
            "org-name:       Test Organisation\n" +
            "abuse-c:        TR1-TEST\n" +
            "status:         LIR\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n");

    @Autowired
    private MaintenanceMode maintenanceMode;
    @Autowired
    private TestDateTimeProvider testDateTimeProvider;
    @Autowired
    private ApplicationVersion applicationVersion;

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        maintenanceMode.set("FULL,FULL");
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @After
    public void reset() {
        databaseHelper.getAclTemplate().update("DELETE FROM acl_denied");
        databaseHelper.getAclTemplate().update("DELETE FROM acl_event");
        databaseHelper.getAclTemplate().update("DELETE FROM acl_proxy");
        testPersonalObjectAccounting.resetAccounting();
        ipResourceConfiguration.reload();
    }

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
        assertThat(whoisResources.getService().getName(), is("search"));

        final WhoisObject autnum = whoisResources.getWhoisObjects().get(0);
        assertThat(autnum.getType(), is("aut-num"));
        assertThat(autnum.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(autnum.getPrimaryKey().get(0).getValue(), is("AS102"));

        assertThat(autnum.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));

        final WhoisObject person = whoisResources.getWhoisObjects().get(1);
        assertThat(person.getType(), is("person"));
        assertThat(person.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
        assertThat(person.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST")));

        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
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
        assertThat(flags, containsInAnyOrder(new Flag(QueryFlag.NO_REFERENCED), new Flag(QueryFlag.NO_FILTERING)));
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
            RestTest.assertOnlyErrorMessage(e, "Error", "Invalid search flag '%s' (in parameter '%s')", "h", "show-tag-inforG");
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
            RestTest.assertOnlyErrorMessage(e, "Error", "Flags are not allowed in 'query-string'");
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
            RestTest.assertOnlyErrorMessage(e, "Error", "Disallowed search flag '%s'", "q");
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
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test(expected = NotFoundException.class)
    public void search_include_tag_param_no_results() {
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n"));

        RestTest.target(getPort(),
                "whois/search?source=TEST&query-string=AS102&include-tag=foobar")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
    }

    @Test(expected = NotFoundException.class)
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

        RestTest.target(getPort(),
                "whois/search?source=TEST&query-string=AS102&exclude-tag=foobar&include-tag=unref&include-tag=other")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
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
        assertThat(whoisObject.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "foobar"),
                new WhoisTag("unref", "28")));
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
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
            RestTest.assertOnlyErrorMessage(ignored, "Error", "Query param 'query-string' cannot be empty");
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
            RestTest.assertOnlyErrorMessage(e, "Error", "Invalid source '%s'", "INVALID");
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
            RestTest.assertOnlyErrorMessage(e, "Error", "Invalid source '%s'", "RIPE");
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

        assertThat(person.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(aut_num.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(aut_num.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
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
                "      \"id\" : \"as-block\"\n" +
                "    }, {\n" +
                "      \"id\" : \"aut-num\"\n" +
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
        assertThat(aut_num.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/aut-num/AS102")));
        assertThat(aut_num.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));

        WhoisObject person = whoisResources.getWhoisObjects().get(1);
        assertThat(person.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
        WhoisObject mntner = whoisResources.getWhoisObjects().get(2);
        assertThat(mntner.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")));
        assertThat(mntner.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)
        ));

        WhoisObject person2 = whoisResources.getWhoisObjects().get(3);
        assertThat(person2.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(person2.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void search_invalid_query_flags() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=denis+walker&flags=resource")
                    .request(MediaType.APPLICATION_XML)
                    .get(String.class);
        } catch (BadRequestException e) {
            final WhoisResources response = RestTest.mapClientException(e);

            assertThat(response.getErrorMessages(), hasSize(1));
            assertThat(response.getErrorMessages().get(0).getText(), is("ERROR:115: invalid search key\n" +
                    "\n" +
                    "Search key entered is not valid for the specified object type(s)\n"));
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
        assertThat(whoisObject.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST")));
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
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
                        "source:         TEST"
        );
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
            RestTest.assertOnlyErrorMessage(e, "Error", "Disallowed search flag '%s'", "persistent-connection");
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
        assertThat(aut_num.getLink(), is(Link.create("http://rest-test.db.ripe.net/test-grs/aut-num/AS102")));
        assertThat(aut_num.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "DUMY-RIPE"),
                new Attribute("tech-c", "DUMY-RIPE"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test-grs/mntner/OWNER-MNT"), null),
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
            // ensure no stack trace in response
            assertThat(e.getResponse().readEntity(String.class), not(containsString("Caused by:")));
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void search_illegal_character_encoding_in_query_param() throws Exception {
        try (
                final InputStream inputStream = new URL(
                        String.format("http://localhost:%d/whois/search?flags=rB&source=TEST&type-filter=mntner&query-string=AA1-MNT+{+192.168.0.0/16+}",
                                getPort())).openStream()) {
            fail();
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
    public void search_not_found_json_format() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=invalid&source=TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is(String.format(
                    "{\n" +
                            "  \"link\" : {\n" +
                            "    \"type\" : \"locator\",\n" +
                            "    \"href\" : \"http://localhost:%s/search?query-string=invalid&source=TEST\"\n" +
                            "  },\n" +
                            "  \"errormessages\" : {\n" +
                            "    \"errormessage\" : [ {\n" +
                            "      \"severity\" : \"Error\",\n" +
                            "      \"text\" : \"ERROR:101: no entries found\\n\\nNo entries found in source %%s.\\n\",\n" +
                            "      \"args\" : [ {\n" +
                            "        \"value\" : \"TEST\"\n" +
                            "      } ]\n" +
                            "    } ]\n" +
                            "  },\n" +
                            "  \"terms-and-conditions\" : {\n" +
                            "    \"type\" : \"locator\",\n" +
                            "    \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                            "  }\n" +
                            "}", getPort()
            )));
        }
    }

    // TODO: [ES] xml error response is not pretty printed
    @Test
    public void search_not_found_xml_format() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=invalid&source=TEST")
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is(String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                            "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                            "<link xlink:type=\"locator\" xlink:href=\"http://localhost:%s/search?query-string=invalid&amp;source=TEST\"/>" +
                            "<errormessages>" +
                            "<errormessage severity=\"Error\" text=\"ERROR:101: no entries found&#10;&#10;No entries found in source %%s.&#10;\">" +
                            "<args value=\"TEST\"/>" +
                            "</errormessage>" +
                            "</errormessages>" +
                            "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>" +
                            "</whois-resources>", getPort()
            )));
        }
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
                        "    }\n" +
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
                        "        \"value\" : \"TEST\"\n" +
                        "      } ]\n" +
                        "    }\n" +
                        "  } ]\n" +
                        "},\n" +
                        "\"terms-and-conditions\" : {\n" +
                        "  \"type\" : \"locator\",\n" +
                        "  \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "},\n" +
                        "\"version\" : {\n" +
                        "  \"version\" : \"" + applicationVersion.getVersion() + "\",\n" +
                        "  \"timestamp\" : \"" + applicationVersion.getTimestamp() + "\",\n" +
                        "  \"commit-id\" : \"" + applicationVersion.getCommitId() + "\"\n" +
                        "}\n" +
                        "}"
        ));
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

        assertThat(response, is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "<service name=\"search\"/>\n" +
                "<parameters>\n" +
                "    <inverse-lookup/>\n" +
                "    <type-filters/>\n" +
                "    <flags/>\n" +
                "    <query-strings>\n" +
                "        <query-string value=\"AS102\"/>\n" +
                "    </query-strings>\n" +
                "    <sources>\n" +
                "        <source id=\"TEST\"/>\n" +
                "    </sources>\n" +
                "</parameters>\n" +
                "<objects>\n" +
                "<object type=\"aut-num\">\n" +
                "    <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/aut-num/AS102\"/>\n" +
                "    <source id=\"test\"/>\n" +
                "    <primary-key>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\"/>\n" +
                "    </primary-key>\n" +
                "    <attributes>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\"/>\n" +
                "        <attribute name=\"as-name\" value=\"End-User-2\"/>\n" +
                "        <attribute name=\"descr\" value=\"description\"/>\n" +
                "        <attribute name=\"admin-c\" value=\"TP1-TEST\" referenced-type=\"person\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\"/>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"tech-c\" value=\"TP1-TEST\" referenced-type=\"person\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\"/>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"/>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST\"/>\n" +
                "    </attributes>\n" +
                "</object>\n" +
                "<object type=\"person\">\n" +
                "    <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\"/>\n" +
                "    <source id=\"test\"/>\n" +
                "    <primary-key>\n" +
                "        <attribute name=\"nic-hdl\" value=\"TP1-TEST\"/>\n" +
                "    </primary-key>\n" +
                "    <attributes>\n" +
                "        <attribute name=\"person\" value=\"Test Person\"/>\n" +
                "        <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                "        <attribute name=\"phone\" value=\"+31 6 12345678\"/>\n" +
                "        <attribute name=\"nic-hdl\" value=\"TP1-TEST\"/>\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"/>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST\"/>\n" +
                "    </attributes>\n" +
                "</object>\n" +
                "</objects>\n" +
                "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>\n" +
                "<version " +
                "version=\"" + applicationVersion.getVersion() + "\" " +
                "timestamp=\"" + applicationVersion.getTimestamp() + "\" " +
                "commit-id=\"" + applicationVersion.getCommitId() + "\"/>\n" +
                "</whois-resources>\n"));
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
                        "source:         TEST"
        );
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

        assertThat(response, containsString("<attribute name=\"auth\" value=\"SSO\" comment=\"Filtered\"/>"));
    }

    @Test(expected = NotFoundException.class)
    public void search_huge_query_string() {
        RestTest.target(getPort(), String.format("whois/search?query-string=%s&source=TEST", Strings.repeat("X", 5900)))
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
    }

    @Test
    public void search_successful_error_message_not_included() throws Exception {
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

    @Test
    public void search_gzip_compressed_response() throws Exception {
        final Response response = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST")
                .register(EncodingFilter.class)
                .register(GZipEncoder.class)
                .request(MediaType.APPLICATION_XML)
                .get();

        assertThat(response.getHeaderString("Content-Type"), is(MediaType.APPLICATION_XML));
        assertThat(response.getHeaderString("Content-Encoding"), is("gzip"));

        final WhoisResources whoisResources = response.readEntity(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void search_zlib_compressed_response() throws Exception {
        final Response response = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST&source=TEST")
                .register(EncodingFilter.class)
                .register(DeflateEncoder.class)
                .request(MediaType.APPLICATION_XML)
                .get();

        assertThat(response.getHeaderString("Content-Type"), is(MediaType.APPLICATION_XML));
        assertThat(response.getHeaderString("Content-Encoding"), is("deflate"));

        final WhoisResources whoisResources = response.readEntity(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void search_multiple_params_and_spaces() throws Exception {
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.255.255.255\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ASSIGNED PI\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search")
                .queryParam("query-string", "10.0.0.0 - 10.255.255.255")
                .queryParam("filter-types", "inetnum")
                .queryParam("flags", "r", "exact")
                .queryParam("source", "test")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes().get(0).getValue(), is("10.0.0.0 - 10.255.255.255"));
    }

    @Test
    public void search_no_empty_elements_in_xml_response() {
        final String whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(whoisResources, containsString("Test Person"));
        assertThat(whoisResources, not(containsString("<errormessages")));
        assertThat(whoisResources, not(containsString("<versionsInternal")));
        assertThat(whoisResources, not(containsString("<versions")));
    }

    @Test
    public void search_no_empty_elements_in_json_response() {
        final String whoisResources = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(whoisResources, containsString("Test Person"));
        assertThat(whoisResources, not(containsString("errormessages")));
        assertThat(whoisResources, not(containsString("versionsInternal")));
        assertThat(whoisResources, not(containsString("versions")));
    }

    @Test
    public void search_too_many_single_query_arguments() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=" +
                    "d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d" +
                    "%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d" +
                    "%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d%20d" +
                    "%20d%20d%20d%20d")
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "ERROR:118: too many arguments supplied\n\nToo many arguments supplied.\n");
        }
    }

    @Test
    public void search_too_many_multiple_query_arguments() {
        try {
            RestTest.target(getPort(), "whois/search?abuse-contact=true&ignore40=true&managed-attributes=true&" +
                    "resource-holder=true&flags=r&offset=0&limit=20&query-string=126/8%09APNIC%09Jan-05%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20125/8%09APNIC%09Jan-05%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20124/8%09APNIC%09Jan-05%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20123/8%09APNIC%09Jan-06%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20122/8%09APNIC%09Jan-06%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20121/8%09APNIC%09Jan-06%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20120/8%09APNIC%09Jan-07%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20119/8%09APNIC%09Jan-07%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20118/8%09APNIC%09Jan-07%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20117/8%09APNIC%09Jan-07%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED%20116/8%09APNIC%09Jan-07%09whois.apnic.net%09" +
                    "https://rdap.apnic.net/%09ALLOCATED")
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "ERROR:118: too many arguments supplied\n\nToo many arguments supplied.\n");
        }
    }

    @Test
    public void search_xsi_attributes_not_in_root_level_link() {
        final String whoisResources = RestTest.target(getPort(), "whois/search")
                .queryParam("query-string", "TP1-TEST")
                .queryParam("source", "TEST")
                .request(MediaType.APPLICATION_XML_TYPE).get(String.class);
        assertThat(whoisResources, not(containsString("xsi:type")));
        assertThat(whoisResources, not(containsString("xmlns:xsi")));
    }

    @Test
    public void search_resource_holder_autnum_xml() {
        databaseHelper.addObject(TEST_OTHER_ORGANISATION);
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "org:            ORG-TO1-TEST\n" +
                "status:         ASSIGNED\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources response = RestTest.target(getPort(), "whois/search.xml?query-string=AS102&resource-holder")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(3));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("AS102"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgKey(), is("ORG-TO1-TEST"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgName(), is("Test Organisation"));
    }

    @Test
    public void search_resource_holder_autnum_json() {
        databaseHelper.addObject(TEST_OTHER_ORGANISATION);
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "org:            ORG-TO1-TEST\n" +
                "status:         ASSIGNED\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources response = RestTest.target(getPort(), "whois/search.json?query-string=AS102&resource-holder")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(3));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("AS102"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgKey(), is("ORG-TO1-TEST"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgName(), is("Test Organisation"));
    }

    @Test
    public void search_resource_holder_lir_inetnum() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&resource-holder")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(3));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.0.255"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgKey(), is("ORG-TO2-TEST"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgName(), is("Test Organisation"));
    }

    @Test
    public void search_resource_holder_lir_parent_inetnum() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.255.255.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   PARENT-NET\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n");
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "netname:   CHILD-NET\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ASSIGNED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&resource-holder")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(2));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.0.255"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgKey(), is("ORG-TO2-TEST"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgName(), is("Test Organisation"));
    }

    @Test
    public void search_managed_attributes_lir_inetnum() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +          // managed
                        "org:       ORG-TO2-TEST\n" +                   // managed
                        "netname:   TEST-NET\n" +                       // managed
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +                   // managed
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +                // managed
                        "source:    TEST\n");                           // managed
        ipTreeUpdater.rebuild();

        final WhoisResources response0 = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);
        // Ensure passing no flags means that the comaintained flag is null (nulls are stripped from JSON response)
        assertThat(response0.getWhoisObjects(), hasSize(3));
        assertThat(response0.getWhoisObjects().get(0).isManaged(), is(nullValue()));
        assertThat(response0.getWhoisObjects().get(1).isManaged(), is(nullValue()));
        assertThat(response0.getWhoisObjects().get(2).isManaged(), is(nullValue()));
        assertThat(response0.getWhoisObjects().get(0).getAttributes(), hasSize(11));

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&managed-attributes")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(3));
        assertThat(response.getWhoisObjects().get(0).isManaged(), is(true));
        assertThat(response.getWhoisObjects().get(1).isManaged(), is(false));
        assertThat(response.getWhoisObjects().get(2).isManaged(), is(false));
        assertThat(response.getWhoisObjects().get(0).getAttributes(), hasSize(11));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(0).getName(), is("inetnum"));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(0).getManaged(), is(true));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(1).getName(), is("org"));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(1).getManaged(), is(true));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(2).getName(), is("netname"));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(2).getManaged(), is(true));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(3).getName(), is("descr"));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(3).getManaged(), is(nullValue()));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(7).getName(), is("status"));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(7).getManaged(), is(true));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(9).getName(), is("mnt-by"));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(9).getManaged(), is(true));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(10).getName(), is("source"));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(10).getManaged(), is(true));
    }

    @Test
    public void search_abuse_contact_lir_inetnum() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&abuse-contact")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(3));
        assertThat(response.getWhoisObjects().get(0).getAbuseContact().getKey(), is("TR1-TEST"));
        assertThat(response.getWhoisObjects().get(0).getAbuseContact().getEmail(), is("abuse@test.net"));
    }

    @Test
    public void search_inetnum_single_limit() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&limit=1")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.0.255"));
    }

    @Test
    public void search_inetnum_single_limit_no_referenced() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&limit=1&flags=rB")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.0.255"));
    }

    @Test
    public void search_inetnum_single_limit_no_referenced_offset_by_one() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&limit=1&flags=rB&offset=1")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(0));
    }

    @Test
    public void search_inetnum_single_limit_offset_by_one() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&limit=1&offset=1")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("ORG-TO2-TEST"));
    }

    @Test
    public void search_inetnum_offset_by_one() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&offset=1")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(2));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("ORG-TO2-TEST"));
        assertThat(response.getWhoisObjects().get(1).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void search_inetnum_single_limit_offset_by_two() {
        databaseHelper.addObject(TEST_LIR_ORGANISATION);
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        databaseHelper.addObject(
                "inetnum:   10.0.0.0 - 10.0.0.255\n" +
                        "org:       ORG-TO2-TEST\n" +
                        "netname:   TEST-NET\n" +
                        "descr:     description\n" +
                        "country:   NL\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "status:    ALLOCATED PA\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "mnt-by:    RIPE-NCC-HM-MNT\n" +
                        "source:    TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0%20-%2010.0.0.255&limit=1&offset=2")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    // If no source is defined (the default) both "source: RIPE" and “source: RIPE-NONAUTH” ROUTE(6) objects are returned
    @Test
    public void search_autnum_no_sources_given() {
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-NONAUTH\n"));
        ipTreeUpdater.rebuild();
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=AS102")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        boolean hasSourceTestNonAuth = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST-NONAUTH");
        boolean hasSourceTest = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST");
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));
        assertTrue(hasSourceTestNonAuth);
        assertTrue(hasSourceTest);
    }

    // searching for inetnum without specifying sources should return all related object even route with source: NONAUTH
    @Test
    public void search_inetnum_no_sources_given() {
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        // inetnum with route TEST-NONAUTH
        databaseHelper.addObject(RpslObject.parse("" +
                "inetnum:         10.0.0.0 - 10.0.0.255\n" +
                "netname:         NON-RIPE-NCC-MANAGED-ADDRESS-BLOCK\n" +
                "descr:           IPv4 address block not managed by the RIPE NCC\n" +
                "admin-c:         TP1-TEST\n" +
                "tech-c:          TP1-TEST\n" +
                "status:          ALLOCATED UNSPECIFIED\n" +
                "mnt-by:          RIPE-NCC-HM-MNT\n" +
                "mnt-lower:       RIPE-NCC-HM-MNT\n" +
                "source:          TEST\n"));
        databaseHelper.addObject(RpslObject.parse("" +
                "route:           10.0.0.0/24\n" +
                "descr:           Ripe test allocation\n" +
                "origin:          AS102\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-lower:       OWNER-MNT\n" +
                "source:          TEST-NONAUTH\n"));

        ipTreeUpdater.rebuild();
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=10.0.0.0/24")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        boolean hasSourceTestNonAuth = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST-NONAUTH");
        boolean hasSourceTest = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST");
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(4));
        assertTrue(hasSourceTestNonAuth);
        assertTrue(hasSourceTest);
    }

    @Test
    public void search_route_no_sources_given() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route:           193.4.0.0/16\n" +
                "descr:           Ripe test allocation\n" +
                "origin:          AS102\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          OWNER-MNT\n" +
                "mnt-lower:       OWNER-MNT\n" +
                "source:          TEST-NONAUTH\n"));
        ipTreeUpdater.rebuild();
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=193.4.0.0/16AS102")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        boolean hasSourceTestNonAuth = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST-NONAUTH");
        boolean hasSourceTest = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST");
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));
        assertTrue(hasSourceTestNonAuth);
        assertTrue(hasSourceTest);
    }

    //  If "sources" is used in queries out-of-region resources will be shown only if ‘RIPE-NONAUTH’ is included explicitly.
    @Test
    public void search_autnum_by_sources_nonauth_given() {
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-NONAUTH\n"));
        ipTreeUpdater.rebuild();
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=AS102&source=TEST-NONAUTH")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        final WhoisObject autnum = whoisResources.getWhoisObjects().get(0);

        assertThat(autnum.getType(), is("aut-num"));
        assertThat(autnum.getSource().getId(), is("test-nonauth"));
        assertThat(autnum.getLink().getType(), is("locator"));
        assertThat(autnum.getLink().getHref(), is("http://rest-test.db.ripe.net/test-nonauth/aut-num/AS102"));
        assertThat(autnum.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST-NONAUTH")
        ));

        final WhoisObject person = whoisResources.getWhoisObjects().get(1);

        assertThat(person.getType(), is("person"));
        assertThat(person.getSource().getId(), is("test"));
        assertThat(person.getLink().getType(), is("locator"));
        assertThat(person.getLink().getHref(), is("http://rest-test.db.ripe.net/test/person/TP1-TEST"));
        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void search_route_by_sources_nonauth_given() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route:           193.4.0.0/16\n" +
                "descr:           Ripe test allocation\n" +
                "origin:          AS102\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          OWNER-MNT\n" +
                "mnt-lower:       OWNER-MNT\n" +
                "source:          TEST-NONAUTH\n"));
        ipTreeUpdater.rebuild();
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=193.4.0.0/16AS102&source=TEST-NONAUTH")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        final WhoisObject route = whoisResources.getWhoisObjects().get(0);

        assertThat(route.getType(), is("route"));
        assertThat(route.getSource().getId(), is("test-nonauth"));
        assertThat(route.getLink().getType(), is("locator"));
        assertThat(route.getLink().getHref(), is("http://rest-test.db.ripe.net/test-nonauth/route/193.4.0.0/16AS102"));
        assertThat(route.getAttributes(), contains(
                new Attribute("route", "193.4.0.0/16"),
                new Attribute("descr", "Ripe test allocation"),
                new Attribute("origin", "AS102"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("mnt-lower", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST-NONAUTH")
        ));

        final WhoisObject person = whoisResources.getWhoisObjects().get(1);

        assertThat(person.getType(), is("person"));
        assertThat(person.getSource().getId(), is("test"));
        assertThat(person.getLink().getType(), is("locator"));
        assertThat(person.getLink().getHref(), is("http://rest-test.db.ripe.net/test/person/TP1-TEST"));
        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test(expected = NotFoundException.class)
    public void search_route_by_sources_auth_given() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route:           193.4.0.0/16\n" +
                "descr:           Ripe test allocation\n" +
                "origin:          AS102\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          OWNER-MNT\n" +
                "mnt-lower:       OWNER-MNT\n" +
                "source:          TEST-NONAUTH\n"));
        ipTreeUpdater.rebuild();
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=193.4.0.0/16AS102&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
    }

    @Test
    public void search_inetnum_with_resource_flag() {
        databaseHelper.addObject(RIPE_NCC_HM_MNT);
        // inetnum with route TEST-NONAUTH
        databaseHelper.addObject(RpslObject.parse("" +
                "inetnum:         193.4.0.0 - 193.4.0.255\n" +
                "netname:         NON-RIPE-NCC-MANAGED-ADDRESS-BLOCK\n" +
                "descr:           IPv4 address block not managed by the RIPE NCC\n" +
                "admin-c:         TP1-TEST\n" +
                "tech-c:          TP1-TEST\n" +
                "status:          ALLOCATED UNSPECIFIED\n" +
                "mnt-by:          RIPE-NCC-HM-MNT\n" +
                "mnt-lower:       RIPE-NCC-HM-MNT\n" +
                "source:          TEST\n"));
        databaseHelper.addObject(RpslObject.parse("" +
                "route:           193.4.0.0/24\n" +
                "descr:           Ripe test allocation\n" +
                "origin:          AS102\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          OWNER-MNT\n" +
                "mnt-lower:       OWNER-MNT\n" +
                "source:          TEST-NONAUTH\n"));
        ipTreeUpdater.rebuild();
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=193.4.0.0/24&resource")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        boolean hasSourceTestNonAuth = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST-NONAUTH");
        boolean hasSourceTest = hasObjectWithSpecifiedSource(whoisResources.getWhoisObjects(), "TEST");
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(4));
        assertTrue(hasSourceTestNonAuth);
        assertTrue(hasSourceTest);
    }

    @Test
    public void lookup_person_with_proxy_not_allowed() {

        databaseHelper.addObject("" +
                "person:    Lo Person\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "nic-hdl:   LP1-TEST\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n");

        try {
            RestTest.target(getPort(), "whois/search?query-string=LP1-TEST&source=TEST&client=testId,10.1.2.3")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().getStatus(), is(400));
            assertThat(e.getResponse().readEntity(String.class), containsString("ERROR:203: you are not allowed to act as a proxy"));
        }
    }

    @Test
    public void lookup_person_with_client_flag_no_proxy() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);

        databaseHelper.addObject("" +
                "person:    Lo Person\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "nic-hdl:   LP1-TEST\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n");

        final int limit = accessControlListManager.getPersonalObjects(localhost);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=LP1-TEST&source=TEST&flags=no-filtering&flags=rB&client=testId")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        //ACL is accounted for as there is no proxy ip specified
        final int remaining = accessControlListManager.getPersonalObjects(localhost);
        assertThat(remaining, is(limit - 1));

        assertThat(whoisResources.getParameters().getClient(), is("testId"));
    }

    @Test
    public void lookup_person_with_proxy() throws Exception {
        final String PROXY_CLIENT = "10.1.2.3";

        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final InetAddress proxyHost = InetAddress.getByName(PROXY_CLIENT);

        databaseHelper.addObject("" +
                "person:    Lo Person\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "nic-hdl:   LP1-TEST\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n");

        databaseHelper.insertAclIpProxy(LOCALHOST);
        ipResourceConfiguration.reload();

        final int limit = accessControlListManager.getPersonalObjects(localhost);
        final int proxylLimit = accessControlListManager.getPersonalObjects(proxyHost);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=LP1-TEST&source=TEST&flags=no-filtering&flags=rB&client=testId,10.1.2.3")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getParameters().getClient(), is("testId,10.1.2.3"));

        //only proxy ip is counted for ACL
        final int remaining = accessControlListManager.getPersonalObjects(localhost);
        assertThat(remaining, is(limit));

        final int proxyRemaining = accessControlListManager.getPersonalObjects(proxyHost);
        assertThat(proxyRemaining, is(proxylLimit - 1));


    }

    @Test
    public void query_string_normalised() {
        try {
            RestTest.target(getPort(), "whois/search?query-string=%E2%80%8E2019%2011:35%5D%20ok")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            // ensure no stack trace in response
            assertThat(e.getResponse().readEntity(String.class), not(containsString("Caused by:")));
        }

    }

    // helper methods

    private boolean hasObjectWithSpecifiedSource(List<WhoisObject> whoisObjects, String source) {
        return whoisObjects
                .stream()
                .anyMatch(object -> object.getAttributes()
                        .stream()
                        .anyMatch(attr -> AttributeType.SOURCE.equals(AttributeType.getByName(attr.getName()))
                                && attr.getValue().equalsIgnoreCase(source)));
    }
}
