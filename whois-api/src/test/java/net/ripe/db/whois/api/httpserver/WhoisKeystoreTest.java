package net.ripe.db.whois.api.httpserver;

import com.google.common.util.concurrent.Uninterruptibles;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

// TODO: [ES] remove duplicate WhoisKeystoreTest class from whois-internal
class WhoisKeystoreTest {

    @Test
    public void no_keystore() {
        assertThat(new WhoisKeystore(null, null, null).getKeystore(), is(nullValue()));
        assertThat(new WhoisKeystore(null, new String[]{}, null).getKeystore(), is(nullValue()));
        assertThat(new WhoisKeystore(new String[]{}, null, null).getKeystore(), is(nullValue()));
        assertThat(new WhoisKeystore(new String[]{}, new String[]{}, null).getKeystore(), is(nullValue()));
        assertThat(new WhoisKeystore(new String[]{}, new String[]{"/non-existant-filename"}, null).getKeystore(), is(nullValue()));
        assertThat(new WhoisKeystore(new String[]{"/non-existant-filename"}, new String[]{}, null).getKeystore(), is(nullValue()));
    }

    @Test
    public void nonexistant_filenames() {
        final WhoisKeystore subject = new WhoisKeystore(new String[]{"/non-existant-filename"}, new String[]{"/non-existant-filename"}, null);

        assertThat(subject.getKeystore(), is(notNullValue()));
    }

    @Test
    public void create_keystore() {
        final TestData testData = generateTestData();

        final WhoisKeystore subject = generateWhoisKeystore(testData);

        assertThat(subject.getKeystore(), is(notNullValue()));
        assertThat(subject.getPassword(), is(notNullValue()));
    }

    @Test
    public void keystore_outdated() throws Exception {
        final TestData testData = generateTestData("cn=one");

        final WhoisKeystore subject = generateWhoisKeystore(testData);

        assertThat(subject.isKeystoreOutdated(), is(false));
        assertThat(loadCertificateFromKeystore(subject.getKeystore(), subject.getPassword(), "CN=one"), is(notNullValue()));

        Uninterruptibles.sleepUninterruptibly(1L, TimeUnit.SECONDS);

        // generate new certificate
        final TestData moreTestData = generateTestData("cn=two");
        Files.copy(new File(moreTestData.certificateFilename).toPath(), new File(testData.certificateFilename).toPath(), StandardCopyOption.REPLACE_EXISTING);
        assertThat(subject.isKeystoreOutdated(), is(true));

        // load new certificate
        subject.reloadKeystore();
        assertThat(subject.isKeystoreOutdated(), is(false));
        assertThat(loadCertificateFromKeystore(subject.getKeystore(), subject.getPassword(), "CN=one"), is(nullValue()));
        assertThat(loadCertificateFromKeystore(subject.getKeystore(), subject.getPassword(), "CN=two"), is(notNullValue()));
    }


    // helper methods

    private TestData generateTestData() {
        return generateTestData("CN=subject");
    }

    private TestData generateTestData(final String certificateAlias) {
        try {
            final KeyPair keyPair = generateKeyPair();
            final X509CertificateHolder certificate = generateCertificate(keyPair, certificateAlias);
            return new TestData(certificate, keyPair.getPrivate());
        } catch (NoSuchAlgorithmException | OperatorCreationException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance("RSA").generateKeyPair();
    }

    private X509CertificateHolder generateCertificate(final KeyPair keyPair, final String alias) throws OperatorCreationException {
        final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        final Date notBefore = Date.from(Instant.now().minus(1L, ChronoUnit.DAYS));
        final Date notAfter = Date.from(Instant.now().plus(1L, ChronoUnit.DAYS));

        final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name("CN=issuer"),
                BigInteger.ONE,
                notBefore,
                notAfter,
                new X500Name(alias),    // subject
                publicKeyInfo);
        final ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider(new BouncyCastleProvider())
                .build(keyPair.getPrivate());

        return certificateBuilder.build(signer);
    }

    private static WhoisKeystore generateWhoisKeystore(final TestData testData) {
        return new WhoisKeystore(new String[]{testData.privateKeyFilename}, new String[]{testData.certificateFilename}, null);
    }

    @Nullable
    private static java.security.cert.Certificate loadCertificateFromKeystore(final String keystoreFilename, final String keystorePassword, final String alias) {
        try {
            final KeyStore keystore = KeyStore.getInstance(new File(keystoreFilename), keystorePassword.toCharArray());
            return keystore.getCertificate(alias);
        } catch (Exception e) {
            return null;
        }
    }

    private static class TestData {
        private final String certificateFilename;
        private final String privateKeyFilename;

        public TestData(final X509CertificateHolder certificate, final PrivateKey privateKey) throws IOException {
            this.certificateFilename = writeCertificateFile(certificate);
            this.privateKeyFilename = writePrivateKey(privateKey);
        }

        private static String writeCertificateFile(final X509CertificateHolder certificate) throws IOException {
            return writeObjectToFile(certificate, "certificate", "crt");
        }

        private static String writePrivateKey(final PrivateKey privateKey) throws IOException {
            return writeObjectToFile(privateKey, "privatekey", "key");
        }

        private static String writeObjectToFile(final Object object, final String prefix, final String suffix) throws IOException {
            return writeObjectToFile(object, Files.createTempFile(prefix, suffix).toFile());
        }

        private static String writeObjectToFile(final Object object, final File file) throws IOException {
            try (final JcaPEMWriter writer = new JcaPEMWriter(new FileWriter(file))) {
                writer.writeObject(object);
            }
            return file.getAbsolutePath();
        }
    }

}
