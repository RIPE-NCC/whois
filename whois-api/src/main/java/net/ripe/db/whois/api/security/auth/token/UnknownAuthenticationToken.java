package net.ripe.db.whois.api.security.auth.token;

import net.ripe.db.whois.common.oauth.UnknownSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class UnknownAuthenticationToken extends AbstractAuthenticationToken {

    private final UnknownSession unkwnownSession;

    public UnknownAuthenticationToken(final UnknownSession unkwnownSession) {
        super(Collections.emptyList());
        this.unkwnownSession = unkwnownSession;

        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return unkwnownSession;
    }
}
