package net.ripe.db.whois.api.security.auth.token;

import net.ripe.db.whois.common.oauth.APIKeySession;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final APIKeySession apiKeySession;

    public ApiKeyAuthenticationToken(final APIKeySession apiKeySession) {
        super(Collections.emptyList());
        this.apiKeySession = apiKeySession;

        setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return apiKeySession;
    }
}
