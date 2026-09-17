package net.ripe.db.whois.api.rest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
public class DomainObjectOidcServiceTestIntegration extends DomainObjectServiceTestIntegration {

    @BeforeAll
    public static void setupApiProperties() {
        System.setProperty("oidc.auth.enable","true");
    }

    @AfterAll
    public static void restApiProperties() {
        System.clearProperty("oidc.auth.enable");
    }
}
