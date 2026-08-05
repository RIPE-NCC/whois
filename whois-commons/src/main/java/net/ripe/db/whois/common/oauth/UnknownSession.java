package net.ripe.db.whois.common.oauth;

import com.google.common.base.MoreObjects;

public class UnknownSession extends AbstractOAuthSession {

    UnknownSession(final UnknownSession.Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("errorStatus", getErrorStatus())
                .toString();
    }

    public static class Builder extends AbstractOAuthSession.Builder<UnknownSession.Builder, UnknownSession>{

        @Override
        protected UnknownSession.Builder self() {
            return this;
        }

        @Override
        public UnknownSession build() {
            return new UnknownSession(this);
        }
    }
}
