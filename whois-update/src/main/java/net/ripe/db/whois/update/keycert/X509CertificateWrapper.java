package net.ripe.db.whois.update.keycert;


import com.google.common.base.Charsets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.x509.util.StreamParsingException;
import org.joda.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class X509CertificateWrapper implements KeyWrapper {
    private static final String X509_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String X509_FOOTER = "-----END CERTIFICATE-----";

    private static final String METHOD = "X509";

    private final X509Certificate certificate;

    private X509CertificateWrapper(final X509Certificate certificate) {
        this.certificate = certificate;
    }

    public static X509CertificateWrapper parse(final RpslObject rpslObject) {
        if (!looksLikeX509Key(rpslObject)) {
            throw new IllegalArgumentException("The supplied object has no key");
        }

        try {
            final byte[] bytes = new RpslObjectFilter(rpslObject).getCertificateFromKeyCert().getBytes(Charsets.UTF_8);

            X509CertParser parser = new X509CertParser();
            parser.engineInit(new ByteArrayInputStream(bytes));
            X509Certificate result = (X509Certificate) parser.engineRead();

            if (result == null) {
                throw new IllegalArgumentException("Invalid X509 Certificate");
            }

            return new X509CertificateWrapper(result);

        } catch (StreamParsingException e) {
            throw new IllegalArgumentException("Error parsing X509 certificate from key-cert object", e);
        }
    }

    static boolean looksLikeX509Key(final RpslObject rpslObject) {
        final String pgpKey = new RpslObjectFilter(rpslObject).getCertificateFromKeyCert();
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
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return ((X509CertificateWrapper) o).getCertificate().equals(
                this.getCertificate());
    }

    @Override
    public int hashCode() {
        return certificate != null ? certificate.hashCode() : 0;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public String getMethod() {
        return METHOD;
    }

    @Override
    public String getOwner() {
        return convertFromRfc2253ToCompatFormat(certificate.getSubjectDN().getName());
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
        final LocalDateTime notBefore = new LocalDateTime(certificate.getNotBefore());
        return notBefore.isAfter(dateTimeProvider.getCurrentDateTime());
    }

    public boolean isExpired(final DateTimeProvider dateTimeProvider) {
        final LocalDateTime notAfter = new LocalDateTime(certificate.getNotAfter());
        return notAfter.isBefore(dateTimeProvider.getCurrentDateTime());
    }
}
