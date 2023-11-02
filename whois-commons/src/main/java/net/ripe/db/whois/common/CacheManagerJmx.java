package net.ripe.db.whois.common;

import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import com.hazelcast.spring.cache.HazelcastCache;
import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
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

    @ManagedOperation(description = "Get cache status")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "name", description = "cache name"),
    })
    public void getStatus(final String name) {
        final HazelcastCache cache = (HazelcastCache)cacheManager.getCache(name);
        final IMap<Object, Object> nativeCache = cache.getNativeCache();
        final LocalMapStats localMapStats = nativeCache.getLocalMapStats();
        LOGGER.info("{} cache size is {} status is {}", name, nativeCache.size(), localMapStats);
    }

    @ManagedOperation(description = "Clear contents of cache")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "name", description = "clear contents of named cache"),
    })
    public void clearAll(final String name) {
        final Cache cache = cacheManager.getCache(name);
        cache.clear();
    }
}
