package net.ripe.db.whois.common;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.sso.domain.ValidateTokenResponse;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "CacheManager", description = "Cache Manager operations")
public class CacheManagerJmx extends JmxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerJmx.class);

    private final CacheManager cacheManager;

    @Autowired
    public CacheManagerJmx(final CacheManager cacheManager) {
        super(LOGGER);
        this.cacheManager = cacheManager;
    }

    @ManagedOperation(description = "Get status of SSO Validate Token cache")
    public void ssoValidateTokenStatus() {
        final Cache<String, ValidateTokenResponse> cache = cacheManager.getCache("ssoValidateToken", String.class, ValidateTokenResponse.class);
        if (cache == null) {
            LOGGER.info("cache not found");
        } else {
            LOGGER.info("cache: {}", cache.getClass().getName());
        }
    }

    @ManagedOperation(description = "Clear contents of SSO Validate Token cache")
    public void clearSsoValidateToken() {
        final Cache<String, ValidateTokenResponse> cache = cacheManager.getCache("ssoValidateToken", String.class, ValidateTokenResponse.class);
        cache.clear();
    }



}
