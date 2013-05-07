package net.ripe.db.whois.common.rpsl;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RpslObjectFilterTest {
    RpslObject mntner;
    RpslObjectFilter subject;

    @Before
    public void setUp() throws Exception {
        mntner = RpslObject.parse("" +
                "mntner:          DEV-MNT\n" +
                "descr:           DEV maintainer\n" +
                "admin-c:         VM1-DEV\n" +
                "tech-c:          VM1-DEV\n" +
                "upd-to:          v.m@example.net\n" +
                "mnt-nfy:         auto@example.net\n" +
                "auth:            MD5-PW $1$q8Su3Hq/$rJt5M3TNLeRE4UoCh5bSH/\n" +
                "remarks:         password: secret\n" +
                "mnt-by:          DEV-MNT\n" +
                "referral-by:     DEV-MNT\n" +
                "changed:         BECHA@example.net 20101010\n" +
                "source:          DEV\n"
        );

        subject = new RpslObjectFilter(mntner);
    }

    @Test(expected = AuthenticationException.class)
    public void getCertificateFromKeyCert() {
        subject.getCertificateFromKeyCert();
    }

    @Test
    public void getSource() {
        assertThat(subject.getSource().toString(), is("DEV"));
    }

    @Test
    public void isFiltered() {
        final boolean filtered = RpslObjectFilter.isFiltered(mntner);
        assertThat(filtered, is(false));
    }

    @Test
    public void getCertificateOnlyOneCertifAttribute() {
        RpslObject keycert = RpslObject.parse(
                "key-cert: X509-1\n" +
                        "certif: -----BEGIN CERTIFICATE-----\n" +
                        "        MIIHaTCCBlGgAwIBAgICGSowDQYJKoZIhvcNAQEFBQAwgYwxCzAJBgNVBAYTAklM\n" +
                        "        -----END CERTIFICATE-----");

        final String certificate = (new RpslObjectFilter(keycert)).getCertificateFromKeyCert();

        assertThat(certificate, is(
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIIHaTCCBlGgAwIBAgICGSowDQYJKoZIhvcNAQEFBQAwgYwxCzAJBgNVBAYTAklM\n" +
                        "-----END CERTIFICATE-----\n"));
    }

    @Test
    public void getCertificateMultipleCertifAttributes() {
        RpslObject keycert = RpslObject.parse(
                "key-cert: X509-1\n" +
                        "certif: -----BEGIN CERTIFICATE-----\n" +
                        "certif: MIIHaTCCBlGgAwIBAgICGSowDQYJKoZIhvcNAQEFBQAwgYwxCzAJBgNVBAYTAklM\n" +
                        "certif: -----END CERTIFICATE-----");

        final String certificate = (new RpslObjectFilter(keycert)).getCertificateFromKeyCert();

        assertThat(certificate, is(
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIIHaTCCBlGgAwIBAgICGSowDQYJKoZIhvcNAQEFBQAwgYwxCzAJBgNVBAYTAklM\n" +
                        "-----END CERTIFICATE-----\n"));
    }

    @Test
    public void certificateContinuationLines() {
        RpslObject keycert = RpslObject.parse(
                "key-cert: X509-1\n" +
                        "certif: -----BEGIN CERTIFICATE-----\n" +
                        "        MIIHaTCCBlGgAwIBAgICGSowDQYJKoZIhvcNAQEFBQAwgYwxCzAJBgNVBAYTAklM\n" +
                        "\t      MRYwFAYDVQQKEw1TdGFydENvbSBMdGQuMSswKQYDVQQLEyJTZWN1cmUgRGlnaXRh\n" +
                        "+       -----END CERTIFICATE-----");

        final String certificate = (new RpslObjectFilter(keycert)).getCertificateFromKeyCert();

        assertThat(certificate, is(
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIIHaTCCBlGgAwIBAgICGSowDQYJKoZIhvcNAQEFBQAwgYwxCzAJBgNVBAYTAklM\n" +
                        "MRYwFAYDVQQKEw1TdGFydENvbSBMdGQuMSswKQYDVQQLEyJTZWN1cmUgRGlnaXRh\n" +
                        "-----END CERTIFICATE-----\n"));
    }
}
