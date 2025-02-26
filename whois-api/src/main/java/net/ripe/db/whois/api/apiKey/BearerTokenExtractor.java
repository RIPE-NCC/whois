package net.ripe.db.whois.api.apiKey;

import com.google.common.net.HttpHeaders;
import com.nimbusds.oauth2.sdk.ParseException;
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
    private final URI tokenIntrospectEndpoint;
    private final ClientSecretBasic keycloakClient;

    @Autowired
    public BearerTokenExtractor(@Value("${apikey.authenticate.enabled:false}") final boolean enabled,
                                @Value("${openId.metadata.url:}")  final String openIdMetadataUrl,
                                @Value("${keycloak.idp.password:}")  final String keycloakPassword,
                                @Value("${keycloak.idp.client:whois}") final String whoisKeycloakId) {
        this.enabled = enabled;
        this.keycloakClient = new ClientSecretBasic(new ClientID(whoisKeycloakId), new Secret(keycloakPassword));
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
        final BearerAccessToken accessToken = getBearerToken(request);
        if(!enabled || accessToken == null ) {
            return null;
        }

        return getOAuthSession(accessToken, apiKeyId);
    }

    @Nullable
    public OAuthSession extractAndValidateAudience(final HttpServletRequest request, final String apiKeyId) {
      final OAuthSession oAuthSession = extractBearerToken(request, apiKeyId);
      if(oAuthSession == null) {
          return oAuthSession;
      }

      return ApiKeyUtils.validateAudience(oAuthSession, keycloakClient.getClientID().getValue()) ? oAuthSession :
                        new OAuthSession.Builder().keyId(apiKeyId).errorStatus("Failed to validate Audience").build();
    }

    private OAuthSession getOAuthSession(final BearerAccessToken accessToken, final String apiKeyId) {
        final OAuthSession.Builder oAuthSessionBuilder = new OAuthSession.Builder().keyId(apiKeyId);

        try {
            final TokenIntrospectionResponse response = TokenIntrospectionResponse.parse(new TokenIntrospectionRequest(
                                                                tokenIntrospectEndpoint,
                                                                keycloakClient,
                                                                accessToken).toHTTPRequest().send());

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

    private BearerAccessToken getBearerToken(final HttpServletRequest request) {
        try {
            return BearerAccessToken.parse(request.getHeader(HttpHeaders.AUTHORIZATION));
        } catch (ParseException e) {
            LOGGER.debug("Failed to parse BearerToken", e.getMessage());
            return null;
        }
    }
}
