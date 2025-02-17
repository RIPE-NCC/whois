package net.ripe.db.whois.api.apiKey;

import com.nimbusds.jose.jwk.JWKSet;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

@Component
public class ApiPublicKeyLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiPublicKeyLoader.class);

    private final String jwksSetUrl;

    @Autowired
    public ApiPublicKeyLoader(@Value("${api.public.key.url}")  final String jwksSetUrl) {
        this.jwksSetUrl = jwksSetUrl;
    }

    @Cacheable(cacheNames = "JWTpublicKeyDetails")
    public JWKSet loadPublicKey() throws IOException, ParseException {

        LOGGER.info("Loading public key from {}", this.jwksSetUrl);
        try {
            return JWKSet.load(new URL(this.jwksSetUrl));
        } catch (Exception e) {
            LOGGER.error("Failed to load RSA public key  apikey due to {}:{}", e.getClass().getName(), e.getMessage());
            throw e;
        }
    }
}
