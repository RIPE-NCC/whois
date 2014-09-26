package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.update.keycert.PgpSignedMessage;
import org.bouncycastle.openpgp.PGPPublicKey;

import javax.annotation.Nullable;
import java.nio.charset.Charset;

public class PgpCredential implements Credential {

    private final PgpSignedMessage message;
    private final String keyId;

    PgpCredential(final PgpSignedMessage message) {
        this.message = message;
        this.keyId = null;
    }

    PgpCredential(final String keyId) {
        this.message = null;
        this.keyId = keyId;
    }

    public static PgpCredential createKnownCredential(@Nullable final String keyId) {
        return new PgpCredential(keyId);
    }

    public static PgpCredential createOfferedCredential(@Nullable final String clearText, final Charset charset) {
        return new PgpCredential(PgpSignedMessage.parse(clearText, charset));
    }
               //TODO : DELETE
    public static PgpCredential createOfferedCredential(@Nullable final String clearText) {
        return new PgpCredential(PgpSignedMessage.parse(clearText));
    }

    public static PgpCredential createOfferedCredential(final String signedData, final String signature, final Charset charset) {
        return new PgpCredential(PgpSignedMessage.parse(signedData, signature, charset));
    }

    @Nullable
    public String getKeyId() {
        return message != null ? message.getKeyId() : keyId;
    }

    public String getContent() {
        return message.getSignedContent();
    }

    public boolean verify(final PGPPublicKey publicKey) {
        return message.verify(publicKey);
    }

    public boolean verifySigningTime(final DateTimeProvider dateTimeProvider) {
        return message.verifySigningTime(dateTimeProvider);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PgpCredential that = (PgpCredential) o;

        return (keyId != null ? keyId.equals(that.keyId) : that.keyId == null)
               && (message != null ? message.equals(that.message) : that.message == null);
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (keyId != null ? keyId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PgpCredential{" +
                "keyId='" + keyId + '\'' +
                '}';
    }
}
