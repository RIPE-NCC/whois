package net.ripe.db.whois.common;

import com.google.common.collect.Lists;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import jakarta.annotation.PreDestroy;
import net.ripe.db.whois.common.hazelcast.HazelcastMemberShipListener;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

import static net.ripe.db.whois.common.hazelcast.HazelcastInstanceManager.getGenericConfig;

@Profile({WhoisProfile.TEST})
@Configuration
public class TestCacheManagerProvider {

    private CacheManager cacheManager = null;
    private HazelcastInstance hazelcastInstance = null;

    @Bean
    @Profile(WhoisProfile.TEST)
    public HazelcastInstance hazelcastInstance() {
        if (this.hazelcastInstance == null) {
            final Config config = getGenericConfig();
            config.getNetworkConfig().setPortAutoIncrement(true);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setMembers(Arrays.asList("127.0.0.1"));

            this.hazelcastInstance = getHazelcastInstance(config);
        }

        return this.hazelcastInstance;
    }

    @Bean(name = "cacheManager")
    @Profile(WhoisProfile.TEST)
    public CacheManager cacheManagerInstance() {
        if (this.cacheManager == null) {
            this.cacheManager = getCacheManagerInstance();
        }

        return this.cacheManager;
    }

    private HazelcastInstance getHazelcastInstance(final Config config) {
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        instance.getCluster().addMembershipListener(new HazelcastMemberShipListener());
        return instance;
    }

    private CacheManager getCacheManagerInstance() {
        return new HazelcastCacheManager(hazelcastInstance());
    }

    @PreDestroy
    public void shutDownHazelcast() {
        if (this.hazelcastInstance != null) {
            this.hazelcastInstance.shutdown();
        }
    }
}
