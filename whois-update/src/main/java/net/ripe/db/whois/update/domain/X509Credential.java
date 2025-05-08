package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.credentials.Credential;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.update.keycert.X509SignedMessage;

import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.Objects;

public class X509Credential implements Credential {

    private final X509SignedMessage signedMessage;
    private final String keyId;

    private X509Credential(final String signedData, final String signature) {
        this.signedMessage = new X509SignedMessage(signedData, signature);
        this.keyId = null;
    }

    private X509Credential(final String keyId) {
        this.signedMessage = null;
        this.keyId = keyId;
    }

    public static X509Credential createKnownCredential(final String keyId) {
        return new X509Credential(keyId);
    }

    public static X509Credential createOfferedCredential(final String signedData, final String signature) {
        return new X509Credential(signedData, signature);
    }

    @Nullable
    public String getKeyId() {
        return keyId;
    }

    public boolean verify(X509Certificate certificate) {
        if (signedMessage == null) {
            throw new IllegalStateException("No signed message to verify.");
        }

        return signedMessage.verify(certificate);
    }

    public boolean verifySigningTime(final DateTimeProvider dateTimeProvider) {
        if (signedMessage == null) {
            throw new IllegalStateException("No signed message.");
        }

        return signedMessage.verifySigningTime(dateTimeProvider);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final X509Credential that = (X509Credential) o;

        return Objects.equals(keyId, that.keyId) &&
                Objects.equals(signedMessage, that.signedMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signedMessage, keyId);
    }

    @Override
    public String toString() {
        return "X509Credential{" +
                "keyId='" + keyId + '\'' +
                '}';
    }
}
