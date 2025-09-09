package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Base64;

import static jakarta.ws.rs.core.Response.Status.OK;
import static net.ripe.db.whois.api.rest.HttpBasicAuthResponseFilter.BASIC_CHARSET_ISO_8859_1_LATIN_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class WhoisRestBasicAuthTestIntegration extends AbstractHttpsIntegrationTest {

    public static final String TEST_PERSON_STRING = "" +
            "person:         Test Person\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";

    public static final RpslObject TEST_PERSON = RpslObject.parse(TEST_PERSON_STRING);
    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
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
            "source:      TEST");

    private static final String TEST_ROLE_STRING = "" +
            "role:           Test Role\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TR1-TEST\n" +
            "admin-c:        TR1-TEST\n" +
            "abuse-mailbox:  abuse@test.net\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";
    private static final RpslObject TEST_ROLE = RpslObject.parse(TEST_ROLE_STRING);

    private static final RpslObject TEST_IRT = RpslObject.parse("" +
            "irt:          irt-test\n" +
            "address:      RIPE NCC\n" +
            "e-mail:       noreply@ripe.net\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "auth:         MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:       OWNER-MNT\n" +
            "source:       TEST\n");

    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private TestDateTimeProvider testDateTimeProvider;
    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void lookup_mntner_incorrect_basic_auth_password_without_unfiltered_param_is_fully_filtered() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/mntner/OWNER-MNT")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "incorrect"))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_correct_basic_auth_password_and_unfiltered() {
        databaseHelper.addObject(TEST_IRT);

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("irt", "irt-test"),
                new Attribute("address", "RIPE NCC"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));
    }

    @Test
    public void create_succeeds_with_basic_auth_no_password_query() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_succeeds_with_basic_auth_multiple_password_incorrect() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person?password=incorrect1&password=Incorrect2")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_succeeds_with_basic_auth_incorrect_multiple_password_correct() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person?password=incorrect1&password=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "incorrect"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }


    @Test
    public void create_failed_with_basic_auth_incorrect_www_authenticate_cookie() {
        final Response response = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "incorrect"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.UNAUTHORIZED_401));
        assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE), is(BASIC_CHARSET_ISO_8859_1_LATIN_1));
    }

    @Test
    public void create_failed_with_no_basic_auth_incorrect_password_no_www_authenticate_cookie() {
        final Response response = SecureRestTest.target(getSecurePort(), "whois/test/person?password=incorrect")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.UNAUTHORIZED_401));
        assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE), is(nullValue()));
    }

    @Test
    public void create_failed_with_basic_auth_no_https() {

        final Response response =  RestTest.target(getPort(), "whois/test/person")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.UPGRADE_REQUIRED_426));
        assertThat(response.readEntity(String.class), containsString("HTTPS required for Authorization Header"));
    }

    @Test
    public void create_success_no_basic_auth_no_WWW_authenticate_header() {
        final Response response = SecureRestTest.target(getSecurePort(), "whois/test/person?password=incorrect")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE), is(nullValue()));
    }

    @Test
    public void create_succeeds_with_correct_basic_auth_and_wrong_password_query() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person?password=incorrect")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person", getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void delete_object_with_outgoing_references_only_using_basic_auth() {
        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void create_succeeds_with_incorrect_basic_auth_and_correct_password_query() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person?password=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "incorrect"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person", getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }
    
    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

    public static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private static void assertPersonObject(WhoisResources whoisResources, WhoisObject object) {
        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("created", "2001-02-04T17:00:00Z"),
                new Attribute("last-modified", "2001-02-04T17:00:00Z"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }
}
