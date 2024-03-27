package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractHttpsIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    public static void enableHttps() {
        final CertificatePrivateKeyPair certificatePrivateKeyPair = new CertificatePrivateKeyPair();
        System.setProperty("port.api.secure", "0");
        System.setProperty("http.sni.host.check", "false");
        System.setProperty("whois.certificates", certificatePrivateKeyPair.getCertificateFilename());
        System.setProperty("whois.private.keys", certificatePrivateKeyPair.getPrivateKeyFilename());
        System.setProperty("https.x_forwarded_for", "false");
    }

    @AfterAll
    public static void disableHttps() {
        System.clearProperty("port.api.secure");
        System.clearProperty("whois.certificates");
        System.clearProperty("whois.private.keys");
        System.clearProperty("https.x_forwarded_for");
    }

    public int getSecurePort() {
        return jettyBootstrap.getSecurePort();
    }

}
