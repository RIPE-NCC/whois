package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.Scope;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.oauth.OAuthUtils;
import net.ripe.db.whois.common.oauth.OidcSession;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.acl.SSOResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.net.InetAddress;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.APIKEY_TO_CLAIMSET;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_EXPIRED;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INACTIVE_TOKEN;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_ISS_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_SIGNATURE_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_ISSUES_AT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_ANY_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_MULTIPLE_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_MULTIPLE_MNT_WITH_ANY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_NO_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_NULL_SCOPE;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_TEST_NO_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_TEST_TEST_MNT;
import static net.ripe.db.whois.api.OAuthTokenIntrospectDummy.convertToOidcJwt;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class WhoisRestOidcAuthTestIntegration extends WhoisRestServiceTestIntegration {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;
    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private SSOResourceConfiguration ssoResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;


    public static final String TEST_2ROLE_STRING = "" +
            "role:          Test Role\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TR2-TEST\n" +
            "e-mail:        test123@ripe.net\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST";


    private static final RpslObject TEST_2ROLE = RpslObject.parse(TEST_2ROLE_STRING);

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oidc.auth.enable","true");
        System.setProperty("oidc.session.client.id", APP_CLIENT_ID);
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("oidc.auth.enable");
        System.clearProperty("oidc.session.client.id");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.addObject(TEST_ROLE);
        databaseHelper.addObject(TEST_2ROLE);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @AfterEach
    public void reset() throws Exception {
        databaseHelper.clearAclTables();

        ipResourceConfiguration.reload();
        ssoResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();
    }

    @Test
    @Override
    public void update_person_with_invalid_token_fails() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        try {
            getWebTarget("whois/test/person/PP1-TEST",
                    "invalid-token", MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = RestTest.mapClientException(e);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
            RestTest.assertErrorMessage(whoisResources, 1, "Warning", "Invalid Bearer Token");
        }
    }

    @Test
    @Override
    public void delete_self_referencing_maintainer_with_sso_auth_attribute_invalid_token_authenticated_with_password_succeeds() {
        databaseHelper.addObject(SSO_AND_PASSWORD_MNT);

        final WhoisResources whoisResources = getWebTarget("whois/test/mntner/SSO-PASSWORD-MNT?password=test",
                "invalid-token", MediaType.APPLICATION_XML)
                .delete(WhoisResources.class);

        RestTest.assertErrorMessage(whoisResources, 1, "Warning", "Invalid Bearer Token");
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().getFirst().getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));

        try {
            databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-PASSWORD-MNT");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    @Override
    public void create_self_referencing_maintainer_sso_auth_only_invalid_token() {
        try {
            getWebTarget("whois/test/mntner",
                    "invalid", MediaType.APPLICATION_XML)
                    .post(Entity.entity(map(SSO_ONLY_MNT), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = RestTest.mapClientException(e);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "mntner", "SSO-ONLY-MNT", "mnt-by", "SSO-ONLY-MNT");
            RestTest.assertErrorMessage(whoisResources, 1, "Warning", "Invalid Bearer Token");
        }
    }

    @Test
    public void create_failed_with_bearer_token_no_https() {

        final Response response = RestTest.target(getPort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.UPGRADE_REQUIRED_426));
        assertThat(response.readEntity(String.class), containsString("HTTPS required for Authorization Header"));
    }


    @Test
    public void request_failed_with_bearer_token_illegal_query_param() {

        final Response response = getWebTarget("whois/test/person" + OAuthUtils.APIKEY_KEY_ID_QUERY_PARAM + "=test",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    //TODO: Move syncupdates to a different IT class

    @Test
    public void update_selfrefencing_maintainer_only_data_parameter_with_bearer_token() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";
        databaseHelper.addObject(mntner);


        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner + "\nremarks: updated"),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_tokeny() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = getWebTarget("whois/syncupdates/test", getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_fails_no_sso() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";


        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_fails_invalid() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";


        final String response = getWebTarget("whois/syncupdates/test", "Bearer invalid", MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Invalid Bearer Token"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_succeed_null_Scope() {

        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";


        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_NULL_SCOPE), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_multiple_scope_with_any() {
        // Scopes are set to ANY for Oidc
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";


        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT_WITH_ANY), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_with_apiKey_succeed_limit_exceed() {
        // Scopes are set to ANY
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
    }


    @Test
    public void create_mntner_only_data_parameter_with_apiKey_succeed_no_mnt_Scope() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";


        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_NO_MNT), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void lookup_correct_bearer_token_with_sso_and_unfiltered() {
        databaseHelper.addObject(TEST_IRT);
        final WhoisResources whoisResources = getWebTarget("whois/test/irt/irt-test?unfiltered",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().getFirst();
        assertIrt(whoisObject, false);
    }


    @Test
    public void lookup_correct_bearer_token_no_sso_in_mnt_by_and_filtered() {
        databaseHelper.addObject(TEST_IRT);
        final WhoisResources whoisResources = getWebTarget("whois/test/irt/irt-test?unfiltered",
                getBearerTokenForOidc(BASIC_AUTH_TEST_NO_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().getFirst();
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_incorrect_bearer_token_and_filtered() {
        databaseHelper.addObject(TEST_IRT);
        final WhoisResources whoisResources = getWebTarget("whois/test/irt/irt-test?unfiltered", "Bearer invalidToken", MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_correct_bearer_token_with_mnt_and_sso_and_unfiltered() {

        databaseHelper.addObject(TEST_IRT);
        final WhoisResources whoisResources = getWebTarget("whois/test/irt/irt-test?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, false);
    }

    @Test
    public void lookup_correct_bearer_token_with_multiple_mnt_scope_and_sso_and_unfiltered() {

        WhoisResources whoisResources = getWebTarget("whois/test/mntner/OWNER-MNT?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertSSOAttribute(whoisResources, "SSO person@net.net");

        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      TEST-MNT\n" +
                "descr:       TEST Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO person@net.net\n" +
                "mnt-by:      TEST-MNT\n" +
                "source:      TEST"));

        whoisResources = getWebTarget("whois/test/mntner/TEST-MNT?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertSSOAttribute(whoisResources, "SSO person@net.net");

    }

    @Test
    public void lookup_correct_bearer_token_with_multiple_mnt_scope_and_one_has_sso_and_other_not() {

        WhoisResources whoisResources = getWebTarget("whois/test/mntner/OWNER-MNT?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertSSOAttribute(whoisResources, "SSO person@net.net");


        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      TEST-MNT\n" +
                "descr:       TEST Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@ripe.net\n" +
                "mnt-by:      TEST-MNT\n" +
                "source:      TEST"));


        whoisResources = getWebTarget("whois/test/mntner/TEST-MNT?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertSSOAttribute(whoisResources, "SSO");

    }

    @Test
    public void lookup_incorrect_bearer_token_with_multiple_mnt_scope() {

        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      TEST1-MNT\n" +
                "descr:       TEST Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@ripe.net\n" +
                "mnt-by:      TEST1-MNT\n" +
                "source:      TEST"));

        final WhoisResources whoisResources = getWebTarget("whois/test/mntner/TEST1-MNT?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertSSOAttribute(whoisResources, "SSO");

    }

    @Test
    public void lookup_incorrect_number_of_scope_not_filtered() {
        // Scopes are set to ANY for Oidc

        WhoisResources whoisResources = getWebTarget("whois/test/mntner/OWNER-MNT?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertSSOAttribute(whoisResources, "SSO person@net.net");
    }


    @Test
    public void lookup_correct_bearer_token_with_mnt_and_no_sso_and_filtered() {
        databaseHelper.addObject(TEST_IRT);
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final WhoisResources whoisResources = getWebTarget("whois/test/irt/irt-test?unfiltered", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().getFirst();
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_correct_bearer_token_with_different_mnt_and_filtered() {
        databaseHelper.addObject(TEST_IRT);
        final WhoisResources whoisResources = getWebTarget("whois/test/irt/irt-test?unfiltered", getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_correct_bearer_token_with_different_mnt_but_same_sso_and_filtered() {
        databaseHelper.addObject(TEST_IRT);
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

        final WhoisResources whoisResources = getWebTarget("whois/test/irt/irt-test?unfiltered",
                getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void delete_object_with_bearer_token_any_mnt_with_sso() {

        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT), MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void delete_object_with_bearer_token_with_mnt_with_sso() {

        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void delete_object_with_bearer_token_NONE_algo_jws_manually() {

        final String jwsObject = convertToOidcJwt(BASIC_AUTH_PERSON_OWNER_MNT, oAuthTokenIntrospectDummy.getJwk(),
                oAuthTokenIntrospectDummy.getPort(), APP_CLIENT_ID);
        final String modifiedJwsWithNoneAlgo = jwsObject.replaceFirst("^[^.]+", Base64URL.encode("{\"alg\":\"none\"}").toString());

        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", "Bearer " + modifiedJwsWithNoneAlgo, MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_bearer_token_NONE_algo_jws_manually_no_signature() throws ParseException {

        final String jwsObject = convertToOidcJwt(BASIC_AUTH_PERSON_OWNER_MNT, oAuthTokenIntrospectDummy.getJwk(),
                oAuthTokenIntrospectDummy.getPort(), APP_CLIENT_ID);
        final String modifiedJwsWithNoneAlgo = Base64URL.encode("{\"alg\":\"none\"}") + "." + JWSObject.parse(jwsObject).getPayload().toBase64URL() + ".";

        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", "Bearer " + modifiedJwsWithNoneAlgo, MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_invalid_bearer_token() {

        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", "Bearer invalidToken", MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_bearer_token_different_mnt_fails() {

        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_bearer_token_different_mnt_same_sso_fails() {
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


        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_bearer_token_same_mnt_different_sso_fails() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final Response whoisResources = getWebTarget("whois/references/TEST/role/TR1-TEST", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void create_succeeds_with_bearer_token_ANY_mnt_with_sso() {

        final WhoisResources whoisResources = getWebTarget("whois/test/person", getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_succeeds_with_bearer_token_with_mnt_with_sso() {

        final WhoisResources whoisResources = getWebTarget("whois/test/person", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_succeeds_with_bearer_token_with_mnt_with_sso_multiple_scope() {

        final WhoisResources whoisResources = getWebTarget("whois/test/person", getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_object_with_bearer_token_multiple_scope_when_no_sso_fails() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));


        final Response response = getWebTarget("whois/test/person",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));

    }

    @Test
    public void create_object_with_bearer_token_same_mnt_different_sso_fails() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final Response response = getWebTarget("whois/test/person", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void create_object_with_bearer_token_differnt_mnt_fails() {

        final Response response = getWebTarget("whois/test/person", getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void update_object_with_bearer_token_ANY_mnt_with_sso() {
        final RpslObject updated = new RpslObjectBuilder(TEST_2ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final WhoisResources whoisResources = getWebTarget("whois/TEST/role/TR2-TEST",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_ANY_MNT), MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueForAttribute(AttributeType.REMARKS), is("more_test"));

    }

    @Test
    public void update_object_with_bearer_token_with_mnt_with_sso() {
        final RpslObject updated = new RpslObjectBuilder(TEST_2ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final WhoisResources whoisResources = getWebTarget("whois/TEST/role/TR2-TEST",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueForAttribute(AttributeType.REMARKS), is("more_test"));
    }

    @Test
    public void update_object_with_bearer_token_with_multiple_mnt_with_sso() {
        final RpslObject updated = new RpslObjectBuilder(TEST_2ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final WhoisResources whoisResources = getWebTarget("whois/TEST/role/TR2-TEST",
                getBearerTokenForOidc(BASIC_AUTH_PERSON_MULTIPLE_MNT), MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueForAttribute(AttributeType.REMARKS), is("more_test"));
    }

    @Test
    public void update_object_with_invalid_bearer_token() {

        final RpslObject updated = new RpslObjectBuilder(TEST_2ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = getWebTarget("whois/TEST/role/TR2-TEST", "Bearer invalid",
                MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));

    }

    @Test
    public void update_object_with_bearer_token_different_mnt_fails() {

        final RpslObject updated = new RpslObjectBuilder(TEST_2ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = getWebTarget("whois/TEST/role/TR2-TEST",
                getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));

    }

    @Test
    public void update_object_with_bearer_token_different_mnt_same_sso_fails() {

        final RpslObject updated = new RpslObjectBuilder(TEST_2ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = getWebTarget("whois/TEST/role/TR2-TEST",
                getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));
    }

    @Test
    public void update_object_with_bearer_token_same_mnt_different_sso_fails() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final RpslObject updated = new RpslObjectBuilder(TEST_2ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = getWebTarget("whois/TEST/role/TR2-TEST",
                getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));
    }

    @Test
    public void create_multiple_domain_objects_with_bearer_token_success() {

        databaseHelper.addObject("" +
                "inet6num:      2a01:500::/22\n" +
                "mnt-by:        OWNER-MNT\n" +
                "mnt-domains:   OWNER-MNT\n" +
                "source:        TEST");

        final List<RpslObject> domains = Lists.newArrayList();

        for (int i = 4; i < 8; i++) {
            final RpslObject domain = RpslObject.parse(String.format("" +
                    "domain:        %d.0.1.0.a.2.ip6.arpa\n" +
                    "descr:         Reverse delegation for 2a01:500::/22\n" +
                    "admin-c:       TP1-TEST\n" +
                    "tech-c:        TP1-TEST\n" +
                    "zone-c:        TP1-TEST\n" +
                    "nserver:       ns1.example.com\n" +
                    "nserver:       ns2.example.com\n" +
                    "mnt-by:        OWNER-MNT\n" +
                    "source:        TEST", i));

            domains.add(domain);
        }

        final WhoisResources response = getWebTarget("whois/domain-objects/TEST", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(mapRpslObjects(domains.toArray(new RpslObject[0])), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        RestTest.assertErrorCount(response, 0);
        assertThat(response.getWhoisObjects(), hasSize(4));
    }

    @Test
    public void create_multiple_domain_objects_with_bearer_token_no_sso_fails() {
        databaseHelper.addObject("" +
                "mntner:        TEST-MNT\n" +
                "descr:         Test Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "auth:          SSO person@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "inet6num:      2a01:500::/22\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-domains:   TEST-MNT\n" +
                "source:        TEST");


        final RpslObject domain = RpslObject.parse("" +
                "domain:        1.0.1.0.a.2.ip6.arpa\n" +
                "descr:         Reverse delegation for 2a01:500::/22\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "nserver:       ns1.example.com\n" +
                "nserver:       ns2.example.com\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");

        final Response response = getWebTarget("whois/domain-objects/TEST", getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .post(Entity.entity(mapRpslObjects(domain), MediaType.APPLICATION_JSON_TYPE), Response.class);
        assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void lookup_person_using_bearer_token_email_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = accessControlListManager.getAccountingIdentifier(localhost,  getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_PERSON_OWNER_MNT)).getEmail());

        accessControlListManager.accountPersonalObjects(accountingIdentifier, accessControlListManager.getPersonalObjects(accountingIdentifier) + 1);

        try {
            getWebTarget("whois/test/person/TP1-TEST", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                    .get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void lookup_owned_person_using_bearer_token_email_not_acl_accounted() throws Exception {
        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "mnt-by:   OWNER-MNT\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_PERSON_OWNER_MNT)).getEmail());

        getWebTarget("whois/test/person/TP2-TEST", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT), MediaType.APPLICATION_XML)
                .get(Response.class);

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_PERSON_OWNER_MNT)).getEmail());

        assertThat(queriedBySSO, is(accountedBySSO));
    }

    @Test
    public void lookup_not_owned_person_using_bearer_token_email_acl_accounted() throws Exception {

        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "mnt-by:   OWNER-MNT\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_TEST_TEST_MNT)).getEmail());

        getWebTarget("whois/test/person/TP2-TEST", getBearerTokenForOidc(BASIC_AUTH_TEST_TEST_MNT), MediaType.APPLICATION_XML)
                .get(Response.class);

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_TEST_TEST_MNT)).getEmail());

        assertThat(accountedBySSO, is(queriedBySSO + 1));
    }

    @Test
    public void lookup_person_using_bearer_token_acl_counted_no_ip_counted() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedByIP = testPersonalObjectAccounting.getQueriedPersonalObjects(localhost);
        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_TEST_NO_MNT)).getEmail());


        final WhoisResources whoisResources = getWebTarget("whois/test/person/TP2-TEST", getBearerTokenForOidc(BASIC_AUTH_TEST_NO_MNT), MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().getFirst().getAttributes()
                        .stream()
                        .anyMatch( (attribute)-> attribute.getName().equals(AttributeType.E_MAIL.getName())),
                is(false));

        final int accountedByIp = testPersonalObjectAccounting.getQueriedPersonalObjects(localhost);
        assertThat(accountedByIp, is(queriedByIP));

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_TEST_NO_MNT)).getEmail());
        assertThat(accountedBySSO, is(queriedBySSO + 1));
    }

    @Test
    public void lookup_person_using_sso_no_acl_for_unlimited_remoteAddr() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = accessControlListManager.getAccountingIdentifier(localhost, getOAuthSession(APIKEY_TO_CLAIMSET.get(BASIC_AUTH_TEST_NO_MNT)).getEmail());

        databaseHelper.insertAclIpLimit(LOCALHOST_WITH_PREFIX, -1, true);
        ipResourceConfiguration.reload();

        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int limit = accessControlListManager.getPersonalObjects(accountingIdentifier);

        final Response response = getWebTarget("whois/test/person/TP2-TEST", getBearerTokenForOidc(BASIC_AUTH_TEST_NO_MNT), MediaType.APPLICATION_XML)
                .get(Response.class);

        assertThat(response.getStatus(), is(HttpStatus.OK_200));

        final int remaining = accessControlListManager.getPersonalObjects(accountingIdentifier);
        assertThat(remaining, is(limit));
    }


    @Test
    public void update_person_with_token_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final WhoisResources whoisResources = getWebTarget("whois/test/person/PP1-TEST",
                "valid-token", MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().getFirst().getAttributes(), hasItem(new Attribute("remarks", "updated")));
    }


    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_fails_expired_session(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO expired@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = getWebTarget("whois/syncupdates/test", getBearerTokenForOidc(BASIC_AUTH_EXPIRED),
                MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: [OIDC] Token has expired"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_fails_invalid_issued_at_claim(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO issues_at@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = getWebTarget("whois/syncupdates/test", getBearerTokenForOidc(BASIC_AUTH_ISSUES_AT),
                MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: [OIDC] An error occurred while attempting to decode the Jwt: Token\n" +
                "            issued in future"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_fails_invalid_signed_session(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO invalid@ripe.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_INVALID_SIGNATURE_API_KEY), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Invalid OIDC"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_fails_invalid_iss_claim(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO invalid_Iss@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_INVALID_ISS_API_KEY), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: [OIDC] An error occurred while attempting to decode the Jwt: The iss\n" +
                "            claim is not valid"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_fails_inactive_session() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";


        final String response = getWebTarget("whois/syncupdates/test",
                getBearerTokenForOidc(BASIC_AUTH_INACTIVE_TOKEN), MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: [OIDC] Token has expired"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_bearer_token_whois_wrong_audience_fails() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";


        final String response = getWebTarget("whois/syncupdates/test", getBearerTokenForOidc(BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE),
                MediaType.TEXT_PLAIN)
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("***Warning: [OIDC] An error occurred while attempting to decode the Jwt: Session\n" +
                "            cannot be used because it was created for a different application\n" +
                "            or environment"));
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

    WhoisResources map(final RpslObject... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
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


    @Override
    Invocation.Builder getWebTarget(final String path, String authValue,
                                    final String mediaType) {
        if (!authValue.startsWith("Bearer")) {
            authValue = getBearerTokenForOidc(authValue);
        }
        return SecureRestTest.target(getSecurePort(), path)
                .request(mediaType)
                .header(HttpHeaders.AUTHORIZATION, authValue);
    }

    private WhoisResources mapRpslObjects(final RpslObject... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

    public static OidcSession getOAuthSession(final JWTClaimsSet claimSet) throws ParseException {
        return new OidcSession.Builder()
                .aud(claimSet.getAudience())
                .keyId("123").scopes(Scope.parse(claimSet.getStringClaim("scope")).toStringList())
                .uuid(claimSet.getStringClaim("uuid"))
                .email(claimSet.getStringClaim("email")).build();
    }

    public static void assertSSOAttribute(final WhoisResources whoisResources, final String value) {
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes().stream().filter( attribute -> attribute.getValue().startsWith("SSO")).map( attribute -> attribute.getValue()).findFirst().get(), is(value));
    }
}
