package net.ripe.db.whois.common;

import com.hazelcast.config.AutoDetectionConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import jakarta.annotation.PreDestroy;
import net.ripe.db.whois.common.hazelcast.HazelcastMemberShipListener;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;
import static net.ripe.db.whois.common.hazelcast.HazelcastInstanceManager.getGenericConfig;

@Profile({WhoisProfile.TEST})
@Configuration
@EnableSpringConfigured
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableCaching(mode = AdviceMode.ASPECTJ)
public class TestCacheManagerProvider {

    private CacheManager cacheManager = null;
    private HazelcastInstance hazelcastInstance = null;

    @Bean
    @Profile(WhoisProfile.TEST)
    public HazelcastInstance hazelcastInstance() {
        if (this.hazelcastInstance == null) {
            final Config config = getGenericConfig();

            // standalone
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().setAutoDetectionConfig(new AutoDetectionConfig().setEnabled(false));

            config.getNetworkConfig().setPortAutoIncrement(true);

            // no persistence
            config.getCPSubsystemConfig().setPersistenceEnabled(false);

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
