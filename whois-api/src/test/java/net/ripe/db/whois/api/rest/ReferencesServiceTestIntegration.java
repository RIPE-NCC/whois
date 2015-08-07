package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class ReferencesServiceTestIntegration extends AbstractIntegrationTest {

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
                "role:        Test Role\n" +
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
                "role:        Test Role\n" +
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
        try{
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

    @Ignore
    @Test
    public void delete_response_contains_error_message() {

        try {
            RestTest.target(getPort(), "whois/references/TEST/person/TP1-TEST")
                    .request()
                    .delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "TP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    // helper methods

    private boolean objectExists(final ObjectType objectType, final String primaryKey) {
        try {
            RestTest.target(getPort(),
                String.format("whois/TEST/%s/%s", objectType.getName(), primaryKey))
                .request()
                .get(WhoisResources.class);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }




}
