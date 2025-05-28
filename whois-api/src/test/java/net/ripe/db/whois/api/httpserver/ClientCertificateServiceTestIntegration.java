package net.ripe.db.whois.api.httpserver;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.common.support.DirtiesContextBeforeAndAfterClassTestExecutionListener;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestExecutionListeners;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
@TestExecutionListeners(
    listeners = DirtiesContextBeforeAndAfterClassTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ClientCertificateServiceTestIntegration extends AbstractClientCertificateIntegrationTest {

    @Test
    public void client_certificate() {
        final String response = SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/client")
            .request()
            .get(String.class);

        assertThat(response, containsString("Found 1 certificate(s)."));
        assertThat(response, containsString("-----BEGIN CERTIFICATE-----"));
        assertThat(response, containsString("-----END CERTIFICATE-----"));
    }

    @Test
    public void fail_on_no_client_certificate() {
        try {
            SecureRestTest.target(getClientCertificatePort(), "whois/client")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get(String.class);
            fail();
        } catch (ProcessingException e) {
            // expected
        }
    }

}
