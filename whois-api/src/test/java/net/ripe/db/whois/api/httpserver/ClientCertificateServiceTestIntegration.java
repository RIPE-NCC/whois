package net.ripe.db.whois.api.httpserver;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class ClientCertificateServiceTestIntegration extends AbstractClientCertificateIntegrationTest {


    @Test
    public void client_certificate() {
        final String response = SecureRestTest.target(getClientSSLContext(), getSecurePort(), "whois/client")
            .request()
            .get(String.class);

        assertThat(response, containsString("Found 1 certificate(s)."));
        assertThat(response, containsString("-----BEGIN CERTIFICATE-----"));
        assertThat(response, containsString("-----END CERTIFICATE-----"));
    }

    @Test
    public void no_client_certificate() {
        try {
            SecureRestTest.target(getSecurePort(), "whois/client")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Bad Request"));
        }
    }

}
