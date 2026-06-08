package net.ripe.db.whois.api.rest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WhoisRestBearerValidatingAuthTestIntegration extends WhoisRestBearerAuthTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oauth.token.inspection","false");
    }

    @Override
    @Test
    @Disabled
    public void create_mntner_only_data_parameter_with_bearer_token_fails_inactive_session(){
        // TODO: Test with an expired session without inspection call
    }

}
