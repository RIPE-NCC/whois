package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_ANY_MNT;
import static net.ripe.db.whois.api.rest.WhoisRestApiKeyAuthTestIntegration.getBasicAuthHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class WhoisRestServicePasswdNotSupportedTestIntegration extends AbstractHttpsIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    public static final String TEST_PERSON_STRING = """
            person:         Test Person
            address:        Singel 258
            phone:          +31 6 12345678
            nic-hdl:        TP1-TEST
            mnt-by:         OWNER-MNT
            source:         TEST
            """;

    private static final String OWNER_MNT = """
            mntner:      OWNER-MNT
            descr:       Owner Maintainer
            admin-c:     TP1-TEST
            upd-to:      noreply@ripe.net
            auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
            auth:        SSO person@net.net
            mnt-by:      OWNER-MNT
            source:      TEST
            """;


    @BeforeAll
    public static void setUp() {
        System.setProperty("md5.password.supported", "false");

        System.setProperty("apikey.authenticate.enabled","true");
        System.setProperty("apikey.max.scope","2");
    }

    @AfterAll
    public static void clearProperties() {
        System.clearProperty("md5.password.supported");

        System.clearProperty("apikey.authenticate.enabled");
        System.clearProperty("apikey.max.scope");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObjects(RpslObject.parse(TEST_PERSON_STRING), RpslObject.parse(OWNER_MNT));
    }

    // Lookup
    @Test
    public void lookup_mntner_unfilter_with_password_filtered() {
        final WhoisResources whoisResources =
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test&unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().getFirst();
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null, null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_mntner_unfilter_with_apikey_unfiltered() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/mntner/OWNER-MNT?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().getFirst();
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", null, null, null, null)));
    }

    @Test
    public void lookup_mntner_unfilter_with_both_apikey_and_password_unfiltered() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/mntner/OWNER-MNT?password=test&unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().getFirst();
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", null, null, null, null)));
    }

    // POST
    @Test
    public void create_person_with_password_error() {

        final RpslObject createPerson = RpslObject.parse("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """);

        final NotAuthorizedException notAuthorizedException = assertThrows(NotAuthorizedException.class, () ->
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(map(createPerson), MediaType.APPLICATION_JSON), WhoisResources.class)
        );

        final WhoisResources whoisResources = notAuthorizedException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(2));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), containsString("""
                Authorisation for [person] PP1-TEST failed
                using "mnt-by:"
                not authenticated by: OWNER-MNT"""));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is(
                "MD5 hashed password authentication has been ignored because is not longer supported."));
    }

    @Test
    public void create_new_mntner_with_password_error() {

        final RpslObject createMd5Mntner = RpslObject.parse("""
                mntner:      MD5-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                auth:        SSO person@net.net
                mnt-by:      MD5-MNT
                source:      TEST
                """);

        final NotAuthorizedException notAuthorizedException = assertThrows(NotAuthorizedException.class, () ->
                RestTest.target(getPort(), "whois/test/mntner?password=test")
                        .request()
                        .post(Entity.entity(map(createMd5Mntner), MediaType.APPLICATION_JSON), WhoisResources.class)
        );

        final WhoisResources whoisResources = notAuthorizedException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(3));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), is("""
                Authorisation for [mntner] MD5-MNT failed
                using "mnt-by:"
                not authenticated by: MD5-MNT"""));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is(
                "MD5 hashed password authentication is deprecated. Please switch to an alternative authentication method."));
        assertThat(whoisResources.getErrorMessages().get(2).toString(), is(
                "MD5 hashed password authentication has been ignored because is not longer supported."));
    }

    @Test
    public void create_new_password_mntner_with_apikeys_error() {

        final RpslObject createMd5Mntner = RpslObject.parse("""
                mntner:      MD5-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                auth:        SSO person@net.net
                mnt-by:      MD5-MNT
                source:      TEST
                """);

        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () ->
                SecureRestTest.target(getSecurePort(), "whois/test/mntner")
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                        .post(Entity.entity(map(createMd5Mntner), MediaType.APPLICATION_JSON), WhoisResources.class)
        );

        final WhoisResources whoisResources = badRequestException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(1));

        assertThat(whoisResources.getErrorMessages().getFirst().toString(), is(
                "MD5 hashed password authentication is deprecated. Please switch to an alternative authentication method."));
    }

    @Test
    public void create_new_sso_mntner_with_apikeys_succeed() {

        final RpslObject createMd5Mntner = RpslObject.parse("""
                mntner:      MD5-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        SSO test@ripe.net
                auth:        SSO person@net.net
                mnt-by:      MD5-MNT
                source:      TEST
                """);
        
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/mntner")
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                        .post(Entity.entity(map(createMd5Mntner), MediaType.APPLICATION_JSON), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void create_person_with_apikey_succeed() {

        final RpslObject createPerson = RpslObject.parse("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """);

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .post(Entity.entity(map(createPerson), MediaType.APPLICATION_JSON), WhoisResources.class);


        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void create_person_with_both_apikey_and_password_succeed() {

        final RpslObject createPerson = RpslObject.parse("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """);

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person?password=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .post(Entity.entity(map(createPerson), MediaType.APPLICATION_JSON), WhoisResources.class);


        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }


    // PUT

    @Test
    public void update_new_mntner_with_password_error() {

        final RpslObject updateMntner = RpslObject.parse("""
                mntner:      OWNER-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """);

        final NotAuthorizedException notAuthorizedException = assertThrows(NotAuthorizedException.class, () ->
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test")
                        .request()
                        .put(Entity.entity(map(updateMntner), MediaType.APPLICATION_JSON), WhoisResources.class)
        );

        final WhoisResources whoisResources = notAuthorizedException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(3));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), is("""
                Authorisation for [mntner] OWNER-MNT failed
                using "mnt-by:"
                not authenticated by: OWNER-MNT, OWNER-MNT"""));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is(
                "MD5 hashed password authentication is deprecated. Please switch to an alternative authentication method."));
        assertThat(whoisResources.getErrorMessages().get(2).toString(), is(
                "MD5 hashed password authentication has been ignored because is not longer supported."));
    }

    @Test
    public void update_new_password_mntner_with_apikeys_error() {

        final RpslObject updateMntner = RpslObject.parse("""
                mntner:      OWNER-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """);

        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () ->
                SecureRestTest.target(getSecurePort(), "whois/test/mntner/OWNER-MNT")
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                        .put(Entity.entity(map(updateMntner), MediaType.APPLICATION_JSON), WhoisResources.class)
        );

        final WhoisResources whoisResources = badRequestException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(1));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), is(
                "MD5 hashed password authentication is deprecated. Please switch to an alternative authentication method."));
    }

    @Test
    public void update_new_sso_mntner_with_apikeys_succeed() {

        final RpslObject updateMntner = RpslObject.parse("""
                mntner:      OWNER-MNT
                descr:       Owner Maintainer
                admin-c:     TP1-TEST
                upd-to:      noreply@ripe.net
                auth:        SSO test@ripe.net
                auth:        SSO person@net.net
                mnt-by:      OWNER-MNT
                source:      TEST
                """);

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/mntner/OWNER-MNT")
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                        .put(Entity.entity(map(updateMntner), MediaType.APPLICATION_JSON), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void update_person_with_password_error() {

        initObjects();
        final RpslObject updatePerson = RpslObject.parse("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   updated remark
                source:    TEST
                """);

        final NotAuthorizedException notAuthorizedException = assertThrows(NotAuthorizedException.class, () ->
                RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                        .request()
                        .put(Entity.entity(map(updatePerson), MediaType.APPLICATION_JSON), WhoisResources.class)
        );

        final WhoisResources whoisResources = notAuthorizedException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(2));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), containsString("""
                Authorisation for [person] PP1-TEST failed
                using "mnt-by:"
                not authenticated by: OWNER-MNT"""));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is(
                "MD5 hashed password authentication has been ignored because is not longer supported."));
    }

    @Test
    public void update_person_with_apikey_succeed() {

        initObjects();

        final RpslObject updatePerson = RpslObject.parse("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   updated remark
                source:    TEST
                """);

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person/PP1-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .put(Entity.entity(map(updatePerson), MediaType.APPLICATION_JSON), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void update_person_with_both_apikey_and_password_succeed() {

        initObjects();

        final RpslObject updatePerson = RpslObject.parse("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   updated remark
                source:    TEST
                """);

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person/PP1-TEST?password=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .put(Entity.entity(map(updatePerson), MediaType.APPLICATION_JSON), WhoisResources.class);


        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    // DELETE

    @Test
    public void delete_mntner_with_password_error() {

        final NotAuthorizedException notAuthorizedException = assertThrows(NotAuthorizedException.class, () ->
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test")
                        .request()
                        .delete(WhoisResources.class)
        );

        final WhoisResources whoisResources = notAuthorizedException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(3));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), is("""
                Authorisation for [mntner] OWNER-MNT failed
                using "mnt-by:"
                not authenticated by: OWNER-MNT, OWNER-MNT"""));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is("Object [mntner] OWNER-MNT is referenced from other objects"));
        assertThat(whoisResources.getErrorMessages().get(2).toString(), is(
                "MD5 hashed password authentication has been ignored because is not longer supported."));
    }

    @Test
    public void delete_existing_password_mntner_with_apikeys_no_auth_error() {

        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () ->
                SecureRestTest.target(getSecurePort(), "whois/test/mntner/OWNER-MNT")
                        .request()
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                        .delete(WhoisResources.class)
        );

        final WhoisResources whoisResources = badRequestException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(1));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), is("Object [mntner] OWNER-MNT is referenced from other objects"));
    }

    @Test
    public void delete_person_with_password_error() {

        initObjects();

        final NotAuthorizedException notAuthorizedException = assertThrows(NotAuthorizedException.class, () ->
                RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                        .request()
                        .delete(WhoisResources.class)
        );

        final WhoisResources whoisResources = notAuthorizedException.getResponse().readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages().size(), is(2));
        assertThat(whoisResources.getErrorMessages().getFirst().toString(), containsString("""
                Authorisation for [person] PP1-TEST failed
                using "mnt-by:"
                not authenticated by: OWNER-MNT"""));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is(
                "MD5 hashed password authentication has been ignored because is not longer supported."));
    }

    @Test
    public void delete_person_with_apikey_succeed() {

        initObjects();

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person/PP1-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void delete_person_with_both_apikey_and_password_succeed() {

        initObjects();

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person/PP1-TEST?password=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .delete(WhoisResources.class);


        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    // Helper methods


    private void initObjects(){
        databaseHelper.addObject("""
                person:    Pauleth Palthen
                address:   Singel 258
                phone:     +31-1234567890
                e-mail:    noreply@ripe.net
                mnt-by:    OWNER-MNT
                nic-hdl:   PP1-TEST
                remarks:   remark
                source:    TEST
                """);
    }

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
