package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.credentials.Credential;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.update.keycert.PgpSignedMessage;
import org.bouncycastle.openpgp.PGPPublicKey;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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

    public static PgpCredential createOfferedCredential(@Nullable final String clearText) {
        return createOfferedCredential(clearText, StandardCharsets.ISO_8859_1);
    }

    public static PgpCredential createOfferedCredential(@Nullable final String clearText, final Charset charset) {
        return new PgpCredential(PgpSignedMessage.parse(clearText, charset));
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PgpCredential that = (PgpCredential) o;

        return Objects.equals(keyId, that.keyId) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, keyId);
    }

    @Override
    public String toString() {
        return "PgpCredential{" +
                "keyId='" + keyId + '\'' +
                '}';
    }
}
