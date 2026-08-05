package net.ripe.db.whois.api;

import com.google.common.collect.Maps;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.security.OidcConfigurationProvider;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.oauth.OAuthUtils;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.api.AbstractIntegrationTest.getRequestBody;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_EXPIRED;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INACTIVE_TOKEN;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_ISS_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_INVALID_SIGNATURE_API_KEY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_ISSUES_AT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_ANY_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_MULTIPLE_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_MULTIPLE_MNT_WITH_ANY;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_NO_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_NULL_SCOPE;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_TEST_NO_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.BASIC_AUTH_TEST_TEST_MNT;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.TEST_AUD;
import static net.ripe.db.whois.api.ApiKeysAuthServerDummy.commonJwtValues;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_EMAIL_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_UUID_PARAM;

@Profile({WhoisProfile.TEST})
@Component
public class OAuthTokenIntrospectDummy implements Stub {

    private Server server;
    private int port = 0;
    private final OidcConfigurationProvider oidcConfigurationProvider;

    private final String whoisKeycloakId;
    private RSAKey jwk;

    public static final List<String> ACCOUNT_AUD = List.of("account");

    public static final Map<String, JWTClaimsSet> OIDC_TO_CLAIMSET =  Maps.newHashMap();

    {
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_TEST_NO_MNT, getJWT(TEST_AUD,  "test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_INACTIVE_TOKEN, getJWT(TEST_AUD,  "inactive@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", Date.from(Instant.now().minus(2, ChronoUnit.DAYS)), Date.from(Instant.now().minus(1, ChronoUnit.DAYS))));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_ANY_MNT, getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_OWNER_MNT,  getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_MULTIPLE_MNT_WITH_ANY,  getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_MULTIPLE_MNT,  getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT,  getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_TEST_TEST_MNT,  getJWT(TEST_AUD, "test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_INVALID_SIGNATURE_API_KEY,  getJWT(TEST_AUD, "invalid@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5"));

        OIDC_TO_CLAIMSET.put("valid-token", getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "audience/whois profile email"));
        OIDC_TO_CLAIMSET.put("db_e2e_1", getJWT(TEST_AUD, "db_e2e_1@ripe.net", "aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef", "audience/whois profile email"));
        OIDC_TO_CLAIMSET.put("invalid-token", null);
        OIDC_TO_CLAIMSET.put("invalid", null);

        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_NO_MNT, getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email"));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_NULL_SCOPE, getJWT(TEST_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", null));

        OIDC_TO_CLAIMSET.put(BASIC_AUTH_EXPIRED, getJWT(TEST_AUD, "expired@net.net", "906635c2-0405-429a-800b-0602bd716124", Date.from(Instant.now().minus(2, ChronoUnit.DAYS)), Date.from(Instant.now().minus(1, ChronoUnit.DAYS))));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_ISSUES_AT, getJWT(TEST_AUD, "issues_at@net.net", "906635c2-0405-429a-800b-0602bd716124", Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), Date.from(Instant.now().plus(2, ChronoUnit.DAYS))));
        OIDC_TO_CLAIMSET.put(BASIC_AUTH_INVALID_ISS_API_KEY, getJWT(TEST_AUD, "invalid_Iss@net.net", "906635c2-0405-429a-800b-0602bd716124"));

        OIDC_TO_CLAIMSET.put(BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE, getJWT(ACCOUNT_AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email whois.mntner:TEST-MNT"));

    }


    public OAuthTokenIntrospectDummy(final OidcConfigurationProvider oidcConfigurationProvider,
                                     @Value("${keycloak.idp.client:}") final String whoisKeycloakId) {
        this.whoisKeycloakId = whoisKeycloakId;
        this.oidcConfigurationProvider = oidcConfigurationProvider;
    }

    public static String convertToOidcJwt(final String userKey, final RSAKey keyPair, final int port, final String clientId) {

        final JWTClaimsSet jwt = OIDC_TO_CLAIMSET.get(userKey);
        if (jwt == null) {
            return userKey;
        }
        try{
            final JWTClaimsSet oidcJwt = new JWTClaimsSet.Builder(jwt)
                    .claim(OAuthUtils.OAUTH_CUSTOM_AZP_PARAM, clientId)
                    .build();
            return commonJwtValues(userKey, keyPair, port, oidcJwt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class ApiPublicKeyLoaderTestHandler extends Handler.Abstract {

        private final RSAKey jwk;
        private final String whoisKeycloakId;

        private static String ISSUER = "realms/ripe-ncc";

        ApiPublicKeyLoaderTestHandler(final RSAKey jwk, final String whoisKeycloakId) {
            this.jwk = jwk;
            this.whoisKeycloakId = whoisKeycloakId;
        }

        @Override
        public boolean handle(Request request, Response response, Callback callback) {

           response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/xml;charset=utf-8");

            if (request.getHttpURI().getPath().contains("ripe-ncc/protocol/openid-connect/token/introspect")) {
                try {

                    final String body = getRequestBody(request);
                    final SignedJWT signedJWT = SignedJWT.parse(StringUtils.substringAfter(body, "token="));

                    final String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
                    if (email.equals("invalid@ripenet")) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        return false;
                    }

                    final Instant issuedAt = signedJWT.getJWTClaimsSet().getIssueTime().toInstant();
                    final Instant expiredAt = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
                    final boolean hasCorrectIssuer = signedJWT.getJWTClaimsSet().getIssuer().contains(ISSUER);
                    final boolean hasCorrectAudience = signedJWT.getJWTClaimsSet().getAudience().stream().anyMatch(audience -> audience.equals(this.whoisKeycloakId));
                    final boolean hasCorrectAlg = signedJWT.getHeader().getAlgorithm().equals(JWSAlgorithm.RS256);
                    final boolean isActive = hasCorrectAlg && hasCorrectIssuer && hasCorrectAudience &&
                            !email.equals("inactive@ripe.net") && issuedAt.isBefore(Instant.now()) && expiredAt.isAfter(Instant.now());

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                    response.write(
                            true,
                            ByteBuffer.wrap(JSONObjectUtils.parse(signedJWT.getPayload().toString()).appendField("active", isActive).toString().getBytes()),
                            callback
                    );

                    callback.succeeded();

                    return true;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

            if (request.getHttpURI().getPath().contains("realms/ripe-ncc/protocol/openid-connect/certs")) {

                final String jwks = new JWKSet(jwk).toString();

                response.setStatus(HttpServletResponse.SC_OK);
                response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                response.write(
                        true,
                        ByteBuffer.wrap(jwks.getBytes(StandardCharsets.UTF_8)),
                        callback
                );
                callback.succeeded();
                return true;
            }

            if (request.getHttpURI().getPath().contains("ripe-ncc/.well-known/openid-configuration")) {
                final int port = request.getHttpURI().getPort();
                String body = """
                {
                  "issuer": "http://localhost:%d/%s",
                  "jwks_uri": "http://localhost:%d/realms/ripe-ncc/protocol/openid-connect/certs",
                  "introspection_endpoint": "http://localhost:%d/realms/ripe-ncc/protocol/openid-connect/token/introspect",
                  "subject_types_supported": [
                    "public"
                  ],
                  "id_token_signing_alg_values_supported": [
                    "RS256"
                  ]
                }
                """.formatted(port, ISSUER, port, port);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getHeaders().put("Content-Type", "application/json");
                response.write(
                        true,
                        ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)),
                        callback
                );

                response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                return true;
            }

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }
    }

    static JWTClaimsSet getJWT(final Object aud, final String email, final String uuid, final String scopes) {
        return getJWT(aud, email, uuid, scopes,
                Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS))
        );

    }

    static JWTClaimsSet getJWT(final Object aud, final String email, final String uuid, final String scopes,
                                       final Date issuedTime, final Date expirationTime) {
        return new JWTClaimsSet.Builder()
                .claim(JWTClaimNames.AUDIENCE, aud)
                .claim(JWTClaimNames.ISSUED_AT, issuedTime)
                .claim(OAUTH_CUSTOM_EMAIL_PARAM, email)
                .claim( OAUTH_CUSTOM_UUID_PARAM, uuid)
                .claim( JWTClaimNames.EXPIRATION_TIME, expirationTime)
                .claim("scope", scopes).build();

    }

    private static JWTClaimsSet getJWT(final Object aud, final String email, final String uuid) {
        return getJWT(aud, email, uuid,
                Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS))
        );

    }

    private static JWTClaimsSet getJWT(final Object aud, final String email, final String uuid, final Date issuedTime, final Date expirationTime) {
        return new JWTClaimsSet.Builder()
                .claim(JWTClaimNames.AUDIENCE, aud)
                .claim(JWTClaimNames.ISSUED_AT, issuedTime)
                .claim(OAUTH_CUSTOM_EMAIL_PARAM, email)
                .claim( OAUTH_CUSTOM_UUID_PARAM, uuid)
                .claim( JWTClaimNames.EXPIRATION_TIME, expirationTime)
                .claim("scope", "openid audience/whois profile email").build();

    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() throws Exception {
        this.initKeys();

        server = new Server(0);
        server.setHandler(new ApiPublicKeyLoaderTestHandler(jwk, whoisKeycloakId));
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final URI openIdMetadataUrl = new URIBuilder()
                .setScheme("http")
                .setHost("localhost")
                .setPort(port)
                .setPath("realms/ripe-ncc")
                .build();

        ReflectionTestUtils.setField(this.oidcConfigurationProvider, "openIdMetadataUrl", openIdMetadataUrl.toString());
    }

    public void initKeys() throws Exception {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();

        this.jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID("dummy-key-id")
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    @PreDestroy
    public void stop() throws Exception {
        server.stop();
    }

    public int getPort() {
        return port;
    }


    @Override
    public void reset() {
    }

    public RSAKey getJwk() {
        return jwk;
    }
}
