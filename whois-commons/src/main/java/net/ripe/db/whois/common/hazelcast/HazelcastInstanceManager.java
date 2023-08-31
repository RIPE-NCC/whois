package net.ripe.db.whois.common.hazelcast;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@Configuration
@DeployedProfile
public class HazelcastInstanceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastInstanceManager.class);

    final String interfaces;
    final int port;

    private HazelcastInstance hazelcastInstance = null;
    private CacheManager cacheManager = null;

    @Autowired
    public HazelcastInstanceManager(@Value("${hazelcast.config.interfaces:localhost}") final String interfaces, @Value("${hazelcast.port:5701}") final int port) {
        this.interfaces = interfaces;
        this.port = port;
    }

    // TODO: [ES] can configuration be simplified?
    @Bean
    @Profile(WhoisProfile.DEPLOYED)
    public HazelcastInstance hazelcastInstance() {
        if (this.hazelcastInstance == null) {
            LOGGER.info("Creating hazelcast instance with Ripe deployed profile");

            final Config config = getGenericConfig();
            //We define ipv6 addresses
            config.setProperty("hazelcast.prefer.ipv4.stack", "false");

            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().setPort(port).setPortAutoIncrement(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setMembers(Arrays.asList(interfaces.split(","))).setEnabled(true);

            // TODO: [ES] time to live eviction not supported?
            final EvictionConfig evictionConfig = new EvictionConfig().setSize(10000).setEvictionPolicy(EvictionPolicy.LRU);

            // TODO: [ES] is this cache configuration necessary? Is it done already automatically (based on the annotation) ?

            final CacheSimpleConfig ssoValidateTokenConfig = new CacheSimpleConfig("ssoValidateToken")
                .setKeyType("java.lang.String")
                .setValueType("net.ripe.db.whois.common.sso.domain.ValidateTokenResponse")
                .setEvictionConfig(evictionConfig);
            config.addCacheConfig(ssoValidateTokenConfig);

            final CacheSimpleConfig ssoUuidConfig = new CacheSimpleConfig("ssoUuid")
                .setKeyType("java.lang.String")
                .setValueType("java.lang.String")
                .setEvictionConfig(evictionConfig);
            config.addCacheConfig(ssoUuidConfig);

            final CacheSimpleConfig ssoUserDetailsConfig = new CacheSimpleConfig("ssoUserDetails")
                .setKeyType("java.lang.String")
                .setValueType("net.ripe.db.whois.common.sso.domain.ValidateTokenResponse")
                .setEvictionConfig(evictionConfig);
            config.addCacheConfig(ssoUserDetailsConfig);

            this.hazelcastInstance = getHazelcastInstance(config);
        }

        return this.hazelcastInstance;
    }

    @Bean(name = "cacheManager")
    @Profile(WhoisProfile.DEPLOYED)
    public CacheManager cacheManagerInstance() {
        if (this.cacheManager == null) {
            LOGGER.info("Creating cache manager instance with Ripe deployed profile");
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

    private Config getGenericConfig() {
        final Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        config.setProperty("hazelcast.jmx", "true")
                .setProperty("hazelcast.version.check.enabled", "false")
                .setProperty("hazelcast.phone.home.enabled", "false")
                .setProperty("hazelcast.memcache.enabled","false")
                .setProperty("hazelcast.redo.giveup.threshold","10")
                .setProperty("hazelcast.logging.type","slf4j")
                .setProperty("hazelcast.shutdownhook.enabled","false")
                .setProperty("hazelcast.graceful.shutdown.max.wait","60");

        config.getCPSubsystemConfig().setPersistenceEnabled(false);

        return config;
    }
}
