package net.ripe.db.whois.api.security.auth.provider;

import net.ripe.db.whois.api.security.auth.AccessTokenValidationException;
import net.ripe.db.whois.api.security.auth.token.OidcAuthenticationToken;
import net.ripe.db.whois.api.security.auth.validate.OidcTokenValidator;
import net.ripe.db.whois.common.oauth.OidcSession;
import net.ripe.db.whois.update.domain.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static net.ripe.db.whois.api.security.auth.provider.DefaultOauthAuthProvider.buildSession;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_ANY_MNTNR_SCOPE;


public class OidcAuthProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthProvider.class);

    final OidcTokenValidator oidcTokenValidator;

    public OidcAuthProvider(final OidcTokenValidator oidcTokenValidator) {
        this.oidcTokenValidator = oidcTokenValidator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        final OidcSession.Builder oidcSessionBuilder = new OidcSession.Builder();
        final String accessToken = authentication.getCredentials().toString();
        try {
            final Jwt jwt = oidcTokenValidator.validate(accessToken, Update.EffectiveCredentialType.OIDC);

            buildSession(oidcSessionBuilder, jwt);

            oidcSessionBuilder.scopes(List.of(OAUTH_ANY_MNTNR_SCOPE));
            final OidcSession oidcSession = oidcSessionBuilder.build();

            return getAuthenticationToken(oidcSession, accessToken);
        } catch (AccessTokenValidationException e) {
            DefaultOauthAuthProvider.tryToBuildOAuthSession(accessToken, oidcSessionBuilder, e.getMessage());
            return new OidcAuthenticationToken(oidcSessionBuilder.build(),accessToken);
        } catch (Exception e) {
            LOGGER.error("OIDC Authentication failed during validation, due to {}: {}", e.getClass().getName(), e.getMessage());
            DefaultOauthAuthProvider.tryToBuildOAuthSession(accessToken, oidcSessionBuilder, "Invalid OIDC Token");
            return new OidcAuthenticationToken(oidcSessionBuilder.build(),accessToken);
        }
    }

    OidcAuthenticationToken getAuthenticationToken(final OidcSession oAuthSession, final String accessToken) {
        return new OidcAuthenticationToken(oAuthSession, accessToken);
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return OidcAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
