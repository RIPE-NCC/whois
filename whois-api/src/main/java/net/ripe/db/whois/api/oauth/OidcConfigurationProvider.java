package net.ripe.db.whois.api.oauth;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.jwk.source.OutageTolerantJWKSetSource;
import com.nimbusds.jose.jwk.source.RetryingJWKSetSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.GeneralException;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import jakarta.ws.rs.ProcessingException;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_EMAIL_PARAM;
import static net.ripe.db.whois.common.oauth.OAuthUtils.OAUTH_CUSTOM_UUID_PARAM;

@Component
public class OidcConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcConfigurationProvider.class);

    public final static int CONNECT_TIMEOUT_MS = 10_000; // 10 seconds
    public final static int READ_TIMEOUT_MS = 10_000;   // 10 seconds

    private static final long JWKS_CACHE_TIME = 2 * 60 * 60 * 1000L;

    private static final long JWKS_CACHE_REFRESH_TIMEOUT = 30 * 1000L;

    private static final long JWKS_TOKEN_REFRESH_BEFORE_EXPIRE_TIME = 5 * 60 * 1000L;

    private static final long JWKS_WHEN_DOWN_CACHE_TIME = 4 * 60 * 60 * 1000L;

    private final String openIdMetadataUrl;


    private final AtomicReference<OidcConfigurationRecord> oidcConfigurationRecordRef = new AtomicReference<>();

    @Autowired
    public OidcConfigurationProvider(@Value("${openId.metadata.url:}")  final String openIdMetadataUrl) {
        this.openIdMetadataUrl = openIdMetadataUrl;

        getOidcConfigurationOrInitialise();
    }

    @Nullable
    public ConfigurableJWTProcessor<SecurityContext> getProcessorOrInitOidcConfiguration() {
        return getOidcConfigurationOrInitialise() == null ? null : getOidcConfigurationOrInitialise().jwtProcessor;
    }

    @Nullable
    public OIDCProviderMetadata getMetadataOrInitOidcConfiguration(){
        return getOidcConfigurationOrInitialise() == null ? null : getOidcConfigurationOrInitialise().oidcProviderMetadata;
    }

    @Nullable
    private OidcConfigurationRecord getOidcConfigurationOrInitialise() {
        if (oidcConfigurationRecordRef.get() != null){
            return oidcConfigurationRecordRef.get();
        }
        try {
            final OIDCProviderMetadata metadata = getOIDCMetadata();
            oidcConfigurationRecordRef.compareAndSet(null, new OidcConfigurationRecord(metadata, getSecurityContextDefaultJWTProcessor(metadata)));

            return oidcConfigurationRecordRef.get();
        } catch (Exception e) {
            LOGGER.error("Failed to init oidc configuration", e);
            return null;
        }
    }

    private static @NonNull DefaultJWTProcessor<SecurityContext> getSecurityContextDefaultJWTProcessor(final OIDCProviderMetadata metadata) throws MalformedURLException {
        final DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        final JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(
                        new HashSet<>(metadata.getIDTokenJWSAlgs()),
                        getJWKSource(metadata.getJWKSetURI().toURL())
                );

        jwtProcessor.setJWSKeySelector(keySelector);

        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder()
                        .issuer(metadata.getIssuer().getValue())
                        .build(),
                new HashSet<>(Arrays.asList(
                        JWTClaimNames.ISSUER,
                        JWTClaimNames.AUDIENCE,
                        JWTClaimNames.EXPIRATION_TIME,
                        JWTClaimNames.ISSUED_AT,
                        OAUTH_CUSTOM_EMAIL_PARAM,
                        OAUTH_CUSTOM_UUID_PARAM))));
        return jwtProcessor;
    }

    private static JWKSource<SecurityContext> getJWKSource(final URL jwkUri){
        final LoggingResourceRetriever retriever = new LoggingResourceRetriever( new DefaultResourceRetriever(
                CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS,
                JWKSourceBuilder.DEFAULT_HTTP_SIZE_LIMIT
        ));

        return JWKSourceBuilder
                .create(jwkUri, retriever)
                // 2-hour cache and after 30 sec timeout for cache refresh operation to complete after expiration
                .cache(JWKS_CACHE_TIME, JWKS_CACHE_REFRESH_TIMEOUT)
                // 5-minute refresh before the token gets expired
                .refreshAheadCache(JWKS_TOKEN_REFRESH_BEFORE_EXPIRE_TIME, true)
                .retrying(event -> {
                    // Log retry attempt, helps in debugging network timeout issue
                    if (Objects.requireNonNull(event) instanceof RetryingJWKSetSource.RetrialEvent retrialEvent) {
                        LOGGER.warn("JWKS fetch retry: {}: {}", retrialEvent.getException().getClass().getName(), retrialEvent.getException().getMessage());
                    } else {
                        LOGGER.warn("JWKS fetch retry: {}", event);
                    }

                })
                //in case the remote JWK set endpoint goes down set 4 hours value
                .outageTolerant(JWKS_WHEN_DOWN_CACHE_TIME, event -> {
                    if (Objects.requireNonNull(event) instanceof OutageTolerantJWKSetSource.OutageEvent outageEvent) {
                        LOGGER.warn("JWKS outage event: {}: {}", outageEvent.getException().getClass().getName(), outageEvent.getException().getMessage());
                    } else {
                        LOGGER.warn("JWKS outage event : {}", event);
                    }

                })
                .build();
    }

    @RetryFor(value = ProcessingException.class, attempts = 2, intervalMs = 10000)
    private OIDCProviderMetadata getOIDCMetadata() throws GeneralException, IOException {
        return OIDCProviderMetadata.resolve(new Issuer(openIdMetadataUrl));
    }

    public record OidcConfigurationRecord(OIDCProviderMetadata oidcProviderMetadata, ConfigurableJWTProcessor<SecurityContext> jwtProcessor) {

    }
}
