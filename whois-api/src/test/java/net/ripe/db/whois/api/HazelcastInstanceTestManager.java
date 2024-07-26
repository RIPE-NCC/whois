package net.ripe.db.whois.api;


import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.netty.bootstrap.ServerBootstrap;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({WhoisProfile.TEST})
public class HazelcastInstanceTestManager {

    private HazelcastInstance hazelcastInstance = null;

    @Bean
    @Profile(WhoisProfile.TEST)
    public HazelcastInstance hazelcastInstance(final @Value("${hazelcast.port:0}") String port) {
        if (this.hazelcastInstance == null) {
            final Config config = getGenericConfig();
            //We define ipv6 addresses
            config.setProperty("hazelcast.prefer.ipv4.stack", "false");

            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().setPort(Integer.parseInt(port)).setPortAutoIncrement(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setMembers(List.of("localhost")).setEnabled(true);
            this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        }

        return this.hazelcastInstance;
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
