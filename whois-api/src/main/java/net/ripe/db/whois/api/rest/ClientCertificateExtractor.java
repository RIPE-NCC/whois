package net.ripe.db.whois.api.rest;

import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;

import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClientCertificateExtractor {

    @Nullable
    public static List<X509CertificateWrapper> getClientCertificates(final HttpServletRequest request) {

        final Object certAttr = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (certAttr instanceof X509Certificate[] certificates) {

            if (certificates.length == 0) {
                return null;
            }
            return Arrays.stream(certificates)
                    .map(X509CertificateWrapper::new)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}