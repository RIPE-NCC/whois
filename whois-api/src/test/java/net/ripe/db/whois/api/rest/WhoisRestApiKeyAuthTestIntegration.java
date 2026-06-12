package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.api.ApiKeyAuthServerDummy.BASIC_AUTH_ISSUES_AT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class WhoisRestApiKeyAuthTestIntegration extends WhoisRestApiKeyAuthTokenInspectionTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oauth.token.inspection","false");
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
        assertThat(response, containsString("***Warning: Session associated with API Key is not active"));
    }
}
