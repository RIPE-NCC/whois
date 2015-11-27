package net.ripe.db.whois.update.keycert;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X509CertificateWrapperTest {

    @Mock DateTimeProvider dateTimeProvider;
    private RpslObject x509Keycert;
    private RpslObject anotherX509Keycert;
    private RpslObject pgpKeycert;

    @Before
    public void setup() throws IOException {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.parse("2014-01-02"));
        x509Keycert = RpslObject.parse(getResource("keycerts/X509-3465.TXT"));
        anotherX509Keycert = RpslObject.parse(getResource("keycerts/X509-1.TXT"));
        pgpKeycert = RpslObject.parse(getResource("keycerts/PGPKEY-28F6CD6C.TXT"));
    }

    @Test
    public void isX509Key() throws IOException {
        assertThat(X509CertificateWrapper.looksLikeX509Key(x509Keycert), is(true));
        assertThat(X509CertificateWrapper.looksLikeX509Key(pgpKeycert), is(false));
    }

    @Test
    public void isEquals() {
        X509CertificateWrapper subject = X509CertificateWrapper.parse(x509Keycert);
        X509CertificateWrapper another = X509CertificateWrapper.parse(anotherX509Keycert);

        assertThat(subject.equals(subject), is(true));
        assertThat(subject.equals(another), is(false));
    }

    @Test
    public void getMethod() {
        X509CertificateWrapper subject = X509CertificateWrapper.parse(x509Keycert);

        assertThat(subject.getMethod(), is("X509"));
    }

    @Test
    public void getOwner() {
        X509CertificateWrapper subject = X509CertificateWrapper.parse(x509Keycert);

        assertThat(subject.getOwners(), containsInAnyOrder("/C=NL/ST=Some-State/O=BOGUS"));
    }

    @Test
    public void getFingerprint() {
        X509CertificateWrapper subject = X509CertificateWrapper.parse(x509Keycert);

        assertThat(subject.getFingerprint(),
                is("16:4F:B6:A4:D9:BC:0C:92:D4:48:13:FE:B6:EF:E2:82"));
    }

    @Test
    public void notYetValid() {
        X509CertificateWrapper subject = X509CertificateWrapper.parse(x509Keycert);

        assertThat(subject.isNotYetValid(dateTimeProvider), is(false));

        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now().minusYears(100));
        assertThat(subject.isNotYetValid(dateTimeProvider), is(true));
    }

    @Test
    public void isExpired() {
        X509CertificateWrapper subject = X509CertificateWrapper.parse(x509Keycert);

        assertThat(subject.isExpired(dateTimeProvider), is(false));

        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now().plusYears(100));
        assertThat(subject.isExpired(dateTimeProvider), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCertificateBase64IsTruncated() {
        final RpslObject keycert = RpslObject.parse(
                "key-cert:        X509-3465\n" +
                "method:          X509\n" +
                "owner:           /description=570433-veZWA34E9nftz1i7/C=PL/ST=Foo/L=Bar/CN=Test/emailAddress=noreply@test.com\n" +
                "fingerpr:        07:AC:10:C0:22:64:84:E7:00:18:5A:29:F0:54:2C:F3\n" +
                "certif:          -----BEGIN CERTIFICATE-----\n" +
                "certif:          MIIHaTCCBlGgAwIBAgICGSowDQYJKoZIhvcNAQEFBQAwgYwxCzAJBgNVBAYTAklM\n" +
                "certif:          UK1J2P3MpSIP8TFJp8fIJDlad3hoRsZeNxuV6D8=\n" +
                "certif:          -----END CERTIFICATE-----\n" +
                "mnt-by:          UPD-MNT\n" +
                "changed:         noreply@ripe.net 20111128\n" +
                "source:          TEST");

        X509CertificateWrapper.parse(keycert);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCertificateNoBase64Data() {
        final RpslObject keycert = RpslObject.parse(
                "key-cert:        X509-3465\n" +
                "method:          X509\n" +
                "owner:           /description=570433-veZWA34E9nftz1i7/C=PL/ST=Foo/L=Bar/CN=Test/emailAddress=noreply@test.com\n" +
                "fingerpr:        07:AC:10:C0:22:64:84:E7:00:18:5A:29:F0:54:2C:F3\n" +
                "certif:          -----BEGIN CERTIFICATE-----\n" +
                "certif:          -----END CERTIFICATE-----\n" +
                "mnt-by:          UPD-MNT\n" +
                "changed:         noreply@ripe.net 20111128\n" +
                "source:          TEST");

        X509CertificateWrapper.parse(keycert);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCertificateInvalidBase64Data() {
        final RpslObject keycert = RpslObject.parse(
                "key-cert:        X509-3465\n" +
                "method:          X509\n" +
                "owner:           /description=570433-veZWA34E9nftz1i7/C=PL/ST=Foo/L=Bar/CN=Test/emailAddress=noreply@test.com\n" +
                "fingerpr:        07:AC:10:C0:22:64:84:E7:00:18:5A:29:F0:54:2C:F3\n" +
                "certif:          -----BEGIN CERTIFICATE-----\n" +
                "certif:          xxx\n" +
                "certif:          -----END CERTIFICATE-----\n" +
                "mnt-by:          UPD-MNT\n" +
                "changed:         noreply@ripe.net 20111128\n" +
                "source:          TEST");

        X509CertificateWrapper.parse(keycert);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCertificateNoCertifLines() {
        final RpslObject keycert = RpslObject.parse(
                "key-cert:        X509-3465\n" +
                "method:          X509\n" +
                "owner:           /description=570433-veZWA34E9nftz1i7/C=PL/ST=Foo/L=Bar/CN=Test/emailAddress=noreply@test.com\n" +
                "fingerpr:        07:AC:10:C0:22:64:84:E7:00:18:5A:29:F0:54:2C:F3\n" +
                "mnt-by:          UPD-MNT\n" +
                "changed:         noreply@ripe.net 20111128\n" +
                "source:          TEST");

        X509CertificateWrapper.parse(keycert);
    }

    private String getResource(final String resourceName) throws IOException {
        return IOUtils.toString(new ClassPathResource(resourceName).getInputStream());
    }
}
