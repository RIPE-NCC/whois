package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.rest.client.DummyTrustManager;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
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

import static net.ripe.db.whois.common.rpsl.AttributeType.CERTIF;

public class AbstractClientCertificateIntegrationTest extends AbstractHttpsIntegrationTest {

    private static final CertificatePrivateKeyPair CERTIFICATE_PRIVATE_KEY = new CertificatePrivateKeyPair();
    private static final WhoisKeystore CLIENT_KEYSTORE = new WhoisKeystore(
            new String[]{CERTIFICATE_PRIVATE_KEY.getPrivateKeyFilename()},
            new String[]{CERTIFICATE_PRIVATE_KEY.getCertificateFilename()},
            null);
    private static final SSLContext CLIENT_SSL_CONTEXT = createSSLContext();

    @BeforeAll
    public static void enableClientAuth() {
        System.setProperty("client.cert.auth.enabled", "true");
    }

    public SSLContext getClientSSLContext() {
        return CLIENT_SSL_CONTEXT;
    }

    public X509Certificate getClientCertificate() {
        return CERTIFICATE_PRIVATE_KEY.getCertificate();
    }

    // helper methods

    private static SSLContext createSSLContext() {
        final KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(new File(CLIENT_KEYSTORE.getKeystore()), CLIENT_KEYSTORE.getPassword().toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalStateException(e);
        }

        final KeyManager keyManager;
        try {
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, CLIENT_KEYSTORE.getPassword().toCharArray());
            keyManager = keyManagerFactory.getKeyManagers()[0];
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            throw new IllegalStateException(e);
        }

        final TrustManager trustManager = new DummyTrustManager();

        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{keyManager}, new TrustManager[]{trustManager}, new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }
    }

    public static RpslObject createKeycertObject(final X509Certificate x509, final String mntner) {
        final RpslObjectBuilder builder = new RpslObjectBuilder();

        final X509CertificateWrapper x509Wrapper = new X509CertificateWrapper(x509);

        final String key = String.format("%s-TEST", x509Wrapper.getFingerprint().replaceAll("[:]", "").substring(24));
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

}
