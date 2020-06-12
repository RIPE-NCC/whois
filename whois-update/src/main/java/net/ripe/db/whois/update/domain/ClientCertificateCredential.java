package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.update.keycert.X509CertificateWrapper;

import java.security.cert.X509Certificate;
import java.util.Objects;

public class ClientCertificateCredential implements Credential {

    private final X509Certificate x509Certificate;
    private final String fingerprint;

    public ClientCertificateCredential(final X509Certificate x509Certificate) {
        this.x509Certificate = x509Certificate;
        this.fingerprint = X509CertificateWrapper.wrap(x509Certificate).getFingerprint();
    }

    public static Credential createOfferedCredential(X509Certificate clientCertificate) {
        return new ClientCertificateCredential(clientCertificate);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ClientCertificateCredential that = (ClientCertificateCredential) o;

        return Objects.equals(x509Certificate, that.x509Certificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x509Certificate);
    }

    @Override
    public String toString() {
        return "ClientCertificateCredential";
    }

    public String getFingerprint() {
        return fingerprint;
    }

}
