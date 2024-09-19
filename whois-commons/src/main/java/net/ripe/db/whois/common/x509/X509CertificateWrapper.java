package net.ripe.db.whois.common.x509;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.DateUtil;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;


public final class X509CertificateWrapper implements KeyWrapper {

    private static final Provider PROVIDER = new BouncyCastleProvider();

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Base64.Encoder BASE64_ENCODER = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());

    public static final String X509_HEADER = "-----BEGIN CERTIFICATE-----";
    public static final String X509_FOOTER = "-----END CERTIFICATE-----";

    private static final String METHOD = "X509";

    private final X509Certificate certificate;

    public X509CertificateWrapper(final X509Certificate certificate) {
        this.certificate = certificate;
    }

    public static X509CertificateWrapper parse(final RpslObject rpslObject) {
        if (!looksLikeX509Key(rpslObject)) {
            throw new IllegalArgumentException("The supplied object has no key");
        }

        return parse(RpslObjectFilter.getCertificateFromKeyCert(rpslObject).getBytes(StandardCharsets.ISO_8859_1));
    }

    public static X509CertificateWrapper parse(final String certificate) {
        return parse(certificate.getBytes());
    }

    public static X509CertificateWrapper parse(final byte[] certificate) {
        final X509Certificate result;
        try {
            final CertificateFactory factory = CertificateFactory.getInstance("X.509", PROVIDER);
            result = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certificate));
            if (result == null) {
                throw new IllegalArgumentException("Invalid X509 Certificate");
            }
        } catch (CertificateException e) {
            throw new IllegalArgumentException("Invalid X509 Certificate", e);
        }
        return new X509CertificateWrapper(result);
    }

    public static boolean looksLikeX509Key(final RpslObject rpslObject) {
        final String pgpKey = RpslObjectFilter.getCertificateFromKeyCert(rpslObject);
        return pgpKey.contains(X509_HEADER) && pgpKey.contains(X509_FOOTER);
    }

    private String convertFromRfc2253ToCompatFormat(final String name) {
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
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] der = certificate.getEncoded();
            md.update(der);
            final byte[] digest = md.digest();

            final StringBuilder builder = new StringBuilder();
            for (byte next : digest) {
                if (builder.length() > 0) {
                    builder.append(':');
                }
                builder.append(String.format("%02X", next));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            throw new IllegalArgumentException("Invalid X509 Certificate", e);
        }
    }

    public String getCertificateAsString() {
        final StringBuilder builder = new StringBuilder();
        try {
            return builder.append(X509_HEADER)
                .append(LINE_SEPARATOR)
                .append(new String(BASE64_ENCODER.encode(certificate.getEncoded())))
                .append(LINE_SEPARATOR)
                .append(X509_FOOTER)
                .toString();
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
