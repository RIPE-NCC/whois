package net.ripe.db.whois.api.oauth;

import com.google.common.net.HttpHeaders;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.apache.commons.lang3.StringUtils;
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

    @Nullable
    public OAuthSession extractBearerToken(final HttpServletRequest request, final String apiKeyId) {
        if(!enabled) return null;

        final BearerAccessToken accessToken = getBearerToken(request);
        return accessToken != null ? getOAuthSession(accessToken, apiKeyId) : null;
    }

    private OAuthSession getOAuthSession(final BearerAccessToken accessToken, final String apiKeyId) {
        final OAuthSession.Builder oAuthSessionBuilder = new OAuthSession.Builder().keyId(apiKeyId);
        final String authType = StringUtils.isEmpty(apiKeyId) ? "Access Token" : "API Key";

        try {
            final TokenIntrospectionResponse response = TokenIntrospectionResponse.parse(new TokenIntrospectionRequest(
                                                                tokenIntrospectEndpoint,
                                                                keycloakClient,
                                                                accessToken).toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                tryToBuildOAuthSession(accessToken, oAuthSessionBuilder);
                oAuthSessionBuilder.errorStatus("Invalid " + authType);
                return oAuthSessionBuilder.build();
            }

            final TokenIntrospectionSuccessResponse tokenDetails = response.toSuccessResponse();

            if (! tokenDetails.isActive()) {
                tryToBuildOAuthSession(accessToken, oAuthSessionBuilder);
                oAuthSessionBuilder.errorStatus(String.format("Session associated with %s is not active", authType));
                return oAuthSessionBuilder.build();
            }

            if(!validateAudience(tokenDetails.getAudience())) {
                oAuthSessionBuilder.errorStatus(UpdateMessages.invalidApiKeyAudience().toString());
            }

            return oAuthSessionBuilder.azp(tokenDetails.getStringParameter(OAUTH_CUSTOM_AZP_PARAM))
                    .email(tokenDetails.getStringParameter(OAUTH_CUSTOM_EMAIL_PARAM))
                    .aud(Audience.toStringList(tokenDetails.getAudience()))
                    .jti(tokenDetails.getJWTID().getValue())
                    .uuid(tokenDetails.getStringParameter(OAUTH_CUSTOM_UUID_PARAM))
                    .scope(tokenDetails.getScope().toString()).build();

        } catch (Exception e) {
            LOGGER.error("Failed to extract OAuth session", e);
            tryToBuildOAuthSession(accessToken, oAuthSessionBuilder);
            return oAuthSessionBuilder.build();
        }
    }

    @Nullable
    private BearerAccessToken getBearerToken(final HttpServletRequest request) {
        try {
            return BearerAccessToken.parse(request.getHeader(HttpHeaders.AUTHORIZATION));
        } catch (ParseException e) {
            LOGGER.debug("Failed to parse BearerToken {}", e.getMessage());
            return null;
        }
    }

    private boolean validateAudience(final List<Audience> audiences) {
        return Audience.toStringList(audiences).stream().anyMatch(appName -> appName.equalsIgnoreCase(keycloakClient.getClientID().getValue()));
    }

    //Try to build an oAuth session from invalid token to help us in Audit
    public void tryToBuildOAuthSession(final BearerAccessToken accessToken, final OAuthSession.Builder oAuthSessionBuilder) {
        try {

            final JWTClaimsSet claimSets = SignedJWT.parse(accessToken.getValue()).getJWTClaimsSet();
            oAuthSessionBuilder.azp(claimSets.getStringClaim(OAUTH_CUSTOM_AZP_PARAM))
                    .email(claimSets.getStringClaim(OAUTH_CUSTOM_EMAIL_PARAM))
                    .aud(claimSets.getAudience())
                    .jti(claimSets.getJWTID())
                    .uuid(claimSets.getStringClaim(OAUTH_CUSTOM_UUID_PARAM))
                    .scope(claimSets.getStringClaim("scope"));

        } catch (Exception e) {
            //Ignore exceptions
        }
    }
}
