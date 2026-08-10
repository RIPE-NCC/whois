package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.api.SecureRestTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class OverrideWithNonTrustedIpOidcTestIntegration extends OverrideWithNonTrustedIpTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("ipranges.trusted", "");
        System.setProperty("oidc.auth.enable","true");
        System.setProperty("oidc.session.client.id", APP_CLIENT_ID);
    }

    @AfterAll
    public static void restApiProperties() {
        System.setProperty("ipranges.trusted", "");
        System.clearProperty("oidc.auth.enable");
        System.clearProperty("oidc.session.client.id");
    }

    @Override
    Invocation.Builder getWebTarget(final String path,
                                    final String mediaType) {

        return SecureRestTest.target(getSecurePort(), path)
                .request(mediaType)
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc("db_e2e_1"));
    }

}
