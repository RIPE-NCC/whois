package net.ripe.db.whois.common.oauth;

import com.google.common.base.MoreObjects;

public class OidcSession extends OAuthSession{

    OidcSession(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("aud", getAud())
                .add("azp", getAzp())
                .add("email", getEmail())
                .add("scopes", getScopes())
                .add("jti", getJti())
                .add("errorStatus", getErrorStatus())
                .toString();
    }
}
