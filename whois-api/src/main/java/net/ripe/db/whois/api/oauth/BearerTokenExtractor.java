package net.ripe.db.whois.api.oauth;

import com.google.common.net.HttpHeaders;
import com.nimbusds.jose.proc.BadJWSException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

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
import net.ripe.db.whois.common.oauth.OAuthSession;
import net.ripe.db.whois.update.domain.Update;
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
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_ANY_MNTNR_SCOPE;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_AZP_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_EMAIL_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_JTI_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_SCOPE_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_UUID_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.getWhoisScope;

@Component
public class BearerTokenExtractor   {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenExtractor.class);

    private final boolean enabled;
    private final boolean isScopeMandatory;
    private final URI tokenIntrospectEndpoint;
    private final ClientSecretBasic keycloakClient;

    private final URI jwksSetUrl;

    @Autowired
    public BearerTokenExtractor(@Value("${apikey.authenticate.enabled:false}") final boolean enabled,
                                @Value("${apikey.scope.mandatory:false}") final boolean isScopeMandatory,
                                @Value("${openId.metadata.url:}")  final String openIdMetadataUrl,
                                @Value("${keycloak.idp.password:}")  final String keycloakPassword,
                                @Value("${keycloak.idp.client:whois}") final String whoisKeycloakId) {
        this.enabled = enabled;
        this.isScopeMandatory = isScopeMandatory;
        this.keycloakClient = new ClientSecretBasic(new ClientID(whoisKeycloakId), new Secret(keycloakPassword));

        final OIDCProviderMetadata oidcProviderMetadata = getOIDCMetadata(openIdMetadataUrl);
        this.tokenIntrospectEndpoint = oidcProviderMetadata != null ? oidcProviderMetadata.getIntrospectionEndpointURI() : null;
        this.jwksSetUrl =  oidcProviderMetadata != null ? oidcProviderMetadata.getJWKSetURI() : null;
    }

    @Nullable
    private static OIDCProviderMetadata getOIDCMetadata(final String openIdMetadataUrl) {
        try {
            return OIDCProviderMetadata.resolve(new Issuer(openIdMetadataUrl));
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

        final Update.EffectiveCredentialType authType = StringUtils.isEmpty(apiKeyId) ? Update.EffectiveCredentialType.OAUTH : Update.EffectiveCredentialType.APIKEY;

        final BearerAccessToken accessToken = getBearerToken(request);
        if (accessToken == null) {
            return new OAuthSession.Builder().keyId(apiKeyId).errorStatus("Invalid " + authType.name()).build();
        }

        //In case of oauth2 we need to call TokenInspection endpoint as token validity is 1 hour so user can be deleted after issuing of token
        //With APIkey we recieve token from the filter, so we can do verification of token manually
        return authType == Update.EffectiveCredentialType.OAUTH ? callTokenInspectionEndpoint(accessToken)
                                                                    : validateTokenOffline(accessToken, apiKeyId);
    }

    private OAuthSession validateTokenOffline(final BearerAccessToken accessToken, final String apiKeyId) {
        final OAuthSession.Builder oAuthSessionBuilder = new OAuthSession.Builder().keyId(apiKeyId);

        try {
            final SignedJWT signedJWT=  SignedJWT.parse(accessToken.getValue());

            final ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

            final JWKSource<SecurityContext> keySource = JWKSourceBuilder
                    .create(jwksSetUrl.toURL())
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
                            OAUTH_CUSTOM_EMAIL_PARAM,
                            OAUTH_CUSTOM_UUID_PARAM))));

            final JWTClaimsSet claimSet = jwtProcessor.process(accessToken.getValue(), null);

            if (!validateAudience(claimSet.getAudience())) {
                oAuthSessionBuilder.errorStatus(UpdateMessages.invalidOauthAudience("API Key").toString());
            }

            populateScope(claimSet.getStringClaim(OAUTH_CUSTOM_SCOPE_PARAM), oAuthSessionBuilder);

            return oAuthSessionBuilder.azp(claimSet.getStringClaim(OAUTH_CUSTOM_AZP_PARAM))
                    .email(claimSet.getStringClaim(OAUTH_CUSTOM_EMAIL_PARAM))
                    .aud(claimSet.getStringListClaim(JWTClaimNames.AUDIENCE))
                    .jti(claimSet.getStringClaim(OAUTH_CUSTOM_JTI_PARAM))
                    .uuid(claimSet.getStringClaim(OAUTH_CUSTOM_UUID_PARAM)).build();

        } catch (BadJWSException e) {
            tryToBuildOAuthSession(accessToken,oAuthSessionBuilder, String.format("Token validation failed, %s", e.getMessage()));
            return oAuthSessionBuilder.build();
        } catch (ParseException e) {
            tryToBuildOAuthSession(accessToken,oAuthSessionBuilder, "Failed to parse Bearer token from Api Key");
            return oAuthSessionBuilder.build();
        } catch (Exception e) {
            LOGGER.info("Invalid ApiKey ", e);
            tryToBuildOAuthSession(accessToken,oAuthSessionBuilder, "Invalid ApiKey");
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

    private boolean validateAudience(final List<String> audiences) {
        return audiences.stream().anyMatch(appName -> appName.equalsIgnoreCase(keycloakClient.getClientID().getValue()));
    }

    private void populateScope(final String scopes,  final OAuthSession.Builder oAuthSessionBuilder) {

        final Optional<String> whoisScope = getWhoisScope(scopes);
        if(whoisScope.isPresent() ) {
            oAuthSessionBuilder.scope(whoisScope.get());
            return;
        }

        if(isScopeMandatory) {
            oAuthSessionBuilder.errorStatus("Whois scope can not be empty");
            return;
        }

       oAuthSessionBuilder.scope(OAUTH_ANY_MNTNR_SCOPE);
    }

    private OAuthSession callTokenInspectionEndpoint(final BearerAccessToken accessToken) {
        final OAuthSession.Builder oAuthSessionBuilder = new OAuthSession.Builder();

        try {
            final TokenIntrospectionResponse response = TokenIntrospectionResponse.parse(new TokenIntrospectionRequest(
                    tokenIntrospectEndpoint,
                    keycloakClient,
                    accessToken).toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                tryToBuildOAuthSession(accessToken, oAuthSessionBuilder, "Failed to validate oAuthSession");
                return oAuthSessionBuilder.build();
            }

            final TokenIntrospectionSuccessResponse tokenDetails = response.toSuccessResponse();

            if (!tokenDetails.isActive()) {
                tryToBuildOAuthSession(accessToken, oAuthSessionBuilder, "Session associated with Access Token is not active");
                return oAuthSessionBuilder.build();
            }

            if (!validateAudience(Audience.toStringList(tokenDetails.getAudience()))) {
                oAuthSessionBuilder.errorStatus(UpdateMessages.invalidOauthAudience("Access Token").toString());
            }

            populateScope(tokenDetails.getStringParameter(OAUTH_CUSTOM_SCOPE_PARAM), oAuthSessionBuilder);

            return oAuthSessionBuilder.azp(tokenDetails.getStringParameter(OAUTH_CUSTOM_AZP_PARAM))
                    .email(tokenDetails.getStringParameter(OAUTH_CUSTOM_EMAIL_PARAM))
                    .aud(Audience.toStringList(tokenDetails.getAudience()))
                    .jti(tokenDetails.getStringParameter(OAUTH_CUSTOM_JTI_PARAM))
                    .uuid(tokenDetails.getStringParameter(OAUTH_CUSTOM_UUID_PARAM)).build();

        } catch (Exception e) {
            LOGGER.error("Failed to extract OAuth session", e);
            tryToBuildOAuthSession(accessToken, oAuthSessionBuilder, "Error validating OauthSession");
            return oAuthSessionBuilder.build();
        }
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
