package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.domain.*;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
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
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
    }

    @Before
    @Override
    public void setUpClient() {
        final JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
        provider.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        client = ClientBuilder.newBuilder()
                .register(provider)
                .build();
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
            createResource(AUDIENCE, "whois/lookup/test/inet6num/2001:2002:2003::").request().get(WhoisResources.class);
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/lookup/test/inet6num/2001:2002:2003::/48").request().get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final RpslObject inet6num = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(inet6num.getKey(), is(ciString("2001:2002:2003::/48")));
    }

    @Test
    public void lookup_object() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/lookup/test/person/PP1-TEST").request().get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final RpslObject rpslObject = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(rpslObject.getKey(), is(ciString("PP1-TEST")));
    }

    @Test
    public void lookup_object_accept_json() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/lookup/TEST/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final RpslObject rpslObject = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(rpslObject.getKey(), is(ciString("TP1-TEST")));
    }

    @Test
    public void lookup_object_json_extension() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/lookup/TEST/person/TP1-TEST.json")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final RpslObject rpslObject = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(rpslObject.getKey(), is(ciString("TP1-TEST")));
    }

    @Test
    public void lookup_object_not_found() {
        try {
            createResource(AUDIENCE, "whois/lookup/test/person/PP1-TEST").request().get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void lookup_object_wrong_source() {
        try {
            createResource(AUDIENCE, "whois/lookup/test-grs/person/TP1-TEST").request().get(String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void grs_lookup_object_wrong_source() {
        try {
            createResource(AUDIENCE, "whois/lookup/pez/person/PP1-TEST").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Invalid source 'pez'"));
        }
    }

    @Test
    public void lookup_includes_tags() {
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
                "whois/lookup/TEST/aut-num/AS102")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "description"),
                new WhoisTag("other", "other stuff"),
                new WhoisTag("unref", "28")));
    }

    // create

    @Test
    public void create_succeeds() {
        final boolean filter = false;
        createResource(AUDIENCE, "whois/create?password=test")
                .request()
                .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter), MediaType.APPLICATION_XML));
    }

    @Test
    public void create_source_in_url() {
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
            createResource(AUDIENCE, "whois/create/test?password=test")
                    .request()
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(rpslObject), false), MediaType.APPLICATION_JSON_TYPE), String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
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
            createResource(AUDIENCE, "whois/create?password=test")
                    .request()
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(rpslObject), false), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Unrecognized source: NONE"));
        }
    }

    @Test
    public void create_invalid_reference() {
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
            createResource(AUDIENCE, "whois/create?password=test")
                    .request()
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(rpslObject), filter), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("\"admin-c\" is not valid for this object type"));
        }
    }

    @Test
    public void create_multiple_passwords() {
        final boolean filter = false;
        createResource(AUDIENCE, "whois/create?password=invalid&password=test")
                .request()
                .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter), MediaType.APPLICATION_XML), String.class);
    }

    @Test
    public void create_invalid_password() {
        try {
            final boolean filter = false;
            createResource(AUDIENCE, "whois/create?password=invalid")
                    .request()
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void create_no_password() {
        try {
            final boolean filter = false;
            createResource(AUDIENCE, "whois/create")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), filter), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void create_already_exists() {
        try {
            createResource(AUDIENCE, "whois/create?password=test")
                    .request()
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(OWNER_MNT), false), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_CONFLICT));
            assertThat(e.getResponse().readEntity(String.class),
                    containsString("Enforced new keyword specified, but the object already exists in the database"));
        }
    }

    @Test
    public void create_delete_method_not_allowed() {
        try {
            createResource(AUDIENCE, "whois/create")
                    .request()
                    .delete(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void create_get_method_not_allowed() {
        try {
            createResource(AUDIENCE, "whois/create")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void create_json_request() {
        final String response = createResource(AUDIENCE, "whois/create?password=test")
                .request()
                .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN), false), MediaType.APPLICATION_JSON), String.class);
        assertThat(response, isEmptyString());
    }

    // delete

    @Test
    public void delete_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        createResource(AUDIENCE, "whois/delete/person/PP1-TEST?password=test").request().delete(String.class);
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_nonexistant() {
        try {
            createResource(AUDIENCE, "whois/delete/person/NON-EXISTANT").request().delete(String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void delete_referenced_from_other_objects() {
        try {
            createResource(AUDIENCE, "whois/delete/person/TP1-TEST?password=test").request().delete(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class),
                    containsString("Object [person] TP1-TEST is referenced from other objects"));
        }
    }

    @Test
    public void delete_invalid_password() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/delete/person/PP1-TEST?password=invalid").request().delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void delete_no_password() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/delete/person/PP1-TEST").request().delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().readEntity(String.class),
                    containsString("Authorisation for [person] PP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"));
        }
    }

    @Test
    public void delete_post_not_allowed() {
        try {
            createResource(AUDIENCE, "whois/delete")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void delete_get_not_allowed() {
        try {
            createResource(AUDIENCE, "whois/delete")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    // update

    @Test
    public void update_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final RpslObject updatedObject = new RpslObjectFilter(PAULETH_PALTHEN).addAttributes(
                Lists.newArrayList(new RpslAttribute(AttributeType.REMARKS, "updated")));

        WhoisResources response = createResource(AUDIENCE, "whois/update/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(updatedObject)), MediaType.APPLICATION_XML), WhoisResources.class);

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
    public void update_without_query_params() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            createResource(AUDIENCE, "whois/update")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void update_post_not_allowed() {
        try {
            createResource(AUDIENCE, "whois/update/person/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(WhoisObjectMapper.map(Lists.newArrayList(PAULETH_PALTHEN)), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(HttpURLConnection.HTTP_BAD_METHOD));
        }
    }

    @Test
    public void modify_replace_attributes() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new WhoisModify(
                        new WhoisModify.Replace("address",
                                Lists.newArrayList(
                                        new Attribute("address", "P.O. Box 10096"),
                                        new Attribute("address", "1001 EB Amsterdam"),
                                        new Attribute("address", "The Netherlands")))), MediaType.APPLICATION_XML), WhoisResources.class);

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

        WhoisResources response = createResource(AUDIENCE, "whois/modify/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(whoisModify, MediaType.APPLICATION_JSON), WhoisResources.class);

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

        WhoisResources response = createResource(AUDIENCE, "whois/modify/person/PP1-TEST?password=test")
                .request("text/json")
                .post(Entity.entity(whoisModify, "text/json"), WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void modify_append_new_attributes() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new WhoisModify(
                        new WhoisModify.Add(
                                Lists.newArrayList(
                                        new Attribute("remarks", "updated")))), MediaType.APPLICATION_XML), WhoisResources.class);

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
    public void modify_add_new_attributes_from_index() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new WhoisModify(
                        new WhoisModify.Add(5,
                                Lists.newArrayList(
                                        new Attribute("remarks", "These remark lines will be added"),
                                        new Attribute("remarks", "starting from index 5 (line 6) !")))), MediaType.APPLICATION_XML), WhoisResources.class);

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
    public void modify_remove_all_attributes_of_type() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new WhoisModify(new WhoisModify.Remove("remarks")), MediaType.APPLICATION_XML), WhoisResources.class);

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
    public void modify_remove_attribute_at_index() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources response = createResource(AUDIENCE, "whois/modify/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(new WhoisModify(new WhoisModify.Remove(7)), MediaType.APPLICATION_XML), WhoisResources.class);

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
    public void modify_object_not_found() {
        try {
            createResource(AUDIENCE, "whois/modify/person/NONEXISTANT")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(new WhoisModify(new WhoisModify.Remove(7)), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void modify_invalid_object_type() {
        try {
            createResource(AUDIENCE, "whois/modify/invalid/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(new WhoisModify(new WhoisModify.Remove(7)), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException ignored) {
            // expected
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/versions/AS102")
                .request(MediaType.APPLICATION_XML)
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

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/versions/AS102")
                .request(MediaType.APPLICATION_XML)
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

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/versions/AS102")
                .request(MediaType.APPLICATION_JSON)
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

        final List<WhoisVersion> versions = createResource(AUDIENCE, "whois/versions/AS102")
                .request(MediaType.APPLICATION_XML)
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
            createResource(AUDIENCE, "whois/versions/AS102")
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
            createResource(AUDIENCE, "whois/version/2/AS102").request().get(String.class);
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/version/1/AS102")
                .request(MediaType.APPLICATION_XML)
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/version/1/AS102")
                .request(MediaType.APPLICATION_JSON)
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
            createResource(AUDIENCE, "whois/version/1/AS102")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException ignored) {
            // expected
        }
    }

    // schema

    @Test
    @Ignore
    public void schema_int() {
        final String response = createResource(AUDIENCE, "whois/xsd/int-docs/whois-resources.xsd")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);
        assertThat(response, containsString("<xs:element name=\"whois-resources\">"));
    }

    @Test
    @Ignore
    public void schema_ext() {
        final String response = createResource(AUDIENCE, "whois/xsd/ext-docs/whois-resources.xsd")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);
        assertThat(response, containsString("<xs:element name=\"whois-resources\">"));
    }

    // response format

    @Test
    public void lookup_accept_application_xml() {
        final String response = createResource(AUDIENCE, "whois/lookup/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);
        assertThat(response, containsString("<?xml version='1.0' encoding='UTF-8'?>"));
        assertThat(response, containsString("<whois-resources>"));
    }

    @Test
    public void lookup_accept_text_xml() {
        final String response = createResource(AUDIENCE, "whois/lookup/test/person/TP1-TEST")
                .request("text/xml")
                .get(String.class);
        assertThat(response, containsString("<?xml version='1.0' encoding='UTF-8'?>"));
        assertThat(response, containsString("<whois-resources>"));
    }

    @Test
    public void lookup_accept_application_json() {
        final String response = createResource(AUDIENCE, "whois/lookup/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertThat(response, containsString("\"whois-resources\""));
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"xlink:type\""));
        assertThat(response, containsString("\"xlink:href\""));
    }

    @Test
    public void lookup_accept_text_json() {
        final String response = createResource(AUDIENCE, "whois/lookup/test/person/TP1-TEST")
                .request("text/json")
                .get(String.class);
        assertThat(response, containsString("\"whois-resources\""));
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"xlink:type\""));
        assertThat(response, containsString("\"xlink:href\""));
    }

    @Test
    public void lookup_json_extension() {
        final String response = createResource(AUDIENCE, "whois/lookup/test/person/TP1-TEST.json")
                .request()
                .get(String.class);
        assertThat(response, containsString("\"whois-resources\""));
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"xlink:type\""));
        assertThat(response, containsString("\"xlink:href\""));
    }

    @Test
    public void update_json_request_and_response_content() {
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

        final String response = createResource(AUDIENCE, "whois/update/mntner/OWNER-MNT?password=test")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .put(Entity.entity(update, MediaType.APPLICATION_JSON), String.class);

        assertThat(response, containsString("\"whois-resources\""));
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"dbtest@ripe.net 20120101\""));
    }

    @Test
    public void modify_json_request_and_response_content() {
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

        final String response = createResource(AUDIENCE, "whois/modify/mntner/OWNER-MNT?password=test")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(Entity.entity(update, MediaType.APPLICATION_JSON), String.class);

        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"name\" : \"remarks\""));
        assertThat(response, containsString("\"value\" : \"updated\""));
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

        final String response = createResource(AUDIENCE, "whois/lookup/test/mntner/TEST-MNT")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, not(containsString("\u001b")));
        assertThat(response, not(containsString("<b>")));
        assertThat(response, not(containsString("&#x0;")));
        assertThat(response, not(containsString("<!--")));
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        final WhoisObject autnum = whoisResources.getWhoisObjects().get(0);
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

        final WhoisObject person = whoisResources.getWhoisObjects().get(1);
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
    public void search_accept_json() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final RpslObject rpslObject = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(rpslObject.getKey(), is(ciString("TP1-TEST")));
    }

    @Test
    public void search_json_extension() {
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search.json?query-string=TP1-TEST&source=TEST")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final RpslObject rpslObject = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(rpslObject.getKey(), is(ciString("TP1-TEST")));
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
                .request(MediaType.APPLICATION_XML)
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
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Invalid option 'h'"));
        }
    }

    @Test
    public void search_invalid_flag() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=LP1-TEST&source=TEST&flags=q")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Disallowed option 'q'"));
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

        final WhoisResources whoisResources = createResource(AUDIENCE,
                "whois/lookup/TEST/aut-num/AS102?include-tag=foobar&include-tag=unref")
                .request(MediaType.APPLICATION_XML)
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
                .request(MediaType.APPLICATION_XML)
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
                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("tech-c", "TP1-TEST", null, "person-role", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/person-role/TP1-TEST")),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://apps.db.ripe.net/whois/lookup/test/mntner/OWNER-MNT")),
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
            createResource(AUDIENCE,
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

        final WhoisResources whoisResources = createResource(AUDIENCE,
                "whois/search?source=TEST&query-string=AS102&exclude-tag=other&include-tag=unref&include-tag=foobar")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getTags(), contains(
                new WhoisTag("foobar", "foobar"),
                new WhoisTag("unref", "28")));
        assertThat(whoisObject.getAttributes(), contains(
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
    public void search_no_sources_given() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=AS102")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Argument 'source' is missing, you have to specify a valid RIR source for your search request"));
        }
    }

    @Test
    public void search_no_querystring_given() {
        try {
            createResource(AUDIENCE, "whois/search?source=TEST")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException ignored) {
            // expected
        }
    }

    @Test
    public void search_invalid_source() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=AS102&source=INVALID")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is("Invalid source 'INVALID'"));
            assertThat(response, not(containsString("Caused by:")));
        }
    }

    @Test
    public void grs_search_invalid_source() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=AS102&source=INVALID")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is("Invalid source 'INVALID'"));
            assertThat(response, not(containsString("Caused by:")));
        }
    }

    @Test
    public void search_multiple_sources() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&source=RIPE")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException ignored) {
            // expected
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

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=AS102&source=TEST&type-filter=aut-num,as-block")
                .request(MediaType.APPLICATION_XML)
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
    public void search_inverse() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&inverse-attribute=admin-c,tech-c")
                .request(MediaType.APPLICATION_XML)
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
        final WhoisResources whoisResources = createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&flags=BrCx")
                .request(MediaType.APPLICATION_XML)
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
            createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=TEST&flags=kq")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Disallowed option 'k'"));
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
                .request(MediaType.APPLICATION_XML)
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
    public void search_parameters_are_returned() {
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
                .request(MediaType.APPLICATION_XML)
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
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            assertThat(e.getResponse().readEntity(String.class), not(containsString("Caused by:")));
        }
    }

    @Ignore("TODO: [ES] don't set the content-type on an error response")
    @Test
    public void search_dont_set_content_type_on_error() {
        try {
            createResource(AUDIENCE, "whois/search?query-string=TP1-TEST&source=INVALID")
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, containsString("Invalid source 'INVALID'"));
            assertThat(response, not(containsString("Caused by:")));
            assertThat(e.getResponse().getHeaders().get("Content-Type"), not(contains((Object)"application/xml")));
        }
    }

    // helper methods

    @Override
    protected WebTarget createResource(final Audience audience, final String path) {
        return client.target(String.format("http://localhost:%s/%s", getPort(audience), path));
    }
}
