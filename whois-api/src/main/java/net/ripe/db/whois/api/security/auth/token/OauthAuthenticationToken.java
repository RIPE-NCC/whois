package net.ripe.db.whois.api.security.auth.token;

import net.ripe.db.whois.common.oauth.DefaultOauthSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class OauthAuthenticationToken extends AbstractAuthenticationToken {

    private final String accessToken;
    private final DefaultOauthSession defaultOauthSession;

    public OauthAuthenticationToken(final String accessToken) {
        super(Collections.emptyList());

        this.accessToken = accessToken;
        this.defaultOauthSession = null;
        setAuthenticated(false);
    }


    public OauthAuthenticationToken(final DefaultOauthSession defaultOauthSession, final String accessToken) {
        super(Collections.emptyList());
        this.accessToken = accessToken;
        this.defaultOauthSession = defaultOauthSession;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return defaultOauthSession;
    }
}
