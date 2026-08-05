package net.ripe.db.whois.api.security.auth.validate;

import net.ripe.db.whois.api.security.auth.AccessTokenValidationException;
import net.ripe.db.whois.update.domain.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_ACTIVE_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_SCOPE_PARAM;

public abstract class AccessTokenValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenValidator.class);

    private final boolean shouldUseTokenInspector;
    private final JwtDecoder jwtDecoder;
    private final OpaqueTokenIntrospector opaqueTokenIntrospector;

    AccessTokenValidator(final boolean shouldUseTokenInspector,
                         final JwtDecoder jwtDecoder,
                         final OpaqueTokenIntrospector opaqueTokenIntrospector) {
        this.shouldUseTokenInspector = shouldUseTokenInspector;
        this.jwtDecoder = jwtDecoder;
        this.opaqueTokenIntrospector = opaqueTokenIntrospector;
    }

    public Jwt validate(final String accessToken, final Update.EffectiveCredentialType effectiveCredentialType) {
        return shouldUseTokenInspector ?
                validateOnLine(accessToken, effectiveCredentialType) :
                validateOffLine(accessToken, effectiveCredentialType);
    }

    private Jwt validateOffLine(final String accessToken, final Update.EffectiveCredentialType effectiveCredentialType){
        try {
            return jwtDecoder.decode(accessToken);
        } catch (JwtValidationException e){
            if (e.getErrors().stream().anyMatch(error -> error.getDescription().contains("expired"))) {
                throw new AccessTokenValidationException("[" + effectiveCredentialType + "] Token has expired");
            }
            throw new AccessTokenValidationException("[" + effectiveCredentialType + "] " + e.getMessage());
        } catch (Exception e){
            LOGGER.error("Failed to offline validation {} due to {}", effectiveCredentialType, e.getMessage());
            throw new AccessTokenValidationException("Invalid " + effectiveCredentialType);
        }
    }

    private Jwt validateOnLine(final String accessToken, final Update.EffectiveCredentialType effectiveCredentialType) {
        try {
            final OAuth2AuthenticatedPrincipal principal = opaqueTokenIntrospector.introspect(accessToken);

            if (Boolean.FALSE.equals(principal.getAttribute(OAUTH_CUSTOM_ACTIVE_PARAM))) {
                throw new AccessTokenValidationException("Session associated with OIDC Token is not active");
            }

            final Map<String, Object> normalisedClaims = normalizeClaims(principal.getAttributes());
            return Jwt.withTokenValue(accessToken)
                    .header("alg", "none")
                    .claims(c -> c.putAll(normalisedClaims))
                    .build();
        } catch (BadOpaqueTokenException e) {
            LOGGER.info("Failed to validate the {} through token inspection {}", effectiveCredentialType, e.getMessage());
            throw new AccessTokenValidationException(String.format("Session associated with %s is not active", effectiveCredentialType));
        } catch (OAuth2IntrospectionException e) {
            LOGGER.error("Failed to validate the {} through token inspection {}", effectiveCredentialType, e.getMessage());
            throw new AccessTokenValidationException(String.format("Failed to validate %s token", effectiveCredentialType));
        } catch (Exception e) {
            LOGGER.error("Failed to validate the {} due to {}", effectiveCredentialType, e.getMessage());
            throw new AccessTokenValidationException("Invalid authentication");
        }
    }

    private static Map<String, Object> normalizeClaims(final Map<String, Object> original) {
        final Map<String, Object> claims = new HashMap<>(original);

        final Object scopeAttr = claims.get(OAUTH_CUSTOM_SCOPE_PARAM);
        if (scopeAttr instanceof Collection<?> scopeCollection) {
            String scopeAsString = scopeCollection.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" "));
            claims.put(OAUTH_CUSTOM_SCOPE_PARAM, scopeAsString);
        }

        return claims;
    }
}
