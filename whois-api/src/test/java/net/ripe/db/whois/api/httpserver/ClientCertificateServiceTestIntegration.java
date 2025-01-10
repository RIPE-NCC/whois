package net.ripe.db.whois.api.httpserver;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ClientCertificateServiceTestIntegration extends AbstractClientCertificateIntegrationTest {

    @RepeatedTest(value = 5, failureThreshold = 4)
    public void client_certificate() {
        final String response = SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/client")
            .request()
            .get(String.class);

        assertThat(response, containsString("Found 1 certificate(s)."));
        assertThat(response, containsString("-----BEGIN CERTIFICATE-----"));
        assertThat(response, containsString("-----END CERTIFICATE-----"));
    }

    @RepeatedTest(value = 5, failureThreshold = 4)
    public void fail_on_no_client_certificate() {
        try {
            SecureRestTest.target(getClientCertificatePort(), "whois/client")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
            fail();
        } catch (ProcessingException e) {
            assertThat(e.getMessage(), containsString("javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate"));
        }
    }

}
