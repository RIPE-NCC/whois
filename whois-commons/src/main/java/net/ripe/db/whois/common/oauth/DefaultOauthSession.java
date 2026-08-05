package net.ripe.db.whois.common.oauth;

import com.google.common.base.MoreObjects;

public class DefaultOauthSession extends AbstractOAuthSession {

    DefaultOauthSession(final DefaultOauthSession.Builder builder) {
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

    public static class Builder extends AbstractOAuthSession.Builder<DefaultOauthSession.Builder, DefaultOauthSession>{

        @Override
        protected DefaultOauthSession.Builder self() {
            return this;
        }

        @Override
        public DefaultOauthSession build() {
            return new DefaultOauthSession(this);
        }
    }
}
