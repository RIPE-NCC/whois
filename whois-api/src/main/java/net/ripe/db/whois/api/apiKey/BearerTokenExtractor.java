package net.ripe.db.whois.api.apiKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
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
import java.util.List;

@Component
public class BearerTokenExtractor   {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenExtractor.class);

    private final ApiPublicKeyLoader apiPublicKeyLoader;
    private final boolean enabled;
    private final String whoisKeycloakId;

    @Autowired
    public BearerTokenExtractor(final ApiPublicKeyLoader apiPublicKeyLoader,
                                @Value("${apikey.authenticate.enabled:false}") final boolean enabled,
                                @Value("${keycloak.idp.client:whois}") final String whoisKeycloakId) {
        this.apiPublicKeyLoader = apiPublicKeyLoader;
        this.enabled = enabled;
        this.whoisKeycloakId = whoisKeycloakId;
    }

    @Nullable
    public OAuthSession extractBearerToken(final HttpServletRequest request, final String apiKeyId) {
        if(!enabled || StringUtils.isEmpty(apiKeyId)) {
            return null;
        }

        final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        return getOAuthSession(bearerToken, apiKeyId);
    }

    private OAuthSession getOAuthSession(final String bearerToken, final String apiKeyId) {
        if(StringUtils.isEmpty(bearerToken)) {
            return new OAuthSession(apiKeyId);
        }

        try {
            final SignedJWT signedJWT = SignedJWT.parse(StringUtils.substringAfter(bearerToken, "Bearer "));

            if(!verifyJWTSignature(signedJWT)) {
              LOGGER.debug("JWT signature verification failed for {}", apiKeyId);
              return new OAuthSession(apiKeyId);
            }

            //TODO[MA]: remove when apiKeyId is available from api registry call
            final OAuthSession oAuthSession = OAuthSession.from(new ObjectMapper().readValue(signedJWT.getPayload().toString(), OAuthSession.class), apiKeyId);

            return ApiKeyUtils.validateAudience(oAuthSession, whoisKeycloakId) ? oAuthSession : new OAuthSession(apiKeyId);

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize OAuthSession, this should never have happened", e);
            return  new OAuthSession(apiKeyId);
        } catch (Exception e) {
            LOGGER.error("Failed to read OAuthSession, this should never have happened", e);
            return new OAuthSession(apiKeyId);
        }
    }

    private boolean verifyJWTSignature(final SignedJWT signedJWT) {
        try {
            final List<RSAKey> rsaKeys = apiPublicKeyLoader.loadPublicKey();

            if(rsaKeys.isEmpty()) {
              LOGGER.debug("RSA public key is not available");
              return true;
            }

            final RSAKey publicKey = rsaKeys.stream().filter( rsaKey -> rsaKey.getKeyID().equals(signedJWT.getHeader().getKeyID())).findFirst().get();
            final JWSVerifier verifier = new RSASSAVerifier(publicKey);

            return signedJWT.verify(verifier);
        } catch (Exception ex) {
            LOGGER.debug("failed to verify signature {}", ex.getMessage());
            return false;
        }
    }
}
