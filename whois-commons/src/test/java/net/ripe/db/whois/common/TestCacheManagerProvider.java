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

@Profile({WhoisProfile.TEST})
@EnableCaching(mode = AdviceMode.ASPECTJ)
@Configuration
public class TestCacheManagerProvider {

    private CacheManager cacheManager = null;

    @Bean(name = "cacheManager")
    public CacheManager cacheManagerInstance() {
        if (this.cacheManager == null) {
            final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
            final List<Cache> caches = Lists.newArrayList();
            caches.add(new ConcurrentMapCache("ssoUuid"));
            caches.add(new ConcurrentMapCache("ssoUserDetails"));
            caches.add(new ConcurrentMapCache("ssoValidateToken"));
            caches.add(new ConcurrentMapCache("ssoHistoricalUserDetails"));
            caches.add(new ConcurrentMapCache("JWTpublicKeyDetails"));
            simpleCacheManager.setCaches(caches);
            this.cacheManager = simpleCacheManager;
        }

        return this.cacheManager;
    }

}
