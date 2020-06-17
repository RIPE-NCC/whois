package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

public class ClientCertificateExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCertificateExtractor.class);

    private final static String HEADER_SSL_CLIENT_CERT = "SSL_CLIENT_CERT";
    private final static String HEADER_SSL_CLIENT_VERIFY = "SSL_CLIENT_VERIFY";

    private static final String X509_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String X509_FOOTER = "-----END CERTIFICATE-----";

    public static Optional<X509Certificate> getClientCertificate(final HttpServletRequest request) {
        final String sslClientCert = request.getHeader(HEADER_SSL_CLIENT_CERT);

        if (StringUtils.isBlank(sslClientCert)) {
            return Optional.empty();
        }

        final String sslClientVerify = request.getHeader(HEADER_SSL_CLIENT_VERIFY);
        if (!"GENEROUS".equals(sslClientVerify) && !"SUCCESS".equals(sslClientVerify)) {
            return Optional.empty();
        }

        return getX509Certificate(sslClientCert);
    }

    private static Optional<X509Certificate> getX509Certificate(final String certificate) {
        String fingerprint = null;
        try {
            final InputStream input = new ByteArrayInputStream(
                    decodeBase64(certificate.replace(X509_HEADER, "").replace(X509_FOOTER, ""))
            );

            final X509Certificate x509 =
                    (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(input);

            fingerprint = X509CertificateWrapper.wrap(x509).getFingerprint();

            x509.checkValidity();

            return Optional.of(x509);
        } catch (CertificateExpiredException cee) {
            LOGGER.info("Client certificate {} has expired", fingerprint);
        } catch (CertificateNotYetValidException cnyve) {
            LOGGER.info("Client certificate {} is not yet valid", fingerprint);
        } catch (CertificateException e) {
            LOGGER.info("Invalid X.509 certificate");
        }

        return Optional.empty();
    }
}
