package net.ripe.db.whois.common.oauth;

import com.google.common.base.MoreObjects;

import java.util.List;

public abstract class AbstractOAuthSession {

    private final List<String> aud;

    private final String email;

    private final String uuid;

    private final List<String> scopes;

    private final String errorStatus;

    private final String azp;

    private final String jti;

    AbstractOAuthSession(final Builder builder) {
        this.aud = builder.aud;
        this.email = builder.email;
        this.uuid = builder.uuid;
        this.scopes = builder.scopes;
        this.errorStatus = builder.errorStatus;
        this.azp = builder.azp;
        this.jti = builder.jti;
    }

    public List<String> getAud() {
        return aud;
    }

    public String getEmail() {
        return email;
    }

    public String getUuid() {
        return uuid;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public String getJti() {
        return jti;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public String getAzp() {
        return azp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("aud", aud)
                .add("email", email)
                .add("uuid", uuid)
                .add("azp", azp)
                .add("scopes", scopes)
                .add("jti", jti)
                .add("errorStatus", errorStatus)
                .toString();
    }

    public static class ScopeFormatter {

        final String appName;
        final String scopeType;
        final String scopeKey;

        public ScopeFormatter(final String scope) {
            final String[] parts = scope.split(":|\\.");

            if (parts.length == 0 || parts.length < 2) {
                this.appName = null;
                this.scopeType = null;
                this.scopeKey = null;
            } else {
                this.appName = parts[0];
                this.scopeType = parts[1];
                this.scopeKey = parts[2];
            }
        }

        public String getScopeType() {
            return scopeType;
        }

        public String getScopeKey() {
            return scopeKey;
        }

        public String getAppName() {
            return appName;
        }
    }

    public abstract static class Builder<T extends Builder<T, P>, P extends AbstractOAuthSession> {

        protected List<String> aud;

        protected String email;

        protected String uuid;

        protected List<String> scopes;

        protected String errorStatus;

        protected String azp;

        protected String jti;

        protected String keyId;

        public T keyId(String keyId) {
            this.keyId = keyId;
            return self();
        }

        public T aud(List<String> aud) {
            this.aud = aud;
            return self();
        }

        public T jti(String jti) {
            this.jti = jti;
            return self();
        }

        public T email(String email) {
            this.email = email;
            return self();
        }

        public T uuid(String uuid) {
            this.uuid = uuid;
            return self();
        }

        public T scopes(List<String> scopes) {
            this.scopes = scopes;
            return self();
        }

        public T errorStatus(String errorStatus) {
            this.errorStatus = errorStatus;
            return self();
        }

        public T azp(String azp) {
            this.azp = azp;
            return self();
        }

        protected abstract T self();
        public abstract P build();
    }
}
