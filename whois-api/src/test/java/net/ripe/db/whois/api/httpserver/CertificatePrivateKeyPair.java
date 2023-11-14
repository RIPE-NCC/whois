package net.ripe.db.whois.api.httpserver;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class CertificatePrivateKeyPair {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final X509Certificate certificate;
    private final PrivateKey privateKey;
    private final String certificateFilename;
    private final String privateKeyFilename;

    public CertificatePrivateKeyPair() {
        this("CN=subject");
    }

    public CertificatePrivateKeyPair(final String certificateAlias) {
        try {
            final KeyPair keyPair = generateKeyPair();
            final X509CertificateHolder certificate = generateCertificate(keyPair, certificateAlias);
            this.certificate = convertCertificate(certificate);
            this.privateKey = keyPair.getPrivate();
            this.certificateFilename = writeCertificateFile(certificate);
            this.privateKeyFilename = writePrivateKey(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException | OperatorCreationException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getCertificateFilename() {
        return this.certificateFilename;
    }

    public String getPrivateKeyFilename() {
        return this.privateKeyFilename;
    }

    // helper methods

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

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        final KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
        rsa.initialize(2048);
        return rsa.generateKeyPair();
    }

    private X509Certificate convertCertificate(final X509CertificateHolder certificate) {
        try {
            return new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider()).getCertificate(certificate);
        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        }
    }

    private static X509CertificateHolder generateCertificate(final KeyPair keyPair, final String alias) throws OperatorCreationException {
        final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        final Date notBefore = Date.from(Instant.now().minus(1L, ChronoUnit.DAYS));
        final Date notAfter = Date.from(Instant.now().plus(1L, ChronoUnit.DAYS));

        final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name("CN=ripe.net"),
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
}
