package net.ripe.db.whois.common.oauth;

import com.google.common.base.MoreObjects;

public class APIKeySession extends OAuthSession {

    final private String keyId;

    public APIKeySession(final Builder builder) {
        super(builder);
        this.keyId = builder.keyId;
    }

    public String getKeyId() {
        return keyId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("aud", getAud())
                .add("keyId", keyId)
                .add("email", getEmail())
                .add("uuid", getUuid())
                .add("scopes", getScopes())
                .add("azp", getAzp())
                .add("jti", getJti())
                .add("errorStatus", getErrorStatus())
                .toString();
    }
}
