package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.rest.client.DummyTrustManager;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.whois.common.rpsl.AttributeType.CERTIF;

public class AbstractClientCertificateIntegrationTest extends AbstractHttpsIntegrationTest {

    // generate certificate and private key for client-side authentication
    private static final CertificatePrivateKeyPair CERTIFICATE_PRIVATE_KEY = new CertificatePrivateKeyPair();

    // create keystore containing client-side certificate
    private static final WhoisKeystore CLIENT_KEYSTORE = new WhoisKeystore(
            new String[]{CERTIFICATE_PRIVATE_KEY.getPrivateKeyFilename()},
            new String[]{CERTIFICATE_PRIVATE_KEY.getCertificateFilename()},
            null);

    // create client-side SSL context with keystore
    private static final SSLContext CLIENT_SSL_CONTEXT = createSSLContext();

    // offset to create x509 keycert primary keys
    private static final AtomicInteger X509_KEYCERT_OFFSET = new AtomicInteger();

    @BeforeAll
    public static void enableClientAuth() {System.setProperty("port.client.auth", "0");}

    @AfterAll
    public static void disableClientAuth() {
        System.clearProperty("port.client.auth");
    }

    public SSLContext getClientSSLContext() {
        return CLIENT_SSL_CONTEXT;
    }

    public X509Certificate getClientCertificate() {
        return CERTIFICATE_PRIVATE_KEY.getCertificate();
    }

    // helper methods

    // Use client certificate in SSL context
    protected static SSLContext createSSLContext() {
        return createSSLContext(CLIENT_KEYSTORE.getKeystore(), CLIENT_KEYSTORE.getPassword());
    }

    protected static SSLContext getDummySSLContext() throws NoSuchAlgorithmException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new DummyTrustManager()}, new SecureRandom());
            return sslContext;
        } catch (KeyManagementException e) {
            throw new RuntimeException("Failed to initialize dummy SSLContext", e);
        }
    }

    protected static SSLContext createSSLContext(final String keystoreFilename, final String keystorePassword) {
        final KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(new File(keystoreFilename), keystorePassword.toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalStateException(e);
        }

        final KeyManager keyManager;
        try {
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            keyManager = keyManagerFactory.getKeyManagers()[0];
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            throw new IllegalStateException(e);
        }

        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{keyManager}, new TrustManager[]{new DummyTrustManager()}, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }
    }

    // Create X509 keycert object
    public static RpslObject createKeycertObject(final X509Certificate x509, final String mntner) {
        final RpslObjectBuilder builder = new RpslObjectBuilder();

        final X509CertificateWrapper x509Wrapper = new X509CertificateWrapper(x509);

        final String key = String.format("X509-%d", X509_KEYCERT_OFFSET.incrementAndGet());
        builder.append(new RpslAttribute(AttributeType.KEY_CERT, key));
        builder.append(new RpslAttribute(AttributeType.METHOD, "X509"));

        x509Wrapper.getCertificateAsString().lines().forEach(line -> {
            builder.append(new RpslAttribute(CERTIF, line));
        });

        x509Wrapper.getOwners().forEach(owner -> builder.addAttributeSorted(new RpslAttribute(AttributeType.OWNER, owner)));
        builder.addAttributeSorted(new RpslAttribute(AttributeType.FINGERPR, x509Wrapper.getFingerprint()));

        builder.addAttributeSorted(new RpslAttribute(AttributeType.MNT_BY, mntner));
        builder.addAttributeSorted(new RpslAttribute(AttributeType.SOURCE, "TEST"));

        return builder.get();
    }

    public int getClientCertificatePort() {
        return jettyBootstrap.getClientAuthPort();
    }
}
