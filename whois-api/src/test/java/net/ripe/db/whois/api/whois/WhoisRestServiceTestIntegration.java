package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.domain.*;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.*;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class WhoisRestServiceTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;
    private static final String VERSION_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:  Pauleth Palthen\n" +
            "address: Singel 258\n" +
            "phone:   +31-1234567890\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  OWNER-MNT\n" +
            "nic-hdl: PP1-TEST\n" +
            "changed: noreply@ripe.net 20120101\n" +
            "remarks: remark\n" +
            "source:  TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:  Test Person\n" +
            "address: Singel 258\n" +
            "phone:   +31 6 12345678\n" +
            "nic-hdl: TP1-TEST\n" +
            "mnt-by:  OWNER-MNT\n" +
            "changed: dbtest@ripe.net 20120101\n" +
            "source:  TEST\n");

    private static final RpslObject TEST_ROLE = RpslObject.parse("" +
            "role:      Test Role\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TR1-TEST\n" +
            "admin-c:   TR1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "changed:   dbtest@ripe.net 20120101\n" +
            "source:    TEST\n");

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        final JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, false);
        provider.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        cc.getSingletons().add(provider);
        client = Client.create(cc);
    }

    @Test
    public void lookup_inet6num_without_prefix_length() throws Exception {
        databaseHelper.addObject("" +
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
            createResource(AUDIENCE, "whois/test/inet6num/2001:2002:2003::").get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void lookup_inet6num_with_prefix_length() throws Exception {
        databaseHelper.addObject("" +
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/inet6num/2001:2002:2003::/48").get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("2001:2002:2003::/48"));
    }

    @Test
    public void lookup_person() throws Exception {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/person/TP1-TEST").get(WhoisResources.class);

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
    public void rest_get_person_json() throws Exception {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/person/TP1-TEST").accept(MediaType.APPLICATION_JSON_TYPE).get(WhoisResources.class);

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
    public void json_lookup_correct_object() throws Exception {
        final String whoisResources = createResource(AUDIENCE, "whois/test/person/TP1-TEST").accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
        assertThat(whoisResources, containsString("{\"object\":[{\"type\":\"person"));
        assertThat(whoisResources, containsString("\"tags\":{\"tag\":[]}}]}}"));
    }

    @Test
    public void lookup_role() throws Exception {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/role/TR1-TEST").get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("role", "Test Role"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TR1-TEST"),
                new Attribute("admin-c", "TR1-TEST", null, "role", new Link("locator", "http://rest-test.db.ripe.net/test/role/TR1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    public void lookup_person_accept_json() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/TEST/person/TP1-TEST")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void lookup_person_json_extension() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/TEST/person/TP1-TEST.json")
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void lookup_person_not_found() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test/person/PP1-TEST").get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void lookup_person_wrong_source() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test-grs/person/TP1-TEST").get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void grs_lookup_person_wrong_source() throws Exception {
        try {
            createResource(AUDIENCE, "whois/pez/person/PP1-TEST").get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Invalid source 'pez'"));
        }
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

        final WhoisResources whoisResources = createResource(AUDIENCE,
                "whois/TEST/aut-num/AS102")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "description"),
                new WhoisTag("other", "other stuff"),
                new WhoisTag("unref", "28")));
    }

    // create

    @Test
    public void create_succeeds() throws Exception {
        final boolean filter = false;
        final WhoisResources response = createResource(AUDIENCE, "whois/test?password=test")
                .post(WhoisResources.class, whoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
        final WhoisObject object = response.getWhoisObjects().get(0);

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
    }

    @Test
    public void create_invalid_source_in_request_body() throws Exception {
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
            createResource(AUDIENCE, "whois/test?password=test")
                    .post(Response.class, whoisObjectMapper.map(Lists.newArrayList(rpslObject), false));
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("Unrecognized source: NONE"));
        }
    }

    @Test
    public void create_invalid_reference() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test?password=test")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
                    .post(String.class,
                            "<whois-resources>\n" +
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
                                    "</whois-resources>");
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("\"admin-c\" is not valid for this object type"));
        }
    }

    @Test
    public void create_multiple_passwords() throws Exception {
        final boolean filter = false;
        createResource(AUDIENCE, "whois/test?password=invalid&password=test")
                .post(whoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
    }

    @Test
    public void create_invalid_password() throws Exception {
        try {
            final boolean filter = false;
            createResource(AUDIENCE, "whois/test?password=invalid")
                    .post(Response.class, whoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
            assertThat(e.getResponse().getEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void create_no_password() throws Exception {
        try {
            final boolean filter = false;
            createResource(AUDIENCE, "whois/test")
                    .post(Response.class, whoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
            assertThat(e.getResponse().getEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void create_already_exists() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test?password=test")
                    .post(Response.class, whoisObjectMapper.map(Lists.newArrayList(OWNER_MNT), false));
            fail("Expected failure as object already exists");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_CONFLICT));
            assertThat(e.getResponse().getEntity(String.class),
                    containsString("Enforced new keyword specified, but the object already exists in the database"));
        }
    }

    @Test
    public void create_delete_method_not_allowed() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test")
                    .delete();
            fail("expected DELETE method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void create_get_method_not_allowed() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail("expected GET method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void create_json_request() throws Exception {
        final String response = createResource(AUDIENCE, "whois/test?password=test")
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(String.class, whoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), false));

        assertThat(response, containsString("" +
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
                "        }, {"));
    }

    // delete

    @Test
    public void delete_succeeds() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);
        createResource(AUDIENCE, "whois/test/person/PP1-TEST?password=test").delete();
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_nonexistant() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test/person/NON-EXISTANT").delete();
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_NOT_FOUND));
        }
    }

    @Test
    public void delete_referenced_from_other_objects() throws Exception {
        try {
            createResource(AUDIENCE, "whois/test/person/TP1-TEST?password=test").delete();
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
            assertThat(e.getResponse().getEntity(String.class),
                    containsString("Object [person] TP1-TEST is referenced from other objects"));
        }
    }

    @Test
    public void delete_invalid_password() throws Exception {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/test/person/PP1-TEST?password=invalid").delete();
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
            assertThat(e.getResponse().getEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void delete_no_password() throws Exception {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/test/person/PP1-TEST").delete();
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
            assertThat(e.getResponse().getEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    // update
    @Test
    public void update_succeeds() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final RpslObject updatedObject = new RpslObjectFilter(PAULETH_PALTHEN).addAttributes(
                Lists.newArrayList(new RpslAttribute(AttributeType.REMARKS, "updated")));

        WhoisResources response = createResource(AUDIENCE, "whois/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_XML)
                .put(WhoisResources.class, whoisObjectMapper.map(Lists.newArrayList(updatedObject)));

        assertThat(response.getWhoisObjects(), hasSize(1));
        final WhoisObject object = response.getWhoisObjects().get(0);
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
    }

    @Test
    public void update_without_query_params() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/test/person/PP1-TEST")
                    .accept(MediaType.APPLICATION_XML)
                    .put(WhoisResources.class, whoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)));
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
        }
    }

    @Test
    public void update_post_not_allowed() {
        try {
            createResource(AUDIENCE, "whois/test/person/PP1-TEST?password=test")
                    .accept(MediaType.APPLICATION_XML)
                    .post(WhoisResources.class, whoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)));
            fail("expected update with POST method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

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
        databaseHelper.removeObject(autnum);
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

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class).getVersions().getVersions();

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
        databaseHelper.removeObject(autnum);
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

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions")
                .accept(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class).getVersions().getVersions();

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
        databaseHelper.removeObject(autnum);

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class).getVersions().getVersions();

        assertThat(versions, hasSize(1));
        assertThat(versions.get(0).getDeletedDate(), stringMatchesRegexp(VERSION_DATE_PATTERN));
        assertThat(versions.get(0).getOperation(), is(nullValue()));
        assertThat(versions.get(0).getDate(), is(nullValue()));
        assertThat(versions.get(0).getRevision(), is(nullValue()));
    }

    @Test
    public void versions_no_versions_found() throws IOException {
        try {
            createResource(AUDIENCE, "whois/test/aut-num/AS102/versions")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("Not Found"));
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
            createResource(AUDIENCE, "whois/test/aut-num/AS102/versions/2").get(WhoisResources.class);
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_NOT_FOUND));
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions/1")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

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
    public void rest_versions() throws IOException {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions/1")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions/1")
                .accept(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

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
        databaseHelper.removeObject(autnum);

        try {
            createResource(AUDIENCE, "whois/test/aut-num/AS102/versions/1")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail("expected not found (404)");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("404"));
        }
    }

    // schema
    // TODO: schema is outdated, either remove or fix and link from REST API doc

    @Test
    public void schema_int() throws Exception {
        final String response = createResource(Audience.INTERNAL, "api-doc/whois-resources.xsd")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);
        assertThat(response, containsString("<xs:element name=\"whois-resources\">"));
    }

    @Test
    public void schema_ext() throws Exception {
        final String response = createResource(Audience.PUBLIC, "api-doc/whois-resources.xsd")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);
        assertThat(response, containsString("<xs:element name=\"whois-resources\">"));
    }

    // response format

    @Test
    public void lookup_accept_application_xml() throws Exception {
        final String response = createResource(AUDIENCE, "whois/test/person/TP1-TEST")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);
        assertThat(response, containsString("<?xml version='1.0' encoding='UTF-8'?>"));
        assertThat(response, containsString("<whois-resources"));
    }

    @Test
    public void lookup_accept_text_xml() throws Exception {
        final String response = createResource(AUDIENCE, "whois/test/person/TP1-TEST")
                .accept("text/xml")
                .get(String.class);
        assertThat(response, containsString("<?xml version='1.0' encoding='UTF-8'?>"));
        assertThat(response, containsString("<whois-resources"));
    }

    @Test
    public void lookup_accept_application_json() throws Exception {
        final String response = createResource(AUDIENCE, "whois/test/person/TP1-TEST")
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"xlink:type\""));
        assertThat(response, containsString("\"xlink:href\""));
    }

    @Test
    public void lookup_accept_text_json() throws Exception {
        final String response = createResource(AUDIENCE, "whois/test/person/TP1-TEST")
                .accept("text/json")
                .get(String.class);
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"xlink:type\""));
        assertThat(response, containsString("\"xlink:href\""));
    }

    @Test
    public void lookup_json_extension() throws Exception {
        final String response = createResource(AUDIENCE, "whois/test/person/TP1-TEST.json")
                .get(String.class);
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"xlink:type\""));
        assertThat(response, containsString("\"xlink:href\""));
    }

    @Test
    public void update_json_request_and_response_content() throws Exception {
        final String update =
                "{\n" +
                        "  \"objects\" : {\n" +
                        "      \"object\" : [ {\n" +
                        "        \"source\" : {\n" +
                        "          \"id\" : \"test\"\n" +
                        "        },\n" +
                        "        \"attributes\" : {\n" +
                        "          \"attribute\" : [\n" +
                        "            {\"name\":\"mntner\", \"value\":\"OWNER-MNT\"},\n" +
                        "            {\"name\":\"descr\", \"value\":\"description\"},\n" +
                        "            {\"name\":\"admin-c\", \"value\":\"TP1-TEST\"},\n" +
                        "            {\"name\":\"upd-to\", \"value\":\"noreply@ripe.net\"},\n" +
                        "            {\"name\":\"auth\", \"value\":\"MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/\"},\n" +
                        "            {\"name\":\"mnt-by\", \"value\":\"OWNER-MNT\"},\n" +
                        "            {\"name\":\"referral-by\", \"value\":\"OWNER-MNT\"},\n" +
                        "            {\"name\":\"changed\", \"value\":\"dbtest@ripe.net 20120101\"},\n" +
                        "            {\"name\":\"source\", \"value\":\"TEST\"}\n" +
                        "        ] }\n" +
                        "     }]\n" +
                        "   }\n" +
                        "}";

        final String response = createResource(AUDIENCE, "whois/test/mntner/OWNER-MNT?password=test")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(String.class, update);

        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"dbtest@ripe.net 20120101\""));
    }

    @Test
    public void lookup_xml_response_doesnt_contain_invalid_values() throws Exception {
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

        final String response = createResource(AUDIENCE, "whois/test/mntner/TEST-MNT")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, not(containsString("\u001b")));
        assertThat(response, not(containsString("<b>")));
        assertThat(response, not(containsString("&#x0;")));
        assertThat(response, not(containsString("<!--")));
    }

    // search

    @Test
    public void search() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        final WhoisObject autnum = whoisResources.getWhoisObjects().get(0);
        assertThat(autnum.getType(), is("aut-num"));
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

        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Test
    public void search_accept_json() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void search_json_extension() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search.json?query-string=TP1-TEST&source=TEST")
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
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

        final WhoisResources resources = createResource(AUDIENCE, "whois/search?query-string=LP1-TEST&source=TEST&flags=no-filtering&flags=rB")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(resources.getWhoisObjects(), hasSize(1));

        final List<Flag> flags = resources.getParameters().getFlags().getFlags();
        assertThat(flags, hasSize(3));
        assertThat(flags.get(0).getValue(), is("r"));
        assertThat(flags.get(1).getValue(), is("B"));
        assertThat(flags.get(2).getValue(), is("no-filtering"));
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
            createResource(AUDIENCE, "whois/search?query-string=LP1-TEST&source=TEST&flags=show-tag-inforG")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Invalid option 'h'"));
        }
    }

    @Test
    public void search_invalid_flag() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=LP1-TEST&source=TEST&flags=q")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Disallowed option 'q'"));
        }
    }

    @Test
    public void search_tags_in_response() throws Exception {
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

        final WhoisResources whoisResources = createResource(AUDIENCE,
                "whois/TEST/aut-num/AS102?include-tag=foobar&include-tag=unref")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

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

        final WhoisResources whoisResources = createResource(AUDIENCE,
                "whois/search?source=TEST&query-string=AS102&include-tag=foobar&include-tag=unref")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

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
            createResource(AUDIENCE,
                    "whois/search?source=TEST&query-string=AS102&include-tag=foobar")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
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
            createResource(AUDIENCE,
                    "whois/search?source=TEST&query-string=AS102&exclude-tag=foobar&include-tag=unref&include-tag=other")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
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

        final WhoisResources whoisResources = createResource(AUDIENCE,
                "whois/search?source=TEST&query-string=AS102&exclude-tag=other&include-tag=unref&include-tag=foobar")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

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
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void search_no_querystring_given() {
        try {
            createResource(AUDIENCE, "whois/search?source=TEST")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void search_invalid_source() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=AS102&source=INVALID")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Invalid source 'INVALID'"));
            assertThat(e.getResponse().getEntity(String.class), not(containsString("Caused by:")));
        }
    }

    @Test
    public void grs_search_invalid_source() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=AS102&source=INVALID")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Invalid source 'INVALID'"));
            assertThat(e.getResponse().getEntity(String.class), not(containsString("Caused by:")));
        }
    }

    @Test
    public void search_multiple_sources() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&source=RIPE")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    public void search_with_type_filter() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST&type-filter=aut-num,as-block")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(2));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
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
    public void search_inverse() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&inverse-attribute=admin-c,tech-c")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(4));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));
        assertThat(whoisResources.getWhoisObjects().get(1).getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
        assertThat(whoisResources.getWhoisObjects().get(2).getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest-test.db.ripe.net/test/person/TP1-TEST")),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
        assertThat(whoisResources.getWhoisObjects().get(3).getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Test
    public void search_flags() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&flags=BrCx")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
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
    public void search_invalid_flags() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&flags=kq")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Disallowed option 'k'"));
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST-GRS")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
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
    public void search_parameters_are_returned() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = createResource(AUDIENCE, "" +
                "whois/search?inverse-attribute=person" +
                "&type-filter=aut-num" +
                "&source=test" +
                "&flags=rB" +
                "&query-string=TP1-TEST")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        final Parameters parameters = whoisResources.getParameters();
        final Flags flags = parameters.getFlags();
        assertThat(flags.getFlags().get(0).getValue(), is("r"));
        assertThat(flags.getFlags().get(1).getValue(), is("B"));
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
            createResource(AUDIENCE, "whois/search?query-string=NONEXISTANT&source=TEST")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), not(containsString("Caused by:")));
        }
    }

    @Test
    public void streaming_puts_xlink_into_root_element_and_nowhere_else() throws Exception {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String whoisResources = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(whoisResources, containsString("<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">"));
        assertThat(whoisResources, containsString("<object type=\"aut-num\">"));
        assertThat(whoisResources, containsString("<objects>"));
    }

    @Test
    public void non_streaming_puts_xlink_into_root_element_and_nowhere_else() throws Exception {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");
        databaseHelper.addObject(autnum);

        final String whoisResources = createResource(AUDIENCE, "whois/test/aut-num/AS102/versions/1")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(whoisResources, containsString("<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">"));
        assertThat(whoisResources, containsString("<object type=\"aut-num\" version=\"1\">"));
        assertThat(whoisResources, containsString("<objects>"));
    }

    @Test
    public void get_inet6num() throws Exception {
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/inet6num/2001::/48").get(WhoisResources.class);
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

    @Test
    public void get_route() throws Exception {
        final RpslObject route = RpslObject.parse("" +
                "route:           193.254.30.0/24\n" +
                "descr:           Test route\n" +
                "origin:          AS12726\n" +
                "mnt-by:          OWNER-MNT\n" +
                "changed:         ripe@test.net 20091015\n" +
                "source:          TEST\n");
        databaseHelper.addObject(route);
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/test/route/193.254.30.0/24AS12726").get(WhoisResources.class);
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink().getHref(), is("http://rest-test.db.ripe.net/test/route/193.254.30.0/24AS12726"));
        assertThat(whoisObject.getAttributes(), containsInAnyOrder(
                new Attribute("route", "193.254.30.0/24"),
                new Attribute("descr", "Test route"),
                new Attribute("origin", "AS12726", null, "aut-num", new Link("locator", "http://rest-test.db.ripe.net/test/aut-num/AS12726")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Ignore("TODO: [ES] don't set the content-type on an error response")
    @Test
    public void search_dont_set_content_type_on_error() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=INVALID")
                    .get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("Invalid source 'INVALID'"));
            assertThat(e.getResponse().getEntity(String.class), not(containsString("Caused by:")));
            assertThat(e.getResponse().getHeaders().get("Content-Type"), not(contains("application/xml"))); //is(empty()));
        }
    }

    // helper methods

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }
}
