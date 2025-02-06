package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import io.netty.util.internal.StringUtil;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.ripe.db.whois.common.Environment;

import java.io.Serial;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] aud;

    private final String email;

    private final String keyId;

    private final String uuid;

    private final String scope;

    private final Environment env;

    public OAuthSession() {
        this.aud = null;
        this.email = null;
        this.keyId = null;
        this.uuid = null;
        this.scope = null;
        this.env = null;
    }

    public OAuthSession(final String[] aud, final String keyId, final String email, final String uuid, final String scope, final String environment) {
        this.aud = aud;
        this.email = email;
        this.uuid = uuid;
        this.scope = scope;
        this.keyId = keyId;
        this.env = Environment.valueOf(environment.toUpperCase());
    }

    public OAuthSession(final String keyId) {
        this.aud = null;
        this.email = null;
        this.keyId = keyId;
        this.uuid = null;
        this.scope = null;
        this.env = null;
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

    public String getKeyId() {
        return keyId;
    }

    public String getScope() {
        return scope;
    }

    public Environment getEnv() {
        return env;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("aud", aud)
                .add("keyId", keyId)
                .add("email", email)
                .add("uuid", uuid)
                .add("scope", scope)
                .add("environment", env)
                .toString();
    }

   public static class ScopeFormatter {

        final String appName;
        final ScopeType scopeType;
        final String scopeKey;

        public ScopeFormatter(final String scope) {
            final String[] parts = scope.split("[:.]");

            if (parts.length < 3){
                this.appName = null;
                this.scopeType = null;
                this.scopeKey = null;
            } else {
                this.appName = parts[0];
                this.scopeType = setScopeType(parts[1].toUpperCase());
                this.scopeKey = parts[2];
            }
        }

        public ScopeType getScopeType() {
            return scopeType;
        }

        public String getScopeKey() {
            return scopeKey;
        }

        public String getAppName() {
            return appName;
        }

        private ScopeType setScopeType(final String type) {
            if (StringUtil.isNullOrEmpty(type)){
                return null;
            }
            try {
                return ScopeType.valueOf(type);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
   }

    public static OAuthSession from(final OAuthSession oAuthSession, final String keyId, final String environment) {
        if(oAuthSession == null) {
            return new OAuthSession(keyId);
        }

        return new OAuthSession(oAuthSession.getAud(), keyId, oAuthSession.getEmail(), oAuthSession.getUuid(), oAuthSession.getScope(), environment);
    }

    public enum ScopeType {
        MNTNER,
        ENVIRONMENT
    }
}
