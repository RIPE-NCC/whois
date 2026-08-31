package net.ripe.db.whois.api.rest;

import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import org.eclipse.jetty.ee11.servlet.ServletContextRequest;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClientCertificateExtractor {

    public static List<X509CertificateWrapper> getClientCertificates(final HttpServletRequest request) {
        final Object certAttr = request.getAttribute(ServletContextRequest.PEER_CERTIFICATES);
        if (certAttr instanceof X509Certificate[] certificates) {
            return Arrays.stream(certificates)
                    .map(X509CertificateWrapper::new)
                    // Only return the leaf (signing) certificate (the actual identity certificate used for the connection) which is always placed at index 0.
                    .limit(1)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
