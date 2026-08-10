package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class WhoisOidcSearchServiceTestIntegration extends WhoisSearchServiceTestIntegration {

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

    @Override
    Invocation.Builder getWebTarget() {
        return SecureRestTest.target(getSecurePort(), "whois/search?flags=rB&type-filter=mntner&query-string=OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("db_e2e_1,zoh,reason {notify=false}"))
                .queryParam("clientIp", "2001:fff:001::")
                .request(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenForOidc("db_e2e_1"));
    }
}
