package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_EXPIRED;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INACTIVE_TOKEN;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_ISS_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_SIGNATURE_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_ISSUES_AT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class WhoisRestOidcAuthTokenIntrospectionTestIntegration extends WhoisRestOidcAuthTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oidc.auth.enable","true");
        System.setProperty("oidc.session.client.id", APP_CLIENT_ID);

        System.setProperty("oauth.token.introspection","true");
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("oauth.token.introspection");
        System.clearProperty("apikey.public.key.url");
        System.clearProperty("apikey.max.scope");
    }

    @Test
    @Override
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

        assertThat(response, containsString("***Warning: Session associated with OIDC is not active"));
    }

    @Test
    @Override
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
        assertThat(response, containsString("***Warning: Session associated with OIDC is not active"));
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
        assertThat(response, containsString("***Warning: Session associated with OIDC is not active"));
    }

    @Test
    @Override
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
        assertThat(response, containsString("***Warning: Session associated with OIDC is not active"));
    }

    @Test
    @Override
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
        assertThat(response, containsString("***Warning: Session associated with OIDC is not active"));
    }

    @Test
    @Override
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
        assertThat(response, containsString("***Warning: Session associated with OIDC is not active"));
    }
}
