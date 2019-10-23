package net.ripe.db.whois.update.keycert;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.DateUtil;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class X509CertificateWrapper implements KeyWrapper {
    private static final String X509_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String X509_FOOTER = "-----END CERTIFICATE-----";

    private static final String METHOD = "X509";

    private static final CertificateFactory X509_CERTIFICATE_FACTORY;
    static {
        try {
            X509_CERTIFICATE_FACTORY = CertificateFactory.getInstance("x.509");
        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        }
    }

    private final X509Certificate certificate;

    private X509CertificateWrapper(final X509Certificate certificate) {
        this.certificate = certificate;
    }

    public static X509CertificateWrapper parse(final RpslObject rpslObject) {
        if (!looksLikeX509Key(rpslObject)) {
            throw new IllegalArgumentException("The supplied object has no key");
        }

        final byte[] bytes = RpslObjectFilter.getCertificateFromKeyCert(rpslObject).getBytes(StandardCharsets.ISO_8859_1);
        return parse(bytes);
    }

    static X509CertificateWrapper parse(final String certificate) {
        return parse(certificate.getBytes());
    }

    static X509CertificateWrapper parse(final byte[] certificate) {
        final Certificate result;
        try {
            result = X509_CERTIFICATE_FACTORY.generateCertificate(new ByteArrayInputStream(certificate));
        } catch (CertificateException e) {
            throw new IllegalArgumentException(e);
        }

        if (!(result instanceof X509Certificate)) {
            throw new IllegalArgumentException("Not an x.509 certificate");
        }

        return new X509CertificateWrapper((X509Certificate)result);
    }

    static boolean looksLikeX509Key(final RpslObject rpslObject) {
        final String pgpKey = RpslObjectFilter.getCertificateFromKeyCert(rpslObject);
        return pgpKey.indexOf(X509_HEADER) != -1 && pgpKey.indexOf(X509_FOOTER) != -1;
    }

    private String convertFromRfc2253ToCompatFormat(String name) {
        //Convert from RFC2253 format Subject DN returned by the JDK, to match the OpenSSL compat format in the database.
        if (name != null && name.length() > 0) {
            String result = "/" + name;
            result = result.replaceAll("[,]([a-zA-Z]*)[=]", "/$1=");
            result = result.replace("/E=", "/EMAILADDRESS=");
            result = result.replace("/2.5.4.13=", "/DESCRIPTION=");
            result = result.replace("/SURNAME=", "/SN=");
            result = result.replace("/GIVENNAME=", "/GN=");
            result = result.replace("+SERIALNUMBER=", "/SERIALNUMBER=");
            result = result.replace("/STREET=", "/STREETADDRESS=");
            return result;
        }
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final X509CertificateWrapper that = (X509CertificateWrapper) o;

        return Objects.equals(certificate, that.certificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificate);
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public String getMethod() {
        return METHOD;
    }

    @Override
    public List<String> getOwners() {
        return Lists.newArrayList(convertFromRfc2253ToCompatFormat(certificate.getSubjectDN().getName()));
    }

    @Override
    public String getFingerprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] der = certificate.getEncoded();
            md.update(der);
            byte[] digest = md.digest();

            StringBuilder builder = new StringBuilder();
            for (byte next : digest) {
                if (builder.length() > 0) {
                    builder.append(':');
                }
                builder.append(String.format("%02X", next));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Invalid X509 Certificate", e);
        } catch (CertificateEncodingException e) {
            throw new IllegalArgumentException("Invalid X509 Certificate", e);
        }
    }

    public boolean isNotYetValid(final DateTimeProvider dateTimeProvider) {
        final LocalDateTime notBefore = DateUtil.fromDate(certificate.getNotBefore());
        return notBefore.isAfter(dateTimeProvider.getCurrentDateTime());
    }

    public boolean isExpired(final DateTimeProvider dateTimeProvider) {
        final LocalDateTime notAfter = DateUtil.fromDate(certificate.getNotAfter());
        return notAfter.isBefore(dateTimeProvider.getCurrentDateTime());
    }
}
