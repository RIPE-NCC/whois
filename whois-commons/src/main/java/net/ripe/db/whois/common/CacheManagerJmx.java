package net.ripe.db.whois.common;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@DeployedProfile
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
        final Cache cache = cacheManager.getCache("ssoValidateToken");
        if (cache == null) {
            LOGGER.info("cache not found");
        } else {
            // TODO: [ES] log statistics
            LOGGER.info("cache: {}", cache.getClass().getName());
        }
    }

    @ManagedOperation(description = "Clear contents of SSO Validate Token cache")
    public void clearSsoValidateToken() {
        final Cache cache = cacheManager.getCache("ssoValidateToken");
        cache.clear();
    }

    // TODO: [ES] support *all* caches

}
