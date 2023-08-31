package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({WhoisProfile.TEST})
@Configuration
public class TestCacheManagerProvider {

    private CacheManager cacheManager = null;

    @Bean(name = "cacheManager")
    public CacheManager cacheManagerInstance() {
        if (this.cacheManager == null) {
            this.cacheManager = new SimpleCacheManager();
        }

        return this.cacheManager;
    }

}
