package net.ripe.db.whois.common;

import com.hazelcast.cache.CacheNotExistsException;
import com.hazelcast.cache.ICache;
import com.hazelcast.core.HazelcastInstance;
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
    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public CacheManagerJmx(final CacheManager cacheManager, final HazelcastInstance hazelcastInstance) {
        super(LOGGER);
        this.cacheManager = cacheManager;
        this.hazelcastInstance = hazelcastInstance;
    }

    @ManagedOperation(description = "Get cache status")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "name", description = "cache name"),
    })
    public void getStatus(final String name) {
        final ICache<Object, Object> cache;
        try {
            cache = hazelcastInstance.getCacheManager().getCache(name);
        } catch (CacheNotExistsException e) {
            throw new IllegalStateException(e);
        }

        final ICache unwrappedCache =  cache.unwrap( ICache.class );
        final com.hazelcast.cache.CacheStatistics cacheStatistics = unwrappedCache.getLocalCacheStatistics();
        LOGGER.info("{} cache: gets {} size {}", name, cacheStatistics.getCacheGets(), cache.size());
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
