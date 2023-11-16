package net.ripe.db.whois.api.httpserver;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
        final CertificatePrivateKeyPair testData = new CertificatePrivateKeyPair();

        final WhoisKeystore subject = generateWhoisKeystore(testData);

        assertThat(subject.getKeystore(), is(notNullValue()));
        assertThat(subject.getPassword(), is(notNullValue()));
    }

    @Test
    public void keystore_outdated() throws Exception {
        final CertificatePrivateKeyPair testData = new CertificatePrivateKeyPair("cn=one");

        final WhoisKeystore subject = generateWhoisKeystore(testData);

        assertThat(subject.isKeystoreOutdated(), is(false));
        assertThat(loadCertificateFromKeystore(subject.getKeystore(), subject.getPassword(), "CN=one"), is(notNullValue()));

        Uninterruptibles.sleepUninterruptibly(1L, TimeUnit.SECONDS);

        // generate new certificate
        final CertificatePrivateKeyPair moreTestData = new CertificatePrivateKeyPair("cn=two");
        Files.copy(new File(moreTestData.getCertificateFilename()).toPath(), new File(testData.getCertificateFilename()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        assertThat(subject.isKeystoreOutdated(), is(true));

        // load new certificate
        subject.reloadKeystore();
        assertThat(subject.isKeystoreOutdated(), is(false));
        assertThat(loadCertificateFromKeystore(subject.getKeystore(), subject.getPassword(), "CN=one"), is(nullValue()));
        assertThat(loadCertificateFromKeystore(subject.getKeystore(), subject.getPassword(), "CN=two"), is(notNullValue()));
    }


    // helper methods

    private static WhoisKeystore generateWhoisKeystore(final CertificatePrivateKeyPair testData) {
        return new WhoisKeystore(new String[]{testData.getPrivateKeyFilename()}, new String[]{testData.getCertificateFilename()}, null);
    }

    private static Certificate loadCertificateFromKeystore(final String keystoreFilename, final String keystorePassword, final String alias) {
        try {
            final KeyStore keystore = KeyStore.getInstance(new File(keystoreFilename), keystorePassword.toCharArray());
            return keystore.getCertificate(alias);
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
