package net.ripe.db.whois.api.rest;

import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import org.eclipse.jetty.server.SecureRequestCustomizer;

import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientCertificateExtractor {

    @Nullable
    public static List<X509CertificateWrapper> getClientCertificates(final HttpServletRequest request) {
        final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(SecureRequestCustomizer.JAKARTA_SERVLET_REQUEST_X_509_CERTIFICATE);
        if (certificates == null || certificates.length == 0) {
            return null;
        }
        return Arrays.stream(certificates)
                .map(certificate -> new X509CertificateWrapper(certificate))
                .collect(Collectors.toList());
    }
}
