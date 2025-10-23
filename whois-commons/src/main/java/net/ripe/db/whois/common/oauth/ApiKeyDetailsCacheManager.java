package net.ripe.db.whois.common.oauth;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import net.ripe.db.whois.common.dao.KeyCloakApiKeyDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class ApiKeyDetailsCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyDetailsCacheManager.class);

    private final KeyCloakApiKeyDao apiKeyDao;
    private Map<String, LocalDate> keyIdWithExpiry;

    @Autowired
    public ApiKeyDetailsCacheManager(final KeyCloakApiKeyDao apiKeyDao) {
        this.apiKeyDao = apiKeyDao;
    }

    @PostConstruct
    public void init() {
        refreshApiKeyDetailsCaches();
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void refreshApiKeyDetailsCaches() {
        final Map<String, LocalDate> apikeyIdToExpiry;

        try {
            apikeyIdToExpiry = apiKeyDao.getAllKeyIdWithExpiry();
        } catch (final Exception e) {
            LOGGER.warn("Failed to refresh apikey details due to {}: {}", e.getClass().getName(), e.getMessage());
            return;
        }

        if (!MapUtils.isEmpty(apikeyIdToExpiry)) {
            this.keyIdWithExpiry = apikeyIdToExpiry;
        }
    }

    @Nullable
    public LocalDate getExpiryForKeyId(final String keyId) {
       if ( MapUtils.isEmpty(keyIdWithExpiry)) {
           LOGGER.warn("No cache found for apikeys");
           return null;
       }

       return this.keyIdWithExpiry.getOrDefault(keyId, null);
    }
}
