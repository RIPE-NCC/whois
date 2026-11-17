package net.ripe.db.whois.api.security;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.jwk.source.OutageTolerantJWKSetSource;
import com.nimbusds.jose.jwk.source.RetryingJWKSetSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.oauth2.sdk.GeneralException;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import jakarta.ws.rs.ProcessingException;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class OidcConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcConfigurationProvider.class);

    private final static int CONNECT_TIMEOUT_MS = 10_000; // 10 seconds
    private final static int READ_TIMEOUT_MS = 10_000;   // 10 seconds

    private static final long JWKS_CACHE_TIME = 24 * 60 * 60 * 1000L;  // keep the keys (returned by cert) for 24 hours

    private static final long JWKS_CACHE_REFRESH_TIMEOUT = 30 * 1000L;

    private static final long JWKS_TOKEN_REFRESH_BEFORE_EXPIRE_TIME = 10 * 60 * 1000L; //Refresh 10 minutes before 24 hours

    private static final long JWKS_WHEN_DOWN_CACHE_TIME = 48 * 60 * 60 * 1000L; // If keycloak is down, keep the keys for 48 hours

    private final String openIdMetadataUrl;

    private final AtomicReference<OidcConfigurationRecord> oidcConfigurationRecordRef = new AtomicReference<>();

    @Autowired
    public OidcConfigurationProvider(@Value("${openId.metadata.url:}")  final String openIdMetadataUrl) {
        this.openIdMetadataUrl = openIdMetadataUrl;

        getOidcConfigurationOrInitialise();
    }

    @Nullable
    public OIDCProviderMetadata getMetadataOrInitOidcConfiguration(){
        final OidcConfigurationRecord configuration = getOidcConfigurationOrInitialise();
        return configuration == null ? null : configuration.oidcProviderMetadata();
    }

    @Nullable
    public OidcConfigurationRecord getOidcConfigurationOrInitialise() {
        if (oidcConfigurationRecordRef.get() != null){
            return oidcConfigurationRecordRef.get();
        }
        try {
            final OIDCProviderMetadata metadata = getOIDCMetadata();
            oidcConfigurationRecordRef.compareAndSet(null, buildRecord(metadata));

            return oidcConfigurationRecordRef.get();
        } catch (Exception e) {
            LOGGER.error("Failed to init oidc configuration", e);
            return null;
        }
    }

    private static OidcConfigurationRecord buildRecord(final OIDCProviderMetadata metadata) throws MalformedURLException {
        return new OidcConfigurationRecord(metadata, getJWKSource(metadata.getJWKSetURI().toURL()));
    }

    private static JWKSource<SecurityContext> getJWKSource(final URL jwkUri){
        final LoggingResourceRetriever retriever = new LoggingResourceRetriever( new DefaultResourceRetriever(
                CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS,
                JWKSourceBuilder.DEFAULT_HTTP_SIZE_LIMIT
        ));

        return JWKSourceBuilder
                .create(jwkUri, retriever)
                // Cache the downloaded JWK set (the IdP public signing keys); a request that finds it expired
                // waits at most JWKS_CACHE_REFRESH_TIMEOUT for the refetch to complete
                .cache(JWKS_CACHE_TIME, JWKS_CACHE_REFRESH_TIMEOUT)
                // Refetch the JWK set in the background shortly before the cached copy expires, so requests don't wait for it
                .refreshAheadCache(JWKS_TOKEN_REFRESH_BEFORE_EXPIRE_TIME, true)
                .retrying(event -> {
                    // Log retry attempt, helps in debugging network timeout issue
                    if (Objects.requireNonNull(event) instanceof RetryingJWKSetSource.RetrialEvent retrialEvent) {
                        LOGGER.warn("JWKS fetch retry: {}: {}", retrialEvent.getException().getClass().getName(), retrialEvent.getException().getMessage());
                    } else {
                        LOGGER.warn("JWKS fetch retry: {}", event);
                    }

                })
                // If the IdP's certs endpoint is down, keep using the last good JWK set for up to JWKS_WHEN_DOWN_CACHE_TIME
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

    public record OidcConfigurationRecord(OIDCProviderMetadata oidcProviderMetadata, JWKSource<SecurityContext> jwkSource) {

    }
}
