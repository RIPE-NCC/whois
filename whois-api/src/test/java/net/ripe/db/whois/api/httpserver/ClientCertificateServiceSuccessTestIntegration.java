package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.SecureRestTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ClientCertificateServiceSuccessTestIntegration extends AbstractClientCertificateIntegrationTest {

    @Test
    public void client_certificate() {
        final String response = SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/client")
            .request()
            .get(String.class);

        assertThat(response, containsString("Found 1 certificate(s)."));
        assertThat(response, containsString("-----BEGIN CERTIFICATE-----"));
        assertThat(response, containsString("-----END CERTIFICATE-----"));
    }

}
