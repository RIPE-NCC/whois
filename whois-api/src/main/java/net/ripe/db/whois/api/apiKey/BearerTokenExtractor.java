package net.ripe.db.whois.api.apiKey;

import com.google.common.net.HttpHeaders;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

@Component
public class BearerTokenExtractor   {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenExtractor.class);

    private final boolean enabled;
    private final String whoisKeycloakId;
    private final String jwksSetUrl;

    @Autowired
    public BearerTokenExtractor(@Value("${apikey.authenticate.enabled:false}") final boolean enabled,
                                @Value("${api.public.key.url:}")  final String jwksSetUrl,
                                @Value("${keycloak.idp.client:whois}") final String whoisKeycloakId) {
        this.enabled = enabled;
        this.whoisKeycloakId = whoisKeycloakId;
        this.jwksSetUrl = jwksSetUrl;
    }

    @Nullable
    public OAuthSession extractBearerToken(final HttpServletRequest request, final String apiKeyId) {
        if(!enabled || StringUtils.isEmpty(apiKeyId)) {
            return null;
        }

        final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        return getOAuthSession(bearerToken, apiKeyId);
    }

    @Nullable
    public OAuthSession extractAndValidateAudience(final HttpServletRequest request, final String apiKeyId) {
      final OAuthSession oAuthSession = extractBearerToken(request, apiKeyId);
      return ApiKeyUtils.validateAudience(oAuthSession, whoisKeycloakId) ? oAuthSession : new OAuthSession(apiKeyId);
    }

    private OAuthSession getOAuthSession(final String bearerToken, final String apiKeyId) {
        if(StringUtils.isEmpty(bearerToken)) {
            return new OAuthSession(apiKeyId);
        }

        try {
            final String accessToken = StringUtils.substringAfter(bearerToken, "Bearer ");
            final SignedJWT signedJWT = SignedJWT.parse(accessToken);

            final ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

            final JWKSource<SecurityContext> keySource = JWKSourceBuilder
                    .create(new URI(jwksSetUrl).toURL())
                    .retrying(true)
                    .build();

            final JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                    signedJWT.getHeader().getAlgorithm(),
                    keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

            jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                    new JWTClaimsSet.Builder().build(),
                    new HashSet<>(Arrays.asList(
                            JWTClaimNames.AUDIENCE,
                            JWTClaimNames.EXPIRATION_TIME,
                            "email",
                            "uuid"))));

            final JWTClaimsSet claimSet = jwtProcessor.process(accessToken, null);

            return new OAuthSession(claimSet.getAudience(),
                    apiKeyId,
                    claimSet.getStringClaim("email"),
                    claimSet.getStringClaim("uuid"),
                    claimSet.getStringClaim("scope"));

        } catch (Exception e) {
            LOGGER.info("Failed to read OAuthSession from BearerToken ", e);
            return new OAuthSession(apiKeyId);
        }
    }
}
