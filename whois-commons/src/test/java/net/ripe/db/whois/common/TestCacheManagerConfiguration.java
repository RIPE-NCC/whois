package net.ripe.db.whois.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static net.ripe.db.whois.common.hazelcast.HazelcastCacheManagerConfiguration.API_KEY_OAUTH;
import static net.ripe.db.whois.common.hazelcast.HazelcastCacheManagerConfiguration.SSO_HISTORICAL_USER_DETAILS;
import static net.ripe.db.whois.common.hazelcast.HazelcastCacheManagerConfiguration.SSO_USER_DETAILS;
import static net.ripe.db.whois.common.hazelcast.HazelcastCacheManagerConfiguration.SSO_UUID;
import static net.ripe.db.whois.common.hazelcast.HazelcastCacheManagerConfiguration.SSO_VALIDATE_TOKEN;
import static net.ripe.db.whois.common.hazelcast.HazelcastCacheManagerConfiguration.USER_BY_EMAIL;

@Profile({WhoisProfile.TEST})
@EnableCaching(mode = AdviceMode.ASPECTJ)
@Configuration
public class TestCacheManagerConfiguration {

    private CacheManager cacheManager = null;

    @Bean(name = "cacheManager")
    public CacheManager cacheManagerInstance() {
        if (this.cacheManager == null) {
            final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
            final List<Cache> caches = Lists.newArrayList();
            caches.add(new ConcurrentMapCache(SSO_UUID));
            caches.add(new ConcurrentMapCache(SSO_USER_DETAILS));
            caches.add(new ConcurrentMapCache(SSO_VALIDATE_TOKEN));
            caches.add(new ConcurrentMapCache(SSO_HISTORICAL_USER_DETAILS));
            caches.add(new ConcurrentMapCache(USER_BY_EMAIL));
            caches.add(new ConcurrentMapCache(API_KEY_OAUTH));
            simpleCacheManager.setCaches(caches);
            this.cacheManager = simpleCacheManager;
        }

        return this.cacheManager;
    }

}
