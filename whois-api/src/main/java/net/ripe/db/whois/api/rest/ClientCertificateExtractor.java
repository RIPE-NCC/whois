package net.ripe.db.whois.api.rest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class ClientCertificateExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCertificateExtractor.class);

    private final static String HEADER_SSL_CLIENT_CERT = "SSL_CLIENT_CERT";
    private final static String HEADER_SSL_CLIENT_VERIFY = "SSL_CLIENT_VERIFY";

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
        // TODO what to log as cert identifier (fingerprint, subjectDN?)
        Principal subject = null;
        try {
            final X509Certificate x509 = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(IOUtils.toInputStream(certificate));

            subject = x509.getSubjectDN();
            x509.checkValidity();


//            final String fingerprint = DigestUtils.md5Hex(x509.getEncoded());

            // TODO signature verification (both self signed and ca)
            //x509.verify(x509.getPublicKey());

            return Optional.of(x509);
        } catch (CertificateExpiredException cee) {
            LOGGER.info("Certificate {} has expired", subject);
        } catch (CertificateNotYetValidException cnyve) {
            LOGGER.info("Certificate {} is not yet valid", subject);
        } catch (CertificateException e) {
            LOGGER.info("Invalid X.509 certificate");
//        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
//            throw new IllegalArgumentException("Certificate signature verification failed");
        }
        return Optional.empty();
    }
}
