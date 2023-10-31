package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provide a keystore file for the embedded Jetty SSL Connector configuration.
 *
 * Either use an existing keystore, or create a new one based on the private key and SSL certificate.
 */
// TODO: [ES] remove duplicate WhoisKeystore class from whois-internal
@Component
public class WhoisKeystore {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisKeystore.class);

    protected static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    protected static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    // PKCS#1 private key in PEM format
    protected static final String BEGIN_PKCS1_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----";
    protected static final String END_PKCS1_PRIVATE_KEY = "-----END RSA PRIVATE KEY-----";

    // PKCS#8 private key
    protected static final String BEGIN_PKCS8_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    protected static final String END_PKCS8_PRIVATE_KEY = "-----END PRIVATE KEY-----";

    private static final Pattern BASE64_PATTERN = Pattern.compile("(?mi)^([A-Z0-9+/=]+)$");

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final CertificateFactory X509_CERTIFICATE_FACTORY;
    static {
        try {
            X509_CERTIFICATE_FACTORY = CertificateFactory.getInstance("x.509");
        } catch (CertificateException e) {
            LOGGER.error(e.getClass().getName(), e);
            throw new IllegalStateException(e);
        }
    }

    private final String[] privateKeyFilenames;
    private final String[] certificateFilenames;
    private final String keystoreFilename;
    private final String keystorePassword;


    /**
     * Constructor.
     * @param privateKeyFilenames (optional) PEM-encoded private key files (i.e. SSLCertificateKeyFile).
     * @param certificateFilenames (optional) PEM-encoded certificates (i.e. SSLCertificateFile).
     */
    @Autowired
    public WhoisKeystore(
            @Value("${whois.private.keys:}") final String[] privateKeyFilenames,
            @Value("${whois.certificates:}") final String[] certificateFilenames,
            @Value("${whois.keystore:}") final String keyStore) {
        this.privateKeyFilenames = privateKeyFilenames;
        this.certificateFilenames = certificateFilenames;
        if (!isNullOrEmpty(privateKeyFilenames) && !isNullOrEmpty(certificateFilenames)) {
            this.keystorePassword = UUID.randomUUID().toString();
            this.keystoreFilename = writeKeyStoreNewFile(createKeyStore(), keyStore);
            LOGGER.info("Created new keystore: {}", this.keystoreFilename);
        } else {
            LOGGER.info("NO keystore");
            this.keystorePassword = null;
            this.keystoreFilename = null;
        }
    }

    @Nullable
    public String getKeystore() {
        return this.keystoreFilename;
    }

    @Nullable
    public String getPassword() {
        return this.keystorePassword;
    }

    // private key and certificate

    private List<PrivateKey> readPrivateKeys(final String[] privateKeyFilenames) {
        final List<PrivateKey> privateKeys = Lists.newArrayList();
        for (String privateKeyFilename : privateKeyFilenames) {
            if (!exists(privateKeyFilename)) {
                LOGGER.info("Private key {} not found", privateKeyFilename);
                continue;
            }
            privateKeys.addAll(readPrivateKeys(privateKeyFilename));
        }
        return privateKeys;
    }

    private List<Certificate> readCertificates(final String[] certificateFilenames) {
        final List<Certificate> certificates = Lists.newArrayList();
        for (String certificateFilename : certificateFilenames) {
            if (!exists(certificateFilename)) {
                LOGGER.info("Certificate {} not found", certificateFilename);
                continue;
            }
            certificates.addAll(readCertificates(certificateFilename));
        }
        return certificates;
    }

    private List<Certificate> readCertificates(final String filename) {
        final List<Certificate> results = Lists.newArrayList();
        for (final String text : readTextFromFile(filename, BEGIN_CERTIFICATE, END_CERTIFICATE)) {
             try {
                results.add(
                    X509_CERTIFICATE_FACTORY.generateCertificate(
                        new ByteArrayInputStream(readEncodedKey(text))));
             } catch (CertificateException | IOException e) {
                LOGGER.error(e.getClass().getName(), e);
             }
        }
        return results;
    }

    private List<PrivateKey> readPrivateKeys(final String filename){
        final List<PrivateKey> results = Lists.newArrayList();
        for (String text : readTextFromFile(filename, BEGIN_PKCS1_PRIVATE_KEY, END_PKCS1_PRIVATE_KEY)) {
            try {
                results.add(parsePkcs1Key(text));
            } catch (IOException e) {
                LOGGER.error(e.getClass().getName(), e);
            }
        }
        return results;
    }

    // key parsing

    // Parse PKCS#1 key (supported by Bouncy Castle)
    private static PrivateKey parsePkcs1Key(final String text) throws IOException {
        final PEMParser pemParser = new PEMParser(new StringReader(text));
        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
        return converter.getKeyPair((PEMKeyPair) pemParser.readObject()).getPrivate();
    }

    // Parse PKCS#8 key (supported by Java)
    @SuppressWarnings("unused")
    private PrivateKey parsePkcs8Key(final String text) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        return KeyFactory.getInstance("RSA").generatePrivate(
                        new PKCS8EncodedKeySpec(
                            readEncodedKey(text)));
    }

    private byte[] readEncodedKey(final String certificate) throws IOException {
        final StringBuilder builder = new StringBuilder();

        final Iterator<String> lines = certificate.lines().iterator();
        while (lines.hasNext()) {
            final String line = lines.next();
            final Matcher matcher = BASE64_PATTERN.matcher(line);
            if (matcher.matches()) {
                builder.append(line);
            }
        }

        return Base64.getDecoder().decode(builder.toString().getBytes());
    }

    // keystore

    private KeyStore createKeyStore() {
        try {
            final KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
            keyStore.load(null);
            return loadKeyStore(keyStore);
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName(), e);
            throw new IllegalStateException("Couldn't create keystore", e);
        }
    }

    private KeyStore loadKeyStore(final KeyStore keyStore) throws KeyStoreException {
        final List<Certificate> certificates = readCertificates(certificateFilenames);
        for (Certificate certificate : certificates) {
            final String alias = getAlias(certificate);
            LOGGER.info("Adding certificate to keystore: {}", alias);
            keyStore.setCertificateEntry(alias, certificate);
        }

        for (PrivateKey privateKey : readPrivateKeys(privateKeyFilenames)) {
            final String alias = "pk";
            LOGGER.info("Adding private key to keystore");
            keyStore.setKeyEntry(alias, privateKey, keystorePassword.toCharArray(), certificates.toArray(new Certificate[0]));
        }

        return keyStore;
    }

    private String getAlias(final Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            return ((X509Certificate)certificate).getSubjectX500Principal().getName();
        }
        throw new IllegalStateException("Can't get alias for unknown " + certificate.getClass().getName());
    }

    public boolean isKeystoreOutdated() {
        if (this.keystoreFilename == null) {
            return false;
        }

        final FileTime keyStoreLastModified;
        try {
            keyStoreLastModified = Files.getLastModifiedTime(new File(this.keystoreFilename).toPath());
        } catch (IOException e) {
            LOGGER.info("Caught {} on get modified time for keystore {}: {}", e.getClass().getName(), this.keystoreFilename, e.getMessage());
            return false;
        }

        for (String certificateFilename : this.certificateFilenames) {
            try {
                final FileTime certificateLastModified = Files.getLastModifiedTime(new File(certificateFilename).toPath());
                if (keyStoreLastModified.compareTo(certificateLastModified) < 0) {
                    return true;
                }
            } catch (IOException e) {
                LOGGER.info("Caught {} on get modified time for certificate {}: {}", e.getClass().getName(), certificateFilename, e.getMessage());
            }
        }

        for (String privateKeyFilename : this.privateKeyFilenames) {
            try {
                final FileTime privateKeyLastModified = Files.getLastModifiedTime(new File(privateKeyFilename).toPath());
                if (keyStoreLastModified.compareTo(privateKeyLastModified) < 0) {
                    return true;
                }
            } catch (IOException e) {
                LOGGER.info("Caught {} on get modified time for private key {}: {}", e.getClass().getName(), privateKeyFilename, e.getMessage());
            }
        }

        return false;
    }

    public void reloadKeystore() {
        if ((this.keystoreFilename == null) || (this.keystorePassword == null)) {
            return;
        }

        final KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(new File(keystoreFilename), keystorePassword.toCharArray());
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't open keystore", e);
        }

        try {
            deleteKeystoreEntries(keyStore);
            loadKeyStore(keyStore);
            writeKeyStore(keyStore, this.keystoreFilename);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Couldn't reload keystore", e);
        }
    }

    private void deleteKeystoreEntries(final KeyStore keyStore) throws KeyStoreException {
        if (this.keystoreFilename == null) {
            return;
        }

        final Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            LOGGER.info("Caught {} on keystore aliases {}: {}", e.getClass().getName(), this.keystoreFilename, e.getMessage());
            throw e;
        }

        while (aliases.hasMoreElements()) {
            final String alias = aliases.nextElement();
            try {
                keyStore.deleteEntry(alias);
            } catch (KeyStoreException e) {
                LOGGER.info("Caught {} on delete keystore alias {}: {}", e.getClass().getName(), alias, e.getMessage());
                throw e;
            }
        }
    }

    private String writeKeyStoreNewFile(final KeyStore keyStore, final String filename) {
        return writeKeyStore(keyStore, StringUtil.isNullOrEmpty(filename) ? createTempFile() : createFile(filename));
    }

    private String writeKeyStore(final KeyStore keyStore, final String filename) {
        try (final FileOutputStream outputStream = new FileOutputStream(filename)) {
            keyStore.store(outputStream, keystorePassword.toCharArray());
            return filename;
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName(), e);
            throw new IllegalStateException("Unable to write to keystore", e);
        }
    }

    /**
     * Read text from a file, delimited by 'begin' and 'end'. A file may contain multiple delimited texts.
     * @param filename
     * @param begin
     * @param end
     * @return
     */
    private List<String> readTextFromFile(final String filename, final String begin, final String end) {
        try {
            final List<String> results = Lists.newArrayList();
            final Iterator<String> lines = Files.lines(new File(filename).toPath()).iterator();
            while (lines.hasNext()) {
                String line = lines.next();
                if (line.startsWith(begin)) {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(line).append('\n');
                    while (lines.hasNext()) {
                        line = lines.next();
                        builder.append(line).append('\n');
                        if (line.startsWith(end)) {
                            results.add(builder.toString());
                            break;
                        }
                    }
                }
            }
            return results;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to read text from file: %s", filename), e);
        }
    }

    private String createFile(final String fileName) {
        try {
            final File keyStoreFile = new File(fileName);
            if (!keyStoreFile.exists()){
                if (!keyStoreFile.createNewFile()){
                    LOGGER.error(String.format("Unable to create %s file", keyStoreFile));
                    throw new IllegalStateException(String.format("Unable to create %s file", keyStoreFile));
                }
            }
            return keyStoreFile.toString();
        } catch (IOException e) {
            LOGGER.error(e.getClass().getName(), e);
            throw new IllegalStateException("Unable to create keystore", e);
        }
    }

    private String createTempFile() {
        try {
            return Files.createTempFile("keystore", ".jks").toString();
        } catch (IOException e) {
            LOGGER.error(e.getClass().getName(), e);
            throw new IllegalStateException("Unable to create temporary keystore", e);
        }
    }

    private boolean exists(final String filename) {
        return !StringUtils.isEmpty(filename) && (new File(filename)).exists();
    }

    private boolean isNullOrEmpty(final String[] stringArray) {
        return (stringArray == null) || (stringArray.length == 0);
    }
}
