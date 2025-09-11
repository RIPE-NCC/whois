package net.ripe.db.whois.api;

import com.google.common.collect.Maps;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.oauth.ApiKeyAuthServiceClient;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_EMAIL_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_UUID_PARAM;

@Profile({WhoisProfile.TEST})
@Component
public class ApiKeyAuthServerDummy implements Stub {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthServerDummy.class);
    public static final List<String> AUD = Arrays.asList("account", "whois");
    public static final String BASIC_AUTH_TEST_NO_MNT = "eFR0cm9lZUpWYWlmSWNQR1BZUW5kSmhnOmp5akhYR2g4WDFXRWZyc2M5SVJZcUVYbw==";
    public static final String BASIC_AUTH_INACTIVE_TOKEN = "TlpYSTRTRVo0SzBEVTJUQ0Y0QkUxQklEOjJtQ2syNTJ6ekR0b0dva3RPaFJ2czVNWA==";
    public static final String BASIC_AUTH_PERSON_ANY_MNT = "bDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_PERSON_MULTIPLE_MNT = "TDU3UDZWSFZYV1pSWUlWWFBaV0FVWUxaOlk1bFZaR0Z3U25WdGZHTmdSQTR5WjYxaA==";
    public static final String BASIC_AUTH_PERSON_MULTIPLE_MNT_WITH_ANY = "TVNMRTJUTlJUTlg4UFNOQVE5SDJYR1M0OnBhc0RkOUdxeElNYXlZc1NPUVdMbkxvVQ==";
    public static final String BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT = "TVFLTzRXVU1QTllSNU9NTVk2MFNPV0paOnVnMWF4UGZVQ1JVc2Fua2hpVjA5WVhKdQ==";
    public static final String BASIC_AUTH_PERSON_OWNER_MNT = "cDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_TEST_TEST_MNT = "dDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE = "aFR0cm9lZUpWYWlmSWNQR1BZUW5kSmhnOmp5akhYR2g4WDFXRWZyc2M5SVJZcUVYbw==";
    public static final String BASIC_AUTH_INVALID_API_KEY = "aDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_INVALID_SIGNATURE_API_KEY = "TXp1ZzRxRVlpSTVET1dqOXI1Qkp1Y2k4OnZBdzgyRTFCMkZ2dFVyYjB0MDF0Ykt2cg==";

    public static final String BASIC_AUTH_PERSON_NO_MNT = "WVpJUUlVTThOUVo3SUpWSU1HSkZTQ09HOjB2bVNCc2taS0FSMlF5ekNFd0FBRGN5eg==";
    public static final String BASIC_AUTH_PERSON_NULL_SCOPE = "QlBSSTNCSFBPUkhGQUJCUjVHV1M3U1hHOmhlU29ZTYzTXM3elJsM2ppc1czOFJ1Ng==";


    public static final Map<String, JWTClaimsSet> APIKEY_TO_OAUTHSESSION =  Maps.newHashMap();

    {
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_TEST_NO_MNT, getJWT(AUD,  "test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "profile email whois.mntner:ANY:write"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_INACTIVE_TOKEN, getJWT(AUD,  "inactive@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "profile email whois.mntner:ANY:write"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_ANY_MNT, getJWT(AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "whois.mntner:ANY:write"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_OWNER_MNT,  getJWT(AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email whois.mntner:OWNER-MNT"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_MULTIPLE_MNT_WITH_ANY,  getJWT(AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email whois.mntner:OWNER-MNT whois.mntner:ANY:write"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_MULTIPLE_MNT,  getJWT(AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email whois.mntner:OWNER-MNT whois.mntner:TEST-MNT"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_MNT_EXCEED_LIMIT,  getJWT(AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email whois.mntner:OWNER-MNT whois.mntner:TEST-MNT whois.mntner:TEST2-MNT"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_TEST_TEST_MNT,  getJWT(AUD, "test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "whois.mntner:TEST-MNT profile email"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_INVALID_SIGNATURE_API_KEY,  getJWT(AUD, "invalid@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "profile email whois.mntner:TEST-MNT"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_OWNER_MNT_WRONG_AUDIENCE, getJWT(Arrays.asList("account", "whois-invalid"), "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email whois.mntner:TEST-MNT"));

        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_NO_MNT, getJWT(AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_NULL_SCOPE, getJWT(AUD, "person@net.net", "906635c2-0405-429a-800b-0602bd716124", null));
    }

    private Server server;
    private int port = 0;

    private final ApiKeyAuthServiceClient apiKeyAuthServiceClient;

    @Autowired
    public ApiKeyAuthServerDummy(ApiKeyAuthServiceClient apiKeyAuthServiceClient) {
        this.apiKeyAuthServiceClient = apiKeyAuthServiceClient;
    }


    private class OAuthTestHandler extends Handler.Abstract {

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/xml;charset=utf-8");

            if(!request.getHttpURI().getPath().contains("/api/v1/api-keys/authenticate")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }

            final String userKey = StringUtils.substringAfter(request.getHeaders().get("Authorization"), "Basic").trim();

            try {
                final String jwt = convertToJwt(userKey);

                response.setStatus(HttpServletResponse.SC_OK);
                response.write(true, ByteBuffer.wrap(jwt.getBytes(StandardCharsets.UTF_8)), callback);
                response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);

                callback.succeeded();
                return true;
            } catch (NotFoundException ex) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (NotAuthorizedException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (Exception exception) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            return false;
        }
    }

    public static String convertToJwt(final String userKey) {

        final JWTClaimsSet jwt = APIKEY_TO_OAUTHSESSION.get(userKey);
        if (jwt == null) {
            throw new NotFoundException("Api Key not found");
        }

        if(userKey.equals(BASIC_AUTH_INVALID_SIGNATURE_API_KEY)) {
            throw new NotAuthorizedException("Api Key not valid");
        }

        try {
            final RSAKey privateKey = RSAKey.parse(new String(Files.readAllBytes(ResourceUtils.getFile("classpath:JWT_private.key").toPath())));
            final JWSSigner signer = new RSASSASigner(privateKey);

            JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.getKeyID()).build(),
                    new Payload(jwt.toJSONObject()));

            jwsObject.sign(signer);

            return jwsObject.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() {
        server = new Server(0);
        server.setHandler(new OAuthTestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final String restUrl = String.format("http://localhost:%s/api/v1/api-keys/authenticate", getPort());
        LOGGER.info("OAuth dummy server restUrl: {}", restUrl);
        ReflectionTestUtils.setField(apiKeyAuthServiceClient, "restUrl", restUrl);
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

    private static JWTClaimsSet getJWT(final Object AUD, final String email, final String uuid, final String scopes) {
        return new JWTClaimsSet.Builder()
                .claim(JWTClaimNames.AUDIENCE, AUD)
                .claim(OAUTH_CUSTOM_EMAIL_PARAM, email)
                .claim( OAUTH_CUSTOM_UUID_PARAM, uuid)
                .claim( JWTClaimNames.EXPIRATION_TIME, new Date())
                .claim("scope", scopes).build();

    }
}
