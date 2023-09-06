package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ClientCertificateExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCertificateExtractor.class);

    static final String HEADER_SSL_CLIENT_CERT = "SSL_CLIENT_CERT";
    static final String HEADER_SSL_CLIENT_VERIFY = "SSL_CLIENT_VERIFY";

    public static Optional<X509CertificateWrapper> getClientCertificate(final HttpServletRequest request,
                                                                        final DateTimeProvider dateTimeProvider) {
        final String sslClientCert = request.getHeader(HEADER_SSL_CLIENT_CERT);

        if (StringUtils.isBlank(sslClientCert)) {
            return Optional.empty();
        }

        if (!hasAcceptableVerifyHeader(request)) {
            return Optional.empty();
        }

        return getX509Certificate(sslClientCert, dateTimeProvider);
    }

    private static boolean hasAcceptableVerifyHeader(final HttpServletRequest request) {
        final String sslClientVerify = request.getHeader(HEADER_SSL_CLIENT_VERIFY);

        return StringUtils.isNotEmpty(sslClientVerify) &&
                ("GENEROUS".equals(sslClientVerify) ||
                "SUCCESS".equals(sslClientVerify) ||
                sslClientVerify.startsWith("FAILED"));
    }

    private static Optional<X509CertificateWrapper> getX509Certificate(final String certificate,
                                                                final DateTimeProvider dateTimeProvider) {
        String fingerprint;
        try {
            // the PEM cert provided by Apache in SSL_CLIENT_CERT has spaces that JCA doesn't like so we decode it ourselves:
            final byte[] bytes = Base64.decodeBase64(
                    certificate
                    .replace(X509CertificateWrapper.X509_HEADER, "")
                    .replace(X509CertificateWrapper.X509_FOOTER, "")
                    .replaceAll(" ", "")
            );
            // TODO we should probably let the servlet container handle this for us and just use javax.servlet.request.X509Certificate

            final X509CertificateWrapper x509CertificateWrapper = X509CertificateWrapper.parse(bytes);
            fingerprint = x509CertificateWrapper.getFingerprint();

            if (x509CertificateWrapper.isNotYetValid(dateTimeProvider)) {
                LOGGER.info("Client certificate {} is not yet valid", fingerprint);
                return Optional.empty();
            }

            if (x509CertificateWrapper.isExpired(dateTimeProvider)) {
                LOGGER.info("Client certificate {} has expired", fingerprint);
                return Optional.empty();
            }

            return Optional.of(x509CertificateWrapper);
        } catch (IllegalArgumentException e) {
            LOGGER.info("Invalid X.509 certificate");
        }

        return Optional.empty();
    }
}
