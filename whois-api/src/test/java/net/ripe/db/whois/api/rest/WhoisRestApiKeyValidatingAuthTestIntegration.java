package net.ripe.db.whois.api.rest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class WhoisRestApiKeyValidatingAuthTestIntegration extends WhoisRestApiKeyAuthTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oauth.token.inspection","false");
    }
}
