package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.ws.rs.ClientErrorException;
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
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.oauth.APIKeySession;
import net.ripe.db.whois.common.oauth.OAuthSession;
import net.ripe.db.whois.common.oauth.OAuthUtils;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.acl.SSOResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.APIKEY_TO_OAUTHSESSION;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_INVALID_API_KEY;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_INVALID_SIGNATURE_API_KEY;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_ANY_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_NO_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_NULL_SCOPE;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_TEST_NO_MNT;
import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_TEST_TEST_MNT;
import static net.ripe.db.whois.api.rest.WhoisRestBasicAuthTestIntegration.getBasicAuthenticationHeader;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class WhoisRestApiKeyAuthTestIntegration extends AbstractHttpsIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    public static final String TEST_PERSON_STRING = "" +
            "person:         Test Person\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";

    public static final String TEST_ROLE_STRING = "" +
            "role:          Test Role\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TR2-TEST\n" +
            "e-mail:        test123@ripe.net\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST";

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
    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private SSOResourceConfiguration ssoResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;


    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("apikey.authenticate.enabled","true");
        System.setProperty("apikey.scope.mandatory","true");
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("apikey.authenticate.enabled");
        System.clearProperty("apikey.scope.mandatory");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
     //   databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.addObject(TEST_ROLE);
        databaseHelper.addObject(TEST_IRT);
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
    public void create_failed_with_basic_auth_api_key_no_https() {

        final Response response =  RestTest.target(getPort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.UPGRADE_REQUIRED_426));
        assertThat(response.readEntity(String.class), containsString("HTTPS required for Authorization Header"));
    }

    @Test
    public void request_failed_with_basic_auth_api_key_illegal_query_param() {

        final Response response =  SecureRestTest.target(getSecurePort(), "whois/test/person?" + OAuthUtils.APIKEY_KEY_ID_QUERY_PARAM + "=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void request_failed_with_basic_auth_api_key_illegal_bearer_header() {

        final Response response =  SecureRestTest.target(getSecurePort(), "whois/test/person?" + OAuthUtils.APIKEY_KEY_ID_QUERY_PARAM + "=test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer eFR0cm9lZUpWYWlmSWNQR1BZUW5kSmhnOmp5akhYR2g4WDFXRWZyc2M5SVJZcUVYbw==")
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void update_selfrefencing_maintainer_only_data_parameter_with_api_key() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";
        databaseHelper.addObject(mntner);

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(mntner + "\nremarks: updated"))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_no_sso() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .get(String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_wrong_audience() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE))
                .get(String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: The API Key cannot be used because it was created for a different\n" +
                "            application or environment"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_null_Scope() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NULL_SCOPE))
                .get(String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Whois scope can not be empty"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_no_mnt_Scope() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_NO_MNT))
                .get(String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Whois scope can not be empty"));
    }

    @Test
    public void create_mntner_only_data_parameter_with_apiKey_fails_invalid() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(mntner))
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_API_KEY))
                .get(String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Invalid APIKEY"));
    }

    @Test
    public void lookup_correct_api_key_with_sso_and_filtered_wrong_audience() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
    }

    @Test
    public void lookup_correct_api_key_with_sso_and_unfiltered() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, false);
    }

    @Test
    public void lookup_correct_api_key_with_sso_and_wrong_audience() {

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/irt/irt-test?unfiltered")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE))
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertIrt(whoisObject, true);
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
    public void delete_object_with_apikey_ANY_mnt_with_sso() {
        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void delete_object_with_apikey_with_mnt_with_sso() {
        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void delete_object_with_invalid_apikey() {

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_API_KEY))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_valid_apikey_invalid_jwt_signature() {

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_SIGNATURE_API_KEY))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void delete_object_with_apikey_different_mnt_fails() {

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

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/references/TEST/role/TR2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .delete(Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void create_succeeds_with_apiKey_ANY_mnt_with_sso() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person?keyId=l6lRZgvOFIphjiGwtCGuLwqw",getSecurePort())));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_succeeds_with_apiKey_with_mnt_with_sso() {
        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("https://localhost:%s/test/person?keyId=p6lRZgvOFIphjiGwtCGuLwqw",getSecurePort())));
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
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);

        assertPersonObject(whoisResources, object);
    }

    @Test
    public void create_object_with_apikey_same_mnt_different_sso_fails() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final Response response = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void create_object_with_apikey_differnt_mnt_fails() {
        final Response response = SecureRestTest.target(getSecurePort(), "whois/test/person")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), Response.class);

        assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void update_object_with_apikey_ANY_mnt_with_sso() {
        final RpslObject updated = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/TEST/role/TR2-TEST")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_ANY_MNT))
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueForAttribute(AttributeType.REMARKS), is("more_test"));

    }

    @Test
    public void update_object_with_apikey_with_mnt_with_sso() {
        final RpslObject updated = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final WhoisResources whoisResources = SecureRestTest.target(getSecurePort(), "whois/TEST/role/TR2-TEST")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().size(), is(1));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueForAttribute(AttributeType.REMARKS), is("more_test"));
    }

    @Test
    public void update_object_with_invalid_apikey() {

        final RpslObject updated = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/TEST/role/TR2-TEST")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_API_KEY))
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));

    }

    @Test
    public void update_object_with_valid_apikey_invalid_jwt_signature() {

        final RpslObject updated = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/TEST/role/TR2-TEST")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_SIGNATURE_API_KEY))
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));
    }

    @Test
    public void update_object_with_apikey_different_mnt_fails() {

        final RpslObject updated = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/TEST/role/TR2-TEST")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));

    }

    @Test
    public void update_object_with_apikey_different_mnt_same_sso_fails() {
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


        final RpslObject updated = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/TEST/role/TR2-TEST")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));
    }

    @Test
    public void update_object_with_apikey_same_mnt_different_sso_fails() {
        databaseHelper.updateObject(RpslObject.parse("" +
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO test@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final RpslObject updated = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        final Response whoisResources = SecureRestTest.target(getSecurePort(), "whois/TEST/role/TR2-TEST")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .put(Entity.entity(map(updated), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(UNAUTHORIZED.getStatusCode()));
        assertThat(databaseHelper.lookupObject(ROLE, updated.getKey().toString()).getValueOrNullForAttribute(AttributeType.REMARKS), is(nullValue()));
    }

    @Test
    public void create_multiple_domain_objects_with_api_key_success() {

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

        final WhoisResources response = SecureRestTest.target(getSecurePort(), "whois/domain-objects/TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .post(Entity.entity(mapRpslObjects(domains.toArray(new RpslObject[0])), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        RestTest.assertErrorCount(response, 0);
        assertThat(response.getWhoisObjects(), hasSize(4));
    }

    @Test
    public void create_multiple_domain_objects_with_api_key_no_sso_fails() {
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

        final Response response = SecureRestTest.target(getSecurePort(), "whois/domain-objects/TEST")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                    .post(Entity.entity(mapRpslObjects(domain), MediaType.APPLICATION_JSON_TYPE), Response.class);
        assertThat(response.getStatus(), is(UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void lookup_person_using_api_key_email_acl_blocked() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = accessControlListManager.getAccountingIdentifier(localhost,  getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_PERSON_OWNER_MNT)).getEmail());

        accessControlListManager.accountPersonalObjects(accountingIdentifier, accessControlListManager.getPersonalObjects(accountingIdentifier) + 1);

        try {
            SecureRestTest.target(getSecurePort(), "whois/test/person/TP1-TEST")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                    .get(String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(429));       // Too Many Requests
        }
    }

    @Test
    public void lookup_owned_person_using_apikey_token_email_not_acl_accounted() throws Exception {
        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "mnt-by:   OWNER-MNT\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_PERSON_OWNER_MNT)).getEmail());

        SecureRestTest.target(getSecurePort(), "whois/test/person/TP2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT))
                .get(Response.class);

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_PERSON_OWNER_MNT)).getEmail());

        assertThat(queriedBySSO, is(accountedBySSO));
    }

    @Test
    public void lookup_not_owned_person_using_apikey_token_email_acl_accounted() throws Exception {
        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "mnt-by:   OWNER-MNT\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_TEST_TEST_MNT)).getEmail());

        SecureRestTest.target(getSecurePort(), "whois/test/person/TP2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_TEST_MNT))
                .get(Response.class);

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_TEST_TEST_MNT)).getEmail());

        assertThat(accountedBySSO, is(queriedBySSO + 1));
    }

    @Test
    public void lookup_person_using_apiKey_acl_counted_no_ip_counted() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int queriedByIP = testPersonalObjectAccounting.getQueriedPersonalObjects(localhost);
        final int queriedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_TEST_NO_MNT)).getEmail());

        final WhoisResources whoisResources =   SecureRestTest.target(getSecurePort(), "whois/test/person/TP2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_NO_MNT))
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes()
                        .stream()
                        .anyMatch( (attribute)-> attribute.getName().equals(AttributeType.E_MAIL.getName())),
                is(false));

        final int accountedByIp = testPersonalObjectAccounting.getQueriedPersonalObjects(localhost);
        assertThat(accountedByIp, is(queriedByIP));

        final int accountedBySSO = testPersonalObjectAccounting.getQueriedPersonalObjects(getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_TEST_NO_MNT)).getEmail());
        assertThat(accountedBySSO, is(queriedBySSO + 1));
    }

    @Test
    public void lookup_person_using_sso_no_acl_for_unlimited_remoteAddr() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        final AccountingIdentifier accountingIdentifier = accessControlListManager.getAccountingIdentifier(localhost, getOAuthSession(APIKEY_TO_OAUTHSESSION.get(BASIC_AUTH_TEST_NO_MNT)).getEmail());

        databaseHelper.insertAclIpLimit(LOCALHOST_WITH_PREFIX, -1, true);
        ipResourceConfiguration.reload();

        databaseHelper.addObject(
                "person:    Test Person\n" +
                        "nic-hdl:   TP2-TEST\n" +
                        "e-mail:   test@ripe.net\n" +
                        "source:    TEST");

        final int limit = accessControlListManager.getPersonalObjects(accountingIdentifier);

        final Response response =   SecureRestTest.target(getSecurePort(), "whois/test/person/TP2-TEST")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_TEST_NO_MNT))
                .get(Response.class);

        assertThat(response.getStatus(), is(HttpStatus.OK_200));

        final int remaining = accessControlListManager.getPersonalObjects(accountingIdentifier);
        assertThat(remaining, is(limit));
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

    public static String getBasicAuthHeader(final String basicAuth) {
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

    private WhoisResources mapRpslObjects(final RpslObject... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

    private APIKeySession getOAuthSession(final JWTClaimsSet claimSet) throws ParseException {
        return (APIKeySession) new OAuthSession.Builder().aud(claimSet.getAudience())
                .keyId("123").scope(claimSet.getStringClaim("scope"))
                        .uuid(claimSet.getStringClaim("uuid"))
                        .email(claimSet.getStringClaim("email")).build();
    }

}
