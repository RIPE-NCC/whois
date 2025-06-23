package net.ripe.db.whois.api.rest;

import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ClientCertificateExtractor {

    @Nullable
    public static List<X509CertificateWrapper> getClientCertificates(final HttpServletRequest request) {
        return Collections.emptyList();
// TODO: Fix ClassCastException
//        final X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(SecureRequestCustomizer.X509_ATTRIBUTE);
//        if (certificates == null || certificates.length == 0) {
//            return null;
//        }
//        return Arrays.stream(certificates)
//                .map(certificate -> new X509CertificateWrapper(certificate))
//                .collect(Collectors.toList());
    }
}
