package net.ripe.db.whois.api.security.auth.token;

import net.ripe.db.whois.common.oauth.OidcSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class OidcAuthenticationToken extends AbstractAuthenticationToken {

    private final String accessToken;
    private final OidcSession oidcSession;

    public OidcAuthenticationToken(final String accessToken) {
        super(Collections.emptyList());

        this.accessToken = accessToken;
        this.oidcSession = null;
        setAuthenticated(false);
    }

    public OidcAuthenticationToken(final OidcSession oidcSession, final String accessToken) {
        super(Collections.emptyList());
        this.accessToken = accessToken;
        this.oidcSession = oidcSession;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return oidcSession;
    }
}
