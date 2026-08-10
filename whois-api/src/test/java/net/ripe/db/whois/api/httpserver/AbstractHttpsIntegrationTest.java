package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.OAuthTokenIntrospectDummy;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;

import static net.ripe.db.whois.api.OAuthTokenIntrospectDummy.convertToOidcJwt;

public abstract class AbstractHttpsIntegrationTest extends AbstractIntegrationTest {

    public static final String APP_CLIENT_ID = "test-app";

    @Autowired
    protected OAuthTokenIntrospectDummy oAuthTokenIntrospectDummy;

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

    protected String getBearerTokenForOidc(final String userKey) {
        return StringUtils.joinWith(" ","Bearer", convertToOidcJwt(userKey, oAuthTokenIntrospectDummy.getJwk(),
                oAuthTokenIntrospectDummy.getPort(), APP_CLIENT_ID));
    }
}
