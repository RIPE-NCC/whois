package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String application;

    private final String email;

    private final String accessKey;

    private final String uuid;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime expirationDate;

    private final List<String> scopes;

    public OAuthSession() {
        this.application = null;
        this.email = null;
        this.accessKey = null;
        this.uuid = null;
        this.scopes = null;
        this.expirationDate = null;
    }

    public OAuthSession(final String application, final String accessKey, final String email, final String uuid, final LocalDateTime expirationDate, final List<String> scopes) {
        this.application = application;
        this.email = email;
        this.uuid = uuid;
        this.expirationDate = expirationDate;
        this.scopes = scopes;
        this.accessKey = accessKey;
    }

    public OAuthSession(final String accesKey) {
        this.application = null;
        this.email = null;
        this.accessKey = accesKey;
        this.uuid = null;
        this.scopes = null;
        this.expirationDate = null;
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
                .add("expirationDate", expirationDate == null ? null : expirationDate.toString())
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

    public static OAuthSession from(final OAuthSession oAuthSession, final String accessKey) {
        return new OAuthSession(oAuthSession.getApplication(), accessKey, oAuthSession.getEmail(), oAuthSession.getUuid(), oAuthSession.getExpirationDate(), oAuthSession.getScopes());
    }
}
