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
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_INVALID_API_KEY;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_NO_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_TEST_NO_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_TEST_TEST_MNT;
import static net.ripe.db.whois.api.rest.WhoisRestBasicAuthTestIntegration.getBasicAuthenticationHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class WhoisRestApiKeyAuthTestIntegration extends AbstractHttpsIntegrationTest {

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

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("apikey.authenticate.enabled","true");
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("apikey.authenticate.enabled");
        System.clearProperty("apikey.public.key.url");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        databaseHelper.addObject(TEST_IRT);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void create_failed_with_basic_auth_api_key_no_https() {

        final Response response =  RestTest.target(getPort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NO_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.UPGRADE_REQUIRED_426));
        assertThat(response.readEntity(String.class), containsString("HTTPS required for Basic authorization"));
    }

    @Test
    public void request_failed_with_basic_auth_api_key_illegal_query_param() {

        final Response response =  SecureRestTest.target(getSecurePort(), "whois/test/person?" + ApiKeyUtils.APIKEY_ACCESS_QUERY_PARAM + "=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NO_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void request_failed_with_basic_auth_api_key_illegal_bearer_header() {

        final Response response =  SecureRestTest.target(getSecurePort(), "whois/test/person?" + ApiKeyUtils.APIKEY_ACCESS_QUERY_PARAM + "=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer eFR0cm9lZUpWYWlmSWNQR1BZUW5kSmhnOmp5akhYR2g4WDFXRWZyc2M5SVJZcUVYbw==")
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }


    @Test
    public void lookup_correct_api_key_with_sso_and_unfiltered() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NO_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, false);
    }

    @Test
    public void lookup_correct_api_key_no_sso_in_mnt_by_and_filtered() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_NO_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_incorrect_api_key_and_filtered() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_API_KEY))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_correct_api_key_with_mnt_and_sso_and_unfiltered() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, false);
    }

    @Test
    public void lookup_correct_api_key_with_mnt_and_no_sso_and_filtered() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_correct_api_key_with_different_mnt_and_filtered() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_correct_api_key_with_different_mnt_but_same_sso_and_filtered() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      TEST-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "auth:        SSO person@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void delete_object_with_apikey_no_mnt_with_sso() {
        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NO_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void delete_object_with_apikey_with_mnt_with_sso() {
        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void delete_object_with_invalid_apikey() {
        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_API_KEY))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_apikey_different_mnt_fails() {
        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_apikey_different_mnt_same_sso_fails() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      TEST-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "auth:        SSO person@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_apikey_same_mnt_different_sso_fails() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void create_succeeds_with_apiKey_no_mnt_with_sso() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NO_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person?accessKey=l6lRZgvOFIphjiGwtCGuLwqw",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_succeeds_with_basic_auth_and_no_api_key() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person?password=incorrect1&password=Incorrect2")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthenticationHeader("test", "test"))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);

        assertPersonObject(whoisResources, object);
    }


    private static void assertIrt(final WhoisObject whoisObject, final boolean isFIltered) {
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("irt", "irt-test"),
                new Attribute("address", "RIPE NCC"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", isFIltered ? "MD5-PW" : "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", isFIltered ? "Filtered" :"test", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", isFIltered ? "Filtered" : null, null, null, null)));
    }

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

    private static String getBasicAuthHeader(final String basicAuth) {
        return StringUtils.joinWith(" ","Basic ", basicAuth);
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
