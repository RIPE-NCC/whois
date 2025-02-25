package net.ripe.db.whois.api.apiKey;

import com.google.common.net.HttpHeaders;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
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

@Component
public class BearerTokenExtractor   {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenExtractor.class);

    private final boolean enabled;
    private final String whoisKeycloakId;
    private final URI tokenIntrospectEndpoint;
    private final String keycloakPassword;

    @Autowired
    public BearerTokenExtractor(@Value("${apikey.authenticate.enabled:false}") final boolean enabled,
                                @Value("${openId.metadata.url:}")  final String openIdMetadataUrl,
                                @Value("${keycloak.idp.password:}")  final String keycloakPassword,
                                @Value("${keycloak.idp.client:whois}") final String whoisKeycloakId) {
        this.enabled = enabled;
        this.whoisKeycloakId = whoisKeycloakId;
        this.keycloakPassword = keycloakPassword;
        this.tokenIntrospectEndpoint = getTokenIntrospectEndpoint(openIdMetadataUrl);
    }

    private static URI getTokenIntrospectEndpoint(final String openIdMetadataUrl) {
        try {
            return OIDCProviderMetadata.resolve(new Issuer(openIdMetadataUrl)).getIntrospectionEndpointURI();
        } catch (Exception e) {
            LOGGER.error("Failed to read OIDC metadata", e);
            return null;
        }
    }

    @Nullable
    public OAuthSession extractBearerToken(final HttpServletRequest request, final String apiKeyId) {
        final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(!enabled || StringUtils.isEmpty(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return null;
        }

        return getOAuthSession(bearerToken, apiKeyId);
    }

    @Nullable
    public OAuthSession extractAndValidateAudience(final HttpServletRequest request, final String apiKeyId) {
      final OAuthSession oAuthSession = extractBearerToken(request, apiKeyId);
      if(oAuthSession == null || ApiKeyUtils.validateAudience(oAuthSession, whoisKeycloakId)) {
          return oAuthSession;
      }

      return new OAuthSession.Builder().keyId(apiKeyId).errorStatus("Failed to validate Audience").build();
    }

    private OAuthSession getOAuthSession(final String bearerToken, final String apiKeyId) {
        final OAuthSession.Builder oAuthSessionBuilder = new OAuthSession.Builder().keyId(apiKeyId);

        try {
            final TokenIntrospectionResponse response = TokenIntrospectionResponse.parse(new TokenIntrospectionRequest(
                    tokenIntrospectEndpoint,
                    new ClientSecretBasic(new ClientID(whoisKeycloakId), new Secret(keycloakPassword)),
                    BearerAccessToken.parse(bearerToken))
                    .toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                oAuthSessionBuilder.errorStatus("Error: " + response.toErrorResponse().getErrorObject());
                return oAuthSessionBuilder.build();
            }

            final TokenIntrospectionSuccessResponse tokenDetails = response.toSuccessResponse();
            if (! tokenDetails.isActive()) {
                oAuthSessionBuilder.errorStatus("Invalid / expired access token");
                return oAuthSessionBuilder.build();
            }

            return oAuthSessionBuilder.azp(tokenDetails.getStringParameter("azp"))
                    .email(tokenDetails.getStringParameter("email"))
                    .aud(tokenDetails.getStringListParameter("aud"))
                    .uuid(tokenDetails.getStringParameter("uuid"))
                    .scope(tokenDetails.getStringParameter("scope")).build();

        } catch (Exception e) {
            LOGGER.warn("Failed to extract OAuth session", e);
            oAuthSessionBuilder.errorStatus("Failed to read OAuthSession from BearerToken" + e.getMessage());
            return oAuthSessionBuilder.build();
        }
    }
}
