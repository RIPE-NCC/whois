package net.ripe.db.whois.common.credentials;

import net.ripe.db.whois.common.x509.X509CertificateWrapper;

import java.security.cert.X509Certificate;
import java.util.Objects;

public class ClientCertificateCredential implements Credential {

    private final X509CertificateWrapper x509CertificateWrapper;


    public ClientCertificateCredential(final X509CertificateWrapper x509CertificateWrapper) {
        this.x509CertificateWrapper = x509CertificateWrapper;
    }

    public static Credential createOfferedCredential(final X509CertificateWrapper x509CertificateWrapper) {
        return new ClientCertificateCredential(x509CertificateWrapper);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ClientCertificateCredential that = (ClientCertificateCredential) o;

        return Objects.equals(this.x509CertificateWrapper.getCertificate(), that.getCertificate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x509CertificateWrapper.getCertificate());
    }

    @Override
    public String toString() {
        return "ClientCertificateCredential";
    }

    public String getFingerprint() {
        return this.x509CertificateWrapper.getFingerprint();
    }

    public X509Certificate getCertificate() {
        return this.x509CertificateWrapper.getCertificate();
    }

}
