package net.ripe.db.whois.common;

import net.ripe.db.whois.common.jmx.JmxBase;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
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

    @ManagedOperation(description = "Get cache status")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "name", description = "cache name"),
    })
    public String status(final String name) {
        backgroundOperation("get cache status", name, () -> {

            if (!cacheManager.cacheExists(name)) {
                LOGGER.error("Cache {} doesn't exist?", name);
                return null;
            }

            LOGGER.info(
                "Cache {} is {}: Keys {} Size {} Evicted {} Expired {} Hits {} Hit Ratio {} Misses {} Puts {} Size {}",
                    name,
                    cacheManager.getCache(name).getStatus(),
                    cacheManager.getCache(name).getKeys().size(),
                    cacheManager.getCache(name).getSize(),
                    cacheManager.getCache(name).getStatistics().cacheEvictedCount(),
                    cacheManager.getCache(name).getStatistics().cacheExpiredCount(),
                    cacheManager.getCache(name).getStatistics().cacheHitCount(),
                    cacheManager.getCache(name).getStatistics().cacheHitRatio(),
                    cacheManager.getCache(name).getStatistics().cacheMissCount(),
                    cacheManager.getCache(name).getStatistics().cachePutCount(),
                    cacheManager.getCache(name).getStatistics().getSize());

            return null;
        });
        return "Get cache status " + name;
    }

    @ManagedOperation(description = "Clear contents of cache")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "prefix", description = "clear contents of cache(s) starting with prefix"),
    })
    public String clearAll(final String prefix) {
        backgroundOperation("clear contents of cache(s) starting with prefix", prefix, () -> {
            cacheManager.clearAllStartingWith(prefix);
            LOGGER.info("cacheManager.clearAllStartingWith {} completed.", prefix);
            return null;
        });
        return "Clear contents of cache(s) starting with prefix " + prefix;
    }



}
