package net.ripe.db.whois.api.security.auth.provider;

import net.ripe.db.whois.api.security.auth.AccessTokenValidationException;
import net.ripe.db.whois.api.security.auth.token.ApiKeyAuthenticationToken;
import net.ripe.db.whois.api.security.auth.validate.DefaultTokenValidator;
import net.ripe.db.whois.common.oauth.APIKeySession;
import net.ripe.db.whois.common.oauth.ApiKeyAuthServiceClient;
import net.ripe.db.whois.update.domain.Update;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import static net.ripe.db.whois.api.security.auth.provider.DefaultOauthAuthProvider.buildSession;

public class ApiKeyAuthProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthProvider.class);

    private final ApiKeyAuthServiceClient apiKeyAuthServiceClient;

    private final DefaultTokenValidator defaultTokenValidator;
    public ApiKeyAuthProvider(final DefaultTokenValidator defaultTokenValidator,
                              final ApiKeyAuthServiceClient apiKeyAuthServiceClient) {
        this.defaultTokenValidator = defaultTokenValidator;
        this.apiKeyAuthServiceClient = apiKeyAuthServiceClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        final APIKeySession.Builder apiKeySessionBuilder = new APIKeySession.Builder();

        final String accessToken = getAccessTokenFromApiKey(authentication);

        try {
            apiKeySessionBuilder.keyId(authentication.getName());
            if (StringUtils.isEmpty(accessToken)) {
                throw new AccessTokenValidationException("Invalid APIKEY");
            }
            final Jwt jwt = defaultTokenValidator.validate(accessToken, Update.EffectiveCredentialType.APIKEY);

            buildSession(apiKeySessionBuilder, jwt);
            final APIKeySession apiKeySession = apiKeySessionBuilder.build();

            return new ApiKeyAuthenticationToken(apiKeySession);
        } catch (AccessTokenValidationException e) {
            DefaultOauthAuthProvider.tryToBuildOAuthSession(accessToken, apiKeySessionBuilder, e.getMessage());
            return new ApiKeyAuthenticationToken(apiKeySessionBuilder.build());
        } catch (Exception e) {
            LOGGER.error("APIKEY Authentication failed during validation, due to {}: {}", e.getClass().getName(), e.getMessage());
            DefaultOauthAuthProvider.tryToBuildOAuthSession(accessToken, apiKeySessionBuilder, "Invalid APIKEY Token");
            return new ApiKeyAuthenticationToken(apiKeySessionBuilder.build());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private @Nullable String getAccessTokenFromApiKey(Authentication authentication) {
        final String basicHeader = authentication.getDetails().toString();

        return apiKeyAuthServiceClient.validateApiKey(basicHeader, authentication.getName());
    }
}
