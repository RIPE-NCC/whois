package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_EXPIRED;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_ISS_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_SIGNATURE_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_ISSUES_AT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class WhoisRestApiKeyAuthTokenIntrospectionTestIntegration extends WhoisRestApiKeyAuthTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oauth.token.introspection","true");
        System.setProperty("apikey.max.scope","2");
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("oauth.token.introspection");
        System.clearProperty("apikey.max.scope");
    }

    @Test
    @Override
    public void create_mntner_only_data_parameter_with_apiKey_fails_wrong_audience() {
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE))
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Session associated with APIKEY is not active"));
    }

    @Test
    @Override
    public void create_mntner_only_data_parameter_with_apiKey_fails_expired_session(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO expired@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_EXPIRED))
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Session associated with APIKEY is not active"));
    }

    @Test
    @Override
    public void create_mntner_only_data_parameter_with_apiKey_fails_invalid_signed_session(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO invalid@ripe.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_SIGNATURE_API_KEY))
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Session associated with APIKEY is not active"));
    }

    @Test
    @Override
    public void create_mntner_only_data_parameter_with_apiKey_fails_invalid_iss_claim(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO invalid_Iss@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_INVALID_ISS_API_KEY))
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Session associated with APIKEY is not active"));
    }

    @Test
    @Override
    public void create_mntner_only_data_parameter_with_apiKey_fails_invalid_issued_at_claim(){
        final String mntner =
                "mntner:        SSO-MNT\n" +
                        "descr:         description\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        noreply@ripe.net\n" +
                        "auth:          SSO person@net.net\n" +
                        "auth:          SSO issues_at@net.net\n" +
                        "mnt-by:        SSO-MNT\n" +
                        "source:        TEST";

        final String response = SecureRestTest.target(getSecurePort(), "whois/syncupdates/test")
                .request()
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(BASIC_AUTH_ISSUES_AT))
                .post(Entity.entity("DATA=" +  SyncUpdateUtils.encode(mntner),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create FAILED: [mntner] SSO-MNT"));
        assertThat(response, containsString("***Warning: Session associated with APIKEY is not active"));
    }
}
