package net.ripe.db.whois.api.security.auth.validate;

import net.ripe.db.whois.api.security.auth.AccessTokenValidationException;
import net.ripe.db.whois.update.domain.Update;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_ANY_MNTNR_SCOPE;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_SCOPE_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.getWhoisMntnerScopes;

public class DefaultTokenValidator extends AccessTokenValidator {

    private final int maxScopes;

    public DefaultTokenValidator(final int maxScopes,
                                 final boolean shouldUseTokenInspector,
                                 final JwtDecoder jwtDecoder,
                                 final OpaqueTokenIntrospector keycloakIntrospector) {
        super(shouldUseTokenInspector, jwtDecoder, keycloakIntrospector);
        this.maxScopes = maxScopes;
    }

    @Override
    public Jwt validate(final String accessToken, final Update.EffectiveCredentialType effectiveCredentialType) {
        final Jwt jwt = super.validate(accessToken, effectiveCredentialType);
        validateScopes(maxScopes, jwt.getClaimAsString(OAUTH_CUSTOM_SCOPE_PARAM));
        return jwt;
    }

    public static void validateScopes(final int maxScopes, final String scopes) {
        final List<String> whoisScopes = (StringUtils.isEmpty(scopes)) ? Collections.emptyList() : getWhoisMntnerScopes(scopes);

        if(whoisScopes.size() > maxScopes) {
            throw new AccessTokenValidationException("Whois scopes can not be more than " + maxScopes);
        }

        if(whoisScopes.size() > 1 && whoisScopes.contains(OAUTH_ANY_MNTNR_SCOPE)) {
            throw new AccessTokenValidationException("Whois scopes can either have ANY or specific maintainers");
        }

        if(whoisScopes.isEmpty()) {
            throw new AccessTokenValidationException("Wrong whois scope.");
        }
    }
}
