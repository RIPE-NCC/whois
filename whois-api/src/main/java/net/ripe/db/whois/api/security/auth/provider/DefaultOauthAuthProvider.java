package net.ripe.db.whois.api.security.auth.provider;


import net.ripe.db.whois.api.security.auth.AccessTokenValidationException;
import net.ripe.db.whois.api.security.auth.token.OauthAuthenticationToken;
import net.ripe.db.whois.api.security.auth.validate.DefaultTokenValidator;
import net.ripe.db.whois.common.oauth.AbstractOAuthSession;
import net.ripe.db.whois.common.oauth.DefaultOauthSession;
import net.ripe.db.whois.update.domain.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_AZP_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_EMAIL_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_JTI_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_SCOPE_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_UUID_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.getWhoisMntnerScopes;

public class DefaultOauthAuthProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOauthAuthProvider.class);
    private final DefaultTokenValidator defaultTokenValidator;

    public DefaultOauthAuthProvider(final DefaultTokenValidator defaultTokenValidator) {
        this.defaultTokenValidator = defaultTokenValidator;
    }


    @Override
    public Authentication authenticate(Authentication authentication) {
        final DefaultOauthSession.Builder oauthSessionBuilder = new DefaultOauthSession.Builder();
        final String accessToken = authentication.getCredentials().toString();
        try {
            final Jwt jwt = defaultTokenValidator.validate(accessToken, Update.EffectiveCredentialType.OAUTH);

            buildSession(oauthSessionBuilder, jwt);
            final DefaultOauthSession defaultOauthSession = oauthSessionBuilder.build();

            return new OauthAuthenticationToken(defaultOauthSession, accessToken);
        } catch (AccessTokenValidationException e) {
            tryToBuildOAuthSession(accessToken, oauthSessionBuilder, e.getMessage());
            return new OauthAuthenticationToken(oauthSessionBuilder.build(),accessToken);
        } catch (Exception e) {
            LOGGER.error("Oauth Authentication failed during validation, due to {}: {}", e.getClass().getName(), e.getMessage());
            tryToBuildOAuthSession(accessToken, oauthSessionBuilder, "Invalid OIDC Token");
            return new OauthAuthenticationToken(oauthSessionBuilder.build(),accessToken);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OauthAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public static void buildSession(final AbstractOAuthSession.Builder builder, final Jwt jwt) {
        builder.azp(jwt.getClaimAsString(OAUTH_CUSTOM_AZP_PARAM))
                .email(jwt.getClaimAsString(OAUTH_CUSTOM_EMAIL_PARAM))
                .aud(jwt.getAudience())
                .scopes(getWhoisMntnerScopes(jwt.getClaimAsString(OAUTH_CUSTOM_SCOPE_PARAM)))
                .jti(jwt.getClaimAsString(OAUTH_CUSTOM_JTI_PARAM))
                .uuid(jwt.getClaimAsString(OAUTH_CUSTOM_UUID_PARAM));
    }

    public static void tryToBuildOAuthSession(final String accessToken,
                                              final AbstractOAuthSession.Builder oAuthSessionBuilder,
                                              final String errorMessage) {

        oAuthSessionBuilder.errorStatus(errorMessage);

        try {
            final Jwt jwt = Jwt.withTokenValue(accessToken).build();
            buildSession(oAuthSessionBuilder, jwt);
        } catch (Exception e) {
            //Ignore exceptions
        }
    }
}
