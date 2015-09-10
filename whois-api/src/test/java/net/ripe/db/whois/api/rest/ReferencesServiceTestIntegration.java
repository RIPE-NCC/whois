package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class ReferencesServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    @Before
    public void setup() {
        databaseHelper.addObject(
                "role:          dummy role\n" +
                "nic-hdl:       DR1-TEST");
        databaseHelper.addObject(
                "person:        Test Person\n" +
                "nic-hdl:       TP1-TEST");
        databaseHelper.addObject(
                "role:          Test Role\n" +
                "nic-hdl:       TR1-TEST");
        databaseHelper.addObject(
                "mntner:        OWNER-MNT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.updateObject(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
    }

    // CREATE

    @Test
    public void create_person_mntner_pair_success() {
        final WhoisResources whoisResources =
                createWhoisResources(
                    RpslObject.parse(
                        "person:    Some Person\n" +
                        "address:   Amsterdam\n" +
                        "phone:     +3161234\n" +
                        "nic-hdl:   AUTO-1\n" +
                        "mnt-by:    NEW-UHUUU9999-MNT\n" +
                        "source:    TEST"),
                    RpslObject.parse(
                        "mntner:    NEW-UHUUU9999-MNT\n" +
                        "descr:     Maintainer\n" +
                        "admin-c:   AUTO-1\n" +
                        "upd-to:    person@net.net\n" +
                        "auth:      SSO person@net.net\n" +
                        "mnt-by:    NEW-UHUUU9999-MNT\n" +
                        "source:    TEST"));

        final WhoisResources response = RestTest.target(getPort(), "whois/references/TEST")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        // TODO: is this deliberate?
        assertThat(response.getErrorMessages(), hasSize(1));
        assertThat(response.getErrorMessages().get(0).toString(), is("Referenced role object DR1-TEST from mntner: NEW-UHUUU9999-MNT is missing mandatory attribute \"mnt-by:\""));

        assertThat(response.getWhoisObjects(), hasSize(2));

        final WhoisObject personObject = getWhoisObject(response, "person");
        final String nicHdl = getAttribute(personObject, "nic-hdl");
        assertThat(nicHdl, not(equalToIgnoringCase("AUTO-1")));
        final String mntBy = getAttribute(personObject, "mnt-by");
        assertThat(mntBy, equalToIgnoringCase("NEW-UHUUU9999-MNT"));

        final WhoisObject mntnerObject = getWhoisObject(response, "mntner");
        assertThat(nicHdl, equalToIgnoringCase(getAttribute(mntnerObject, "admin-c")));
    }

    @Test
    public void create_person_mntner_pair_fail() {
        final WhoisResources whoisResources =
            createWhoisResources(
                RpslObject.parse(
                    "person:    Some Person\n" +
                    "address:   Amsterdam\n" +
                    "phone:     +3161234\n" +
                    "nic-hdl:   AUTO-1\n" +
                    "mnt-by:    OWNER-MNT\n" +
                    "source:    TEST"),
                RpslObject.parse(
                    "mntner:    NEW-UHUUU9999-MNT\n" +
                    "descr:     Maintainer\n" +
                    "admin-c:   AUTO-1\n" +
                    "upd-to:    person@net.net\n" +
                    "auth:      SSO person@net.net\n" +
                    "mnt-by:    NEW-UHUUU9999-MNT\n" +
                    "source:    TEST"));

        final Response response = RestTest.target(getPort(), "whois/references/TEST")
            .request()
            .cookie("crowd.token_key", "valid-token")
            .post(Entity.entity(whoisResources, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus(), equalTo(401));

        assertThat(objectExists(ObjectType.MNTNER, "NEW-UHUUU9999-MNT"), is(false));
    }

    // READ

    @Test
    public void lookup_mntner_references_success() {
        final ReferencesService.Reference response = RestTest.target(getPort(), "whois/references/TEST/mntner/OWNER-MNT")
            .request()
            .get(ReferencesService.Reference.class);

        assertThat(response.getPrimaryKey(), is("OWNER-MNT"));
        assertThat(response.getObjectType(), is("mntner"));

        // TODO: owner-mnt is not listed as a self-reference
        final List<ReferencesService.Reference> incomingReferences = response.getIncoming();
        assertThat(incomingReferences, hasSize(1));
        assertThat(incomingReferences.get(0).getPrimaryKey(), is("TP1-TEST"));
        assertThat(incomingReferences.get(0).getObjectType(), is("person"));
    }

    @Test
    public void lookup_mntner_references_success_xml() {
        final String response = RestTest.target(getPort(), "whois/references/TEST/mntner/OWNER-MNT.xml")
            .request()
            .get(String.class);

        assertThat(response, is(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<references>" +
            "<primaryKey>OWNER-MNT</primaryKey>" +
            "<objectType>mntner</objectType>" +
            "<incoming>" +
            "<references>" +
            "<primaryKey>TP1-TEST</primaryKey>" +
            "<objectType>person</objectType>" +
            "<incoming>" +
            "<references>" +
            "<primaryKey>OWNER-MNT</primaryKey>" +
            "<objectType>mntner</objectType>" +
            "<incoming/>" +
            "<outgoing/>" +
            "</references>" +
            "</incoming>" +
            "<outgoing/>" +
            "</references>" +
            "</incoming>" +
            "<outgoing/>" +
            "</references>"));
    }

    @Test
    public void lookup_mntner_references_success_json() {
        final String response = RestTest.target(getPort(), "whois/references/TEST/mntner/OWNER-MNT.json")
            .request()
            .get(String.class);

        assertThat(response, is(
                "{\n" +
                "  \"primaryKey\" : \"OWNER-MNT\",\n" +
                "  \"objectType\" : \"mntner\",\n" +
                "  \"incoming\" : [ {\n" +
                "    \"primaryKey\" : \"TP1-TEST\",\n" +
                "    \"objectType\" : \"person\",\n" +
                "    \"incoming\" : [ {\n" +
                "      \"primaryKey\" : \"OWNER-MNT\",\n" +
                "      \"objectType\" : \"mntner\"\n" +
                "    } ]\n" +
                "  } ]\n" +
                "}"));
    }

    @Test
    public void lookup_mntner_references_invalid_object_type() {
        try {
            RestTest.target(getPort(), "whois/references/TEST/invalid/OWNER-MNT")
                .request()
                .get(String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            assertThat(response.getErrorMessages(), contains(new ErrorMessage(new Message(Messages.Type.ERROR, "Invalid object type: invalid"))));
        }
    }

    @Test
    public void lookup_mntner_references_invalid_primary_key() {
        try {
            RestTest.target(getPort(), "whois/references/TEST/mntner/invalid")
                .request()
                .get(String.class);
            fail();
        } catch (NotFoundException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            assertThat(response.getErrorMessages(), contains(new ErrorMessage(new Message(Messages.Type.ERROR, "Not Found"))));
        }
    }

    // UPDATE

    @Test
    public void update_create_multiple_objects_successfully() {
        final RpslObject firstPerson = RpslObject.parse(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        final RpslObject secondPerson = RpslObject.parse(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP3-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        final RpslObject thirdPerson = RpslObject.parse(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP4-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");

        final WhoisResources response = RestTest.target(getPort(), "whois/references")
                .queryParam("password", "test")
                .request()
                .put(Entity.entity(mapRpslObjects(firstPerson, secondPerson, thirdPerson), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        assertThat(response.getErrorMessages(), hasSize(0));
        assertThat(response.getWhoisObjects(), hasSize(3));

        assertThat(objectExists(ObjectType.PERSON, "TP2-TEST"), is(true));
        assertThat(objectExists(ObjectType.PERSON, "TP3-TEST"), is(true));
        assertThat(objectExists(ObjectType.PERSON, "TP4-TEST"), is(true));
    }

    @Test
    public void update_create_multiple_objects_one_fails() {
        final RpslObject firstPerson = RpslObject.parse(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        final RpslObject secondPerson = RpslObject.parse(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         INVALID\n" +    // invalid syntax
                "nic-hdl:       TP3-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        final RpslObject thirdPerson = RpslObject.parse(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP4-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");

        final WhoisResources response = RestTest.target(getPort(), "whois/references")
                .queryParam("password", "test")
                .request()
                .put(Entity.entity(mapRpslObjects(firstPerson, secondPerson, thirdPerson), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        RestTest.assertErrorCount(response, 1);
        RestTest.assertErrorMessage(response, 0, "Error", "Syntax error in %s", "INVALID");
        assertThat(response.getWhoisObjects(), hasSize(3));

        assertThat(objectExists(ObjectType.PERSON, "TP2-TEST"), is(false));
        assertThat(objectExists(ObjectType.PERSON, "TP3-TEST"), is(false));
        assertThat(objectExists(ObjectType.PERSON, "TP4-TEST"), is(false));
    }


    // DELETE


    // OWNER-MNT <- TP1-TEST
    @Test
    public void delete_mntner_success() {
        RestTest.target(getPort(), "whois/references/TEST/mntner/OWNER-MNT?password=test")
            .request()
            .delete();

        assertThat(objectExists(ObjectType.MNTNER, "OWNER-MNT"), is(false));
        assertThat(objectExists(ObjectType.PERSON, "TP1-TEST"), is(false));
    }

    // TP1-TEST <- OWNER-MNT
    @Test
    public void delete_person_success() {
        RestTest.target(getPort(), "whois/references/TEST/person/TP1-TEST?password=test")
            .request()
            .delete();

        assertThat(objectExists(ObjectType.MNTNER, "OWNER-MNT"), is(false));
        assertThat(objectExists(ObjectType.PERSON, "TP1-TEST"), is(false));
    }

    @Test
    public void delete_object_multiple_references_succeeds() {
        databaseHelper.addObject(
                "person:        Test Person2\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject(
                "role:          Test Role\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TR2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");

        RestTest.target(getPort(), "whois/references/TEST/mntner/OWNER-MNT?password=test")
                .request()
                .delete();

        assertThat(objectExists(ObjectType.MNTNER, "OWNER-MNT"), is(false));
        assertThat(objectExists(ObjectType.PERSON, "TP1-TEST"), is(false));
        assertThat(objectExists(ObjectType.PERSON, "TP2-TEST"), is(false));
        assertThat(objectExists(ObjectType.ROLE, "TR2-TEST"), is(false));
    }

    @Test
    public void delete_object_with_outgoing_references_only() {
        databaseHelper.addObject(
                "role:          Test Role\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TR2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");

        RestTest.target(getPort(), "whois/references/TEST/role/TR2-TEST?password=test")
                .request()
                .delete();

        assertThat(objectExists(ObjectType.MNTNER, "OWNER-MNT"), is(true));
        assertThat(objectExists(ObjectType.ROLE, "TR2-TEST"), is(false));
    }

    @Test
    public void delete_non_mntner_or_role() {
        databaseHelper.addObject(
                "organisation:    ORG-TO1-TEST\n" +
                "org-type:        other\n" +
                "org-name:        First Org\n" +
                "address:         RIPE NCC\n" +
                "e-mail:          dbtest@ripe.net\n" +
                "mnt-by:          OWNER-MNT\n" +
                "source:          TEST");
        try {
            RestTest.target(getPort(), "whois/references/TEST/organisation/ORG-TO1-TEST?password=test")
                    .request()
                    .delete(String.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Object type ORGANISATION is not supported.");

            assertThat(objectExists(ObjectType.MNTNER, "OWNER-MNT"), is(true));
            assertThat(objectExists(ObjectType.ORGANISATION, "ORG-TO1-TEST"), is(true));
        }
    }

    // OWNER-MNT <- TP1-TEST <- ANOTHER-MNT
    @Test
    public void delete_mntner_fails_person_referenced_from_another_mntner() {
        databaseHelper.addObject(
                "mntner:        ANOTHER-MNT\n" +
                "descr:         Another Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        ANOTHER-MNT\n" +
                "source:        TEST");

        final Response response = RestTest.target(getPort(), "whois/references/TEST/mntner/OWNER-MNT")
                                    .request()
                                    .delete();

        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.readEntity(String.class), containsString("Referencing object TP1-TEST itself is referenced by ANOTHER-MNT"));
    }

    @Test
    public void delete_mntner_fails_because_of_authorisation() {
        final Response response = RestTest.target(getPort(), "whois/references/TEST/mntner/OWNER-MNT")
                .request()
                .delete();

        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_person_fails_because_of_authorisation() {
        final Response response = RestTest.target(getPort(), "whois/references/TEST/person/TP1-TEST")
                .request()
                .delete();

        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

    @Ignore("TODO: [ES] include error messages in response")
    @Test
    public void delete_response_contains_error_message() {
        try {
            RestTest.target(getPort(), "whois/references/TEST/person/TP1-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "TP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    // helper methods

    private List<RpslObject> lookup(final ObjectType objectType, final String primaryKey) {
        final WhoisResources response = RestTest.target(getPort(),
                                            String.format("whois/TEST/%s/%s", objectType.getName(), primaryKey))
                                            .request()
                                            .get(WhoisResources.class);
        return mapWhoisObjects(response.getWhoisObjects());
    }

    private boolean objectExists(final ObjectType objectType, final String primaryKey) {
        try {
            lookup(objectType, primaryKey);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private WhoisResources mapRpslObjects(final RpslObject... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

    private List<RpslObject> mapWhoisObjects(final List<WhoisObject> whoisResources) {
        return whoisObjectMapper.mapWhoisObjects(whoisResources, FormattedClientAttributeMapper.class);
    }

    private String getAttribute(final WhoisObject whoisObject, final String attributeName) {
        for(Attribute attribute : whoisObject.getAttributes()) {
            if(attribute.getName().equalsIgnoreCase(attributeName)){
                 return attribute.getValue();
            }
        }

        throw new IllegalArgumentException("Couldn't find " + attributeName);
    }

    private WhoisObject getWhoisObject(final WhoisResources whoisResources, final String objectType) {
        for(WhoisObject object : whoisResources.getWhoisObjects()) {
            if (objectType.equalsIgnoreCase(object.getType())) {
                return object;
            }
        }

        throw new IllegalArgumentException("Couldn't find " + objectType);
    }
}
