package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] aud;

    private final String email;

    private final String accessKey;

    private final String uuid;

    private final String scope;

    public OAuthSession() {
        this.aud = null;
        this.email = null;
        this.accessKey = null;
        this.uuid = null;
        this.scope = null;
    }

    public OAuthSession(final String[] aud, final String accessKey, final String email, final String uuid, final String scope) {
        this.aud = aud;
        this.email = email;
        this.uuid = uuid;
        this.scope = scope;
        this.accessKey = accessKey;
    }

    public OAuthSession(final String accesKey) {
        this.aud = null;
        this.email = null;
        this.accessKey = accesKey;
        this.uuid = null;
        this.scope = null;
    }

    public String[] getAud() {
        return aud;
    }

    public String getEmail() {
        return email;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("aud", aud)
                .add("accessKey", accessKey)
                .add("email", email)
                .add("uuid", uuid)
                .add("scopes", scope)
                .toString();
    }

   public static class ScopeFormatter {

        final String appName;
        final String scopeType;
        final String scopeKey;

        public ScopeFormatter(final String scope) {
            final String[] parts = scope.split(":|\\.");

            if(parts.length == 0 || parts.length < 2) {
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

    public static OAuthSession from(final OAuthSession oAuthSession, final String accessKey) {
        return new OAuthSession(oAuthSession.getAud(), accessKey, oAuthSession.getEmail(), oAuthSession.getUuid(), oAuthSession.getScope());
    }
}
