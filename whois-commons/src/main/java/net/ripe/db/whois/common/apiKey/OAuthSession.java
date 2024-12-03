package net.ripe.db.whois.common.apiKey;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.List;

public class OAuthSession {

    private final String application;

    private final String email;

    private final String accessKey;

    private final String uuid;

    private final LocalDateTime expirationDate;

    private final List<String> scopes;

    public OAuthSession(final String application, final String accessKey, final String email, final String uuid, final LocalDateTime expirationDate, final List<String> scopes) {
        this.application = application;
        this.email = email;
        this.uuid = uuid;
        this.expirationDate = expirationDate;
        this.scopes = scopes;
        this.accessKey = accessKey;
    }

    public String getApplication() {
        return application;
    }

    public String getEmail() {
        return email;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public List<String> getScopes() {
        return scopes;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("application", application)
                .add("accessKey", accessKey)
                .add("email", email)
                .add("uuid", uuid)
                .add("expirationDate", expirationDate.toString())
                .add("scopes", scopes)
                .toString();
    }


   public static class ScopeFormatter {

        final String appName;
        final String scopeType;
        final String scopeKey;

        public ScopeFormatter(final String scope) {
            final String[] parts = scope.split(":|\\.");
            this.appName = parts[0];
            this.scopeType = parts[1];
            this.scopeKey = parts[2];
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
}
