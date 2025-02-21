package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<String> aud;

    private final String email;

    private final String keyId;

    private final String uuid;

    private final List<ScopeFormatter> scopes;

    public OAuthSession() {
        this.aud = Collections.emptyList();
        this.email = null;
        this.keyId = null;
        this.uuid = null;
        this.scopes = Collections.emptyList();
    }

    public OAuthSession(final List<String> aud, final String keyId, final String email, final String uuid, final List<ScopeFormatter> scopes) {
        this.aud = aud;
        this.email = email;
        this.uuid = uuid;
        this.scopes = scopes;
        this.keyId = keyId;
    }

    public OAuthSession(final String keyId) {
        this.aud = Collections.emptyList();
        this.email = null;
        this.keyId = keyId;
        this.uuid = null;
        this.scopes = Collections.emptyList();
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

    public String getKeyId() {
        return keyId;
    }

    public List<ScopeFormatter> getScopes() {
        return scopes;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("aud", aud)
                .add("keyId", keyId)
                .add("email", email)
                .add("uuid", uuid)
                .add("scopes", scopes)
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
}
