package net.ripe.db.whois.api.httpserver;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class ClientCertificateServiceTestIntegration extends AbstractClientCertificateIntegrationTest {

    @Test
    @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    public void client_certificate() {
        final String response = getClientCertificateWithCertificate();
        assertThat(response, containsString("Found 1 certificate(s)."));
        assertThat(response, containsString("-----BEGIN CERTIFICATE-----"));
        assertThat(response, containsString("-----END CERTIFICATE-----"));

        final ProcessingException processingException = assertThrows(ProcessingException.class,
                this::getClientCertificateWithoutCertificate);
        assertThat(processingException.getMessage(), containsString("javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate"));
    }

    private String getClientCertificateWithCertificate() {
        return SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/client")
                .request()
                .get(String.class);
    }

    private void getClientCertificateWithoutCertificate(){
        SecureRestTest.target(getClientCertificatePort(), "whois/client")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
    }
}
