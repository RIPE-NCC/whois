package net.ripe.db.whois.api.rest;

import org.junit.jupiter.api.BeforeAll;



class WhoisRestBearerAuthTestIntegration extends WhoisRestBearerAuthTokenInspectionTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oauth.token.inspection","false");
    }

}
