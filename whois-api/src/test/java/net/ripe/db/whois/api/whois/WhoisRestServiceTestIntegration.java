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
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;
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

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new JacksonJaxbJsonProvider().configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true));
        client = Client.create(cc);
    }

    @Test
    public void lookup_object() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/lookup/test/person/PP1-TEST").get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final RpslObject rpslObject = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(rpslObject.getKey(), is(ciString("PP1-TEST")));
    }

    @Test
    public void lookup_object_not_found() throws Exception {
        try {
            createResource(AUDIENCE, "whois/lookup/test/person/PP1-TEST").get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void lookup_object_wrong_source() throws Exception {
        try {
            createResource(AUDIENCE, "whois/lookup/test-grs/person/PP1-TEST").get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("The given grs source id: 'test-grs' is not valid"));
        }
    }

    @Test
    public void grs_lookup_object_wrong_source() throws Exception {
        try {
            createResource(AUDIENCE, "whois/grs-lookup/test/person/PP1-TEST").get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("The given grs source id: 'test' is not valid"));
        }
    }

    // create

    @Test
    public void create_succeeds() throws Exception {
        final boolean filter = false;
        createResource(AUDIENCE, "whois/create/test?password=test")
                .post(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
    }

    @Test
    public void create_no_source_in_url() throws Exception {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31-1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "changed: noreply@ripe.net 20120101\n" +
                "remarks: remark\n" +
                "source:  TEST\n");
        try {
            createResource(AUDIENCE, "whois/create?password=test")
                    .post(Response.class, WhoisObjectMapper.map(Lists.newArrayList(rpslObject), false));
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("Source must be specified in URL"));
        }
    }

    @Test
    public void create_invalid_source_in_url() throws Exception {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31-1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "changed: noreply@ripe.net 20120101\n" +
                "remarks: remark\n" +
                "source:  TEST\n");
        try {
            createResource(AUDIENCE, "whois/create/INVALID?password=test")
                    .post(WhoisObjectMapper.map(Lists.newArrayList(rpslObject), false));
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("Invalid source specified: INVALID"));
        }
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
            createResource(AUDIENCE, "whois/create/TEST?password=test")
                    .post(Response.class, WhoisObjectMapper.map(Lists.newArrayList(rpslObject), false));
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("Unrecognized source: NONE"));
        }
    }

    @Test
    public void create_invalid_reference() throws Exception {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31-1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "admin-c: INVALID\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "changed: noreply@ripe.net 20120101\n" +
                "remarks: remark\n" +
                "source:  TEST\n");
        try {
            final boolean filter = false;
            createResource(AUDIENCE, "whois/create/test?password=test")
                    .post(Response.class, WhoisObjectMapper.map(Lists.newArrayList(rpslObject), filter));
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("\"admin-c\" is not valid for this object type"));
        }
    }

    @Test
    public void create_multiple_passwords() throws Exception {
        final boolean filter = false;
        createResource(AUDIENCE, "whois/create/test?password=invalid&password=test")
                .post(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
    }

    @Test
    public void create_invalid_password() throws Exception {
        try {
            final boolean filter = false;
            createResource(AUDIENCE, "whois/create/test?password=invalid")
                    .post(Response.class, WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
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
            createResource(AUDIENCE, "whois/create/test")
                    .post(Response.class, WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter));
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
            createResource(AUDIENCE, "whois/create/test?password=test")
                    .post(Response.class, WhoisObjectMapper.map(Lists.newArrayList(OWNER_MNT), false));
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
            createResource(AUDIENCE, "whois/create")
                    .delete();
            fail("expected DELETE method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void create_get_method_not_allowed() throws Exception {
        try {
            createResource(AUDIENCE, "whois/create/test")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail("expected GET method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void create_json_request() throws Exception {
        createResource(AUDIENCE, "whois/create/test?password=test")
                .type(MediaType.APPLICATION_JSON)
                .post(String.class, WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), false));
    }

    // delete

    @Test
    public void delete_succeeds() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);
        createResource(AUDIENCE, "whois/delete/test/person/PP1-TEST?password=test").delete();
    }

    @Test
    public void delete_nonexistant() throws Exception {
        try {
            createResource(AUDIENCE, "whois/delete/test/person/NON-EXISTANT").delete();
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_NOT_FOUND));
        }
    }

    @Test
    public void delete_referenced_from_other_objects() throws Exception {
        try {
            createResource(AUDIENCE, "whois/delete/test/person/TP1-TEST?password=test").delete();
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
            createResource(AUDIENCE, "whois/delete/test/person/PP1-TEST?password=invalid").delete();
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
            createResource(AUDIENCE, "whois/delete/test/person/PP1-TEST").delete();
            fail("expected request to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
            assertThat(e.getResponse().getEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void delete_post_not_allowed() throws Exception {
        try {
            createResource(AUDIENCE, "whois/delete")
                    .accept(MediaType.APPLICATION_XML)
                    .post(WhoisResources.class, WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)));
            fail("expected POST method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_NOT_FOUND));
        }
    }

    @Test
    public void delete_get_not_allowed() throws Exception {
        try {
            createResource(AUDIENCE, "whois/delete")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail("expected GET method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_NOT_FOUND));
        }
    }

    // update

    @Test
    public void update_succeeds() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final RpslObject updatedObject = new RpslObjectFilter(PAULETH_PALTHEN).addAttributes(
                Lists.newArrayList(new RpslAttribute(AttributeType.REMARKS, "updated")));

        WhoisResources response = createResource(AUDIENCE, "whois/update/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_XML)
                .put(WhoisResources.class, WhoisObjectMapper.map(Lists.newArrayList(updatedObject)));

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
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void update_invalid_source() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/update/invalid/person/PP1-TEST?password=test")
                    .accept(MediaType.APPLICATION_XML)
                    .put(WhoisResources.class, WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)));
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
            assertThat(e.getResponse().getEntity(String.class), is("Invalid source specified: invalid"));
        }
    }

    @Test
    public void update_without_query_params() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/update")
                    .accept(MediaType.APPLICATION_XML)
                    .put(WhoisResources.class, WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)));
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_NOT_FOUND));
        }
    }

    @Test
    public void update_post_not_allowed() {
        try {
            createResource(AUDIENCE, "whois/update/test/person/PP1-TEST?password=test")
                    .accept(MediaType.APPLICATION_XML)
                    .post(WhoisResources.class, WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)));
            fail("expected update with POST method to fail");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void modify_replace_attributes() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_XML)
                .post(WhoisResources.class, new WhoisModify(
                        new WhoisModify.Replace("address",
                                Lists.newArrayList(
                                        new Attribute("address", "P.O. Box 10096"),
                                        new Attribute("address", "1001 EB Amsterdam"),
                                        new Attribute("address", "The Netherlands")))));

        assertThat(response.getWhoisObjects(), hasSize(1));
        final WhoisObject object = response.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "P.O. Box 10096"),
                new Attribute("address", "1001 EB Amsterdam"),
                new Attribute("address", "The Netherlands"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void modify_replace_attributes_json_request_and_response() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisModify whoisModify = new WhoisModify(
                new WhoisModify.Replace("address",
                        Lists.newArrayList(
                                new Attribute("address", "P.O. Box 10096"),
                                new Attribute("address", "1001 EB Amsterdam"),
                                new Attribute("address", "The Netherlands"))));

        WhoisResources response = createResource(AUDIENCE, "whois/modify/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_JSON)
                .entity(whoisModify, MediaType.APPLICATION_JSON).post(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        final WhoisObject object = response.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "P.O. Box 10096"),
                new Attribute("address", "1001 EB Amsterdam"),
                new Attribute("address", "The Netherlands"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void modify_supports_text_json_accept_and_content_type_headers() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisModify whoisModify = new WhoisModify(
                new WhoisModify.Replace("address",
                        Lists.newArrayList(
                                new Attribute("address", "P.O. Box 10096"),
                                new Attribute("address", "1001 EB Amsterdam"),
                                new Attribute("address", "The Netherlands"))));

        WhoisResources response = createResource(AUDIENCE, "whois/modify/test/person/PP1-TEST?password=test")
                .accept("text/json")
                .entity(whoisModify, "text/json").post(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void modify_append_new_attributes() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_XML)
                .post(WhoisResources.class, new WhoisModify(
                        new WhoisModify.Add(
                                Lists.newArrayList(
                                        new Attribute("remarks", "updated")))));

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
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void modify_add_new_attributes_from_index() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_XML)
                .post(WhoisResources.class, new WhoisModify(
                        new WhoisModify.Add(5,
                                Lists.newArrayList(
                                        new Attribute("remarks", "These remark lines will be added"),
                                        new Attribute("remarks", "starting from index 5 (line 6) !")))));

        assertThat(response.getWhoisObjects(), hasSize(1));
        final WhoisObject object = response.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("remarks", "These remark lines will be added"),
                new Attribute("remarks", "starting from index 5 (line 6) !"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("remarks", "remark"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void modify_remove_all_attributes_of_type() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_XML)
                .post(WhoisResources.class, new WhoisModify(new WhoisModify.Remove("remarks")));

        assertThat(response.getWhoisObjects(), hasSize(1));
        final WhoisObject object = response.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void modify_remove_attribute_at_index() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/test/person/PP1-TEST?password=test")
                .accept(MediaType.APPLICATION_XML)
                .post(WhoisResources.class, new WhoisModify(new WhoisModify.Remove(7)));

        assertThat(response.getWhoisObjects(), hasSize(1));
        final WhoisObject object = response.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/versions/TEST/AS102")
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
        final RpslObject autnum = RpslObject.parse(
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
        databaseHelper.updateObject(
                "aut-num:        AS102\n" +
                        "as-name:        End-User-2\n" +
                        "descr:          description\n" +
                        "admin-c:        TP1-TEST\n" +
                        "tech-c:         TP1-TEST\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "changed:        noreply@ripe.net 20120101\n" +
                        "source:         TEST\n");

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/versions/TEST/AS102")
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

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/versions/TEST/AS102")
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

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/versions/TEST/AS102")
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
            createResource(AUDIENCE, "whois/versions/TEST/AS102")
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
            createResource(AUDIENCE, "whois/version/TEST/2/AS102").get(WhoisResources.class);
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

        System.out.println(createResource(AUDIENCE, "whois/version/TEST/1/AS102")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class));


        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/version/TEST/1/AS102")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));
        final List<Attribute> attributes = object.getAttributes();
        final List<Attribute> originalAttributes = WhoisObjectMapper.map(autnum).getAttributes();

        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i), is(attributes.get(i)));
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

        System.out.println(createResource(AUDIENCE, "whois/version/TEST/1/AS102")
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class));


        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/version/TEST/1/AS102")
                .accept(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getType(), is("aut-num"));
        assertThat(object.getVersion(), is(1));
        final List<Attribute> attributes = object.getAttributes();
        final List<Attribute> originalAttributes = WhoisObjectMapper.map(autnum).getAttributes();
        for (int i = 0; i < originalAttributes.size(); i++) {
            assertThat(originalAttributes.get(i), is(attributes.get(i)));
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
            createResource(AUDIENCE, "whois/version/TEST/1/AS102")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail("expected not found (404)");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("404"));
        }
    }

    // schema

    @Test
    @Ignore
    public void schema_int() throws Exception {
        final String response = doGetRequest(getUrl("xsd/int-docs/whois-resources.xsd"), HttpURLConnection.HTTP_OK);
        assertThat(response, containsString("<xs:element name=\"whois-resources\">"));
    }

    @Test
    @Ignore
    public void schema_ext() throws Exception {
        final String response = doGetRequest(getUrl("xsd/ext-docs/whois-resources.xsd"), HttpURLConnection.HTTP_OK);
        assertThat(response, containsString("<xs:element name=\"whois-resources\">"));
    }

    // response format

    @Test
    public void lookup_json_response() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) (new URL(getUrl("lookup/test/person/TP1-TEST"))).openConnection();
        connection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        assertThat(connection.getResponseCode(), is(HttpURLConnection.HTTP_OK));

        final String response = readResponse(connection);

        assertThat(response, containsString("\"whois-resources\""));
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

        final String response = doPostOrPutRequest(getUrl("update/test/mntner/OWNER-MNT?password=test"), "PUT", update, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("\"whois-resources\""));
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"dbtest@ripe.net 20120101\""));
    }

    @Test
    public void modify_json_request_and_response_content() throws Exception {
        final String update =
                "{\n" +
                        "  \"add\" : {\n" +
                        "    \"attributes\" : {\n" +
                        "          \"attribute\" : [\n" +
                        "                {\n" +
                        "                  \"name\" : \"remarks\",\n" +
                        "                  \"value\" : \"updated\"\n" +
                        "                }\n" +
                        "        ] }\n" +
                        "  }\n" +
                        "}";

        final String response = doPostOrPutRequest(getUrl("modify/test/mntner/OWNER-MNT?password=test"), "POST", update, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"name\" : \"remarks\""));
        assertThat(response, containsString("\"value\" : \"updated\""));
    }

    @Test
    public void xml_response_doesnt_contain_invalid_values() throws Exception {
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

        HttpURLConnection connection = (HttpURLConnection) (new URL(getUrl("lookup/test/mntner/TEST-MNT"))).openConnection();
        connection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
        assertThat(connection.getResponseCode(), is(HttpURLConnection.HTTP_OK));

        final String response = readResponse(connection);

        assertThat(response, not(containsString("\u001b")));
        assertThat(response, not(containsString("<b>")));
        assertThat(response, not(containsString("&#x0;")));
        assertThat(response, not(containsString("<!--")));
    }

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

        final String s = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST")
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);

        System.out.println(s);


        final WhoisResources whoisResources2 = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources2.getWhoisObjects(), hasSize(2));

        final WhoisObject autnum = whoisResources2.getWhoisObjects().get(0);
        assertThat(autnum.getType(), is("aut-num"));
        assertThat(autnum.getPrimaryKey().get(0).getValue(), is("AS102"));
        assertThat(autnum.getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));

        final WhoisObject person = whoisResources2.getWhoisObjects().get(1);
        assertThat(person.getType(), is("person"));
        assertThat(person.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));

        assertThat(person.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Test
    public void search_no_sources_given() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=AS102")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("Argument 'source' is missing, you have to specify a valid RIR source for your search request"));
        }
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
            assertThat(e.getResponse().getEntity(String.class), is("The given source id: 'INVALID' is not valid"));
        }
    }

    @Test
    public void grs_search_invalid_source() {
        try {
            createResource(AUDIENCE, "whois/grs-search?query-string=AS102&source=INVALID")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("The given grs source id: 'INVALID' is not valid"));
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
                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
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
                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));
        assertThat(whoisResources.getWhoisObjects().get(1).getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
        assertThat(whoisResources.getWhoisObjects().get(2).getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("referral-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
        assertThat(whoisResources.getWhoisObjects().get(3).getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Test
    public void search_flags() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&flags=BrGCx")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("changed", "dbtest@ripe.net 20120101"),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void search_invalid_flags() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&flags=k")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("The flag: k is not valid."));
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/grs-search?query-string=AS102&source=TEST-GRS")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "DUMY-RIPE", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test-grs/person-role/DUMY-RIPE")),
                new Attribute("tech-c", "DUMY-RIPE", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test-grs/person-role/DUMY-RIPE")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test-grs/mntner/OWNER-MNT")),
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
    public void tags_include() {
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
                "whois/tags/TEST/AS102?include=foobar&include=unref")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        // TODO [AK] Add later, should be on object level
//        final List<WhoisTag> tags = whoisResources.getTags().getTags();
//        assertThat(tags, hasSize(3));
//        assertThat(tags.get(0).getId(), is("foobar"));
//        assertThat(tags.get(0).getData(), is("description"));
//        assertThat(tags.get(1).getId(), is("other"));
//        assertThat(tags.get(1).getData(), is("other stuff"));
//        assertThat(tags.get(2).getId(), is("unref"));
//        assertThat(tags.get(2).getData(), is("28"));
//
//        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
//                new Attribute("aut-num", "AS102"),
//                new Attribute("as-name", "End-User-2"),
//                new Attribute("descr", "description"),
//                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
//                new Attribute("tech-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
//                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
//                new Attribute("source", "TEST")
//        ));
    }

    @Test
    public void tag_include_no_results() {
        try {
            createResource(AUDIENCE,
                    "whois/tags/TEST/TP1-TEST?include=foobar")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }


    @Test
    public void tag_exclude_shows_related_objects() {
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
                "whois/tags/TEST/AS102?exclude=foobar&exclude=unref")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));

        // TODO [AK] Add later, should be on object level
//        assertThat(whoisResources.getTags(), nullValue());
    }

    @Test
    public void tags_include_and_exclude_no_results() {
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
                    "whois/tags/TEST/AS102?exclude=foobar&include=unref&include=other")
                    .accept(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void tags_include_and_exclude() {
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
                "whois/tags/TEST/AS102?exclude=other&include=unref&include=foobar")
                .accept(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        // TODO [AK] Add later, should be on object level
//        final List<WhoisTag> tags = whoisResources.getTags().getTags();
//        assertThat(tags, hasSize(2));
//        assertThat(tags.get(0).getId(), is("foobar"));
//        assertThat(tags.get(0).getData(), is("foobar"));
//        assertThat(tags.get(1).getId(), is("unref"));
//        assertThat(tags.get(1).getData(), is("28"));

        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), contains(
                new Attribute("aut-num", "AS102"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST")
        ));
    }

    // helper methods

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }

    private String getUrl(final String command) {
        return "http://localhost:" + getPort(Audience.PUBLIC) + "/whois/" + command;
    }
}
