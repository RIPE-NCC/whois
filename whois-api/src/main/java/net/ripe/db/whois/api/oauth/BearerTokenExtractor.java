package net.ripe.db.whois.api.oauth;

import com.google.common.net.HttpHeaders;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.apache.commons.lang3.StringUtils;
import net.ripe.db.whois.common.aspects.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

import static net.ripe.db.whois.common.apiKey.ApiKeyUtils.OAUTH_CUSTOM_AZP_PARAM;
import static net.ripe.db.whois.common.apiKey.ApiKeyUtils.OAUTH_CUSTOM_EMAIL_PARAM;
import static net.ripe.db.whois.common.apiKey.ApiKeyUtils.OAUTH_CUSTOM_JTI_PARAM;
import static net.ripe.db.whois.common.apiKey.ApiKeyUtils.OAUTH_CUSTOM_SCOPE_PARAM;
import static net.ripe.db.whois.common.apiKey.ApiKeyUtils.OAUTH_CUSTOM_UUID_PARAM;

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

    @Stopwatch(thresholdMs = 100)
    @Nullable
    public OAuthSession extractBearerToken(final HttpServletRequest request, final String apiKeyId) {
        if(!enabled) return null;

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.isEmpty(authHeader) || !authHeader.startsWith(AccessTokenType.BEARER.toString())) {
            return null;
        }

        return getOAuthSession(request, apiKeyId);
    }

    private OAuthSession getOAuthSession(final HttpServletRequest request, final String apiKeyId) {
        final OAuthSession.Builder oAuthSessionBuilder = new OAuthSession.Builder().keyId(apiKeyId);
        final String authType = StringUtils.isEmpty(apiKeyId) ? "Access Token" : "API Key";

        final BearerAccessToken accessToken = getBearerToken(request);
        if (accessToken == null) {
            return oAuthSessionBuilder.errorStatus("Invalid " + authType).build();
        }

        try {
            final TokenIntrospectionResponse response = TokenIntrospectionResponse.parse(new TokenIntrospectionRequest(
                                                                tokenIntrospectEndpoint,
                                                                keycloakClient,
                                                                accessToken).toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                tryToBuildOAuthSession(accessToken, oAuthSessionBuilder, "Failed to validate " + authType);
                return oAuthSessionBuilder.build();
            }

            final TokenIntrospectionSuccessResponse tokenDetails = response.toSuccessResponse();

            if (! tokenDetails.isActive()) {
                tryToBuildOAuthSession(accessToken, oAuthSessionBuilder, String.format("Session associated with %s is not active", authType));
                return oAuthSessionBuilder.build();
            }

            if(!validateAudience(tokenDetails.getAudience())) {
                oAuthSessionBuilder.errorStatus(UpdateMessages.invalidOauthAudience(authType).toString());
            }

            return oAuthSessionBuilder.azp(tokenDetails.getStringParameter(OAUTH_CUSTOM_AZP_PARAM))
                    .email(tokenDetails.getStringParameter(OAUTH_CUSTOM_EMAIL_PARAM))
                    .aud(Audience.toStringList(tokenDetails.getAudience()))
                    .jti(tokenDetails.getStringParameter(OAUTH_CUSTOM_JTI_PARAM))
                    .uuid(tokenDetails.getStringParameter(OAUTH_CUSTOM_UUID_PARAM))
                    .scope(tokenDetails.getStringParameter(OAUTH_CUSTOM_SCOPE_PARAM)).build();

        } catch (Exception e) {
            LOGGER.error("Failed to extract OAuth session", e);
            tryToBuildOAuthSession(accessToken, oAuthSessionBuilder, "Error validating " + authType);
            return oAuthSessionBuilder.build();
        }
    }

    @Nullable
    private BearerAccessToken getBearerToken(final HttpServletRequest request) {
        try {
              final BearerAccessToken accessToken = BearerAccessToken.parse(request.getHeader(HttpHeaders.AUTHORIZATION));
              SignedJWT.parse(accessToken.getValue());
              return accessToken;
        } catch (Exception e) {
            LOGGER.debug("Failed to parse BearerToken {}", e.getMessage());
            return null;
        }
    }

    private boolean validateAudience(final List<Audience> audiences) {
        return Audience.toStringList(audiences).stream().anyMatch(appName -> appName.equalsIgnoreCase(keycloakClient.getClientID().getValue()));
    }

    //Try to build an oAuth session from invalid token to help us in Audit
    public void tryToBuildOAuthSession(final BearerAccessToken accessToken, final OAuthSession.Builder oAuthSessionBuilder, final String errorMessage) {

        oAuthSessionBuilder.errorStatus(errorMessage);

        try {

            final JWTClaimsSet claimSets = SignedJWT.parse(accessToken.getValue()).getJWTClaimsSet();
            oAuthSessionBuilder.azp(claimSets.getStringClaim(OAUTH_CUSTOM_AZP_PARAM))
                    .email(claimSets.getStringClaim(OAUTH_CUSTOM_EMAIL_PARAM))
                    .aud(claimSets.getAudience())
                    .jti(claimSets.getJWTID())
                    .uuid(claimSets.getStringClaim(OAUTH_CUSTOM_UUID_PARAM))
                    .scope(claimSets.getStringClaim(OAUTH_CUSTOM_SCOPE_PARAM)).build();

        } catch (Exception e) {
            //Ignore exceptions
        }
    }
}
