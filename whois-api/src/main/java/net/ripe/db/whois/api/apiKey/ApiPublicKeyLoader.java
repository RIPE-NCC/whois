package net.ripe.db.whois.api.apiKey;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class ApiPublicKeyLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiPublicKeyLoader.class);

    private final String jwksSetUrl;
    private static final long TTL = 60*60*1000; // 1 hour
    private static final long REFRESH_TIMEOUT = 60*1000; // 1 minute

    @Autowired
    public ApiPublicKeyLoader(@Value("${api.public.key.url}")  final String jwksSetUrl) {
        this.jwksSetUrl = jwksSetUrl;
    }

    public RSAKey loadPublicKey(final String keyId ) throws IOException, KeySourceException {

        LOGGER.debug("Loading public key from {}", this.jwksSetUrl);
        try {
            final JWKSelector selector = new JWKSelector(
                    new JWKMatcher.Builder()
                            .keyType(KeyType.RSA)
                            .keyID(keyId)
                            .keyUse(KeyUse.SIGNATURE)
                            .build());

            final JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(new URL(this.jwksSetUrl))
                    .cache(TTL, REFRESH_TIMEOUT)
                    .build();

            return (RSAKey) jwkSource.get(selector, new SimpleSecurityContext()).getFirst();
        } catch (Exception e) {
            LOGGER.error("Failed to load RSA public key  apikey due to {}:{}", e.getClass().getName(), e.getMessage());
            throw e;
        }
    }
}
