package net.ripe.db.whois.query.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@Configuration
@DeployedProfile
public class HazelcastInstanceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastInstanceManager.class);

    final String interfaces;
    final int port;

    @Autowired
    public HazelcastInstanceManager(@Value("${hazelcast.config.interfaces:localhost}") final String interfaces, @Value("${hazelcast.port:5701}") final int port) {
        this.interfaces = interfaces;
        this.port = port;
    }

    @Bean
    @Profile(WhoisProfile.RIPE_DEPLOYED)
    public HazelcastInstance hazelcastInstance() {
      LOGGER.info("Creating hazelcast instance with Ripe deployed profile");

      final Config config = getGenericConfig();
      //We define ipv6 addresses
      config.setProperty("hazelcast.prefer.ipv4.stack", "false");

      config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
      config.getNetworkConfig().setPort(port).setPortAutoIncrement(false);
      config.getNetworkConfig().getJoin().getTcpIpConfig().setMembers(Arrays.asList(interfaces.split(","))).setEnabled(true);

      return getHazelcastInstance(config);
    }

    @Bean
    @Profile(WhoisProfile.AWS_DEPLOYED)
    @Primary
    public HazelcastInstance hazelcastAwsInstance() {
        LOGGER.info("Creating hazelcast instance with AWS deployed profile");

        final Config config = getGenericConfig();
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(true);
        config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface(interfaces);

        return getHazelcastInstance(config);
    }

    private HazelcastInstance getHazelcastInstance(Config config) {
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        instance.getCluster().addMembershipListener(new HazelcastMemberShipListener());
        return instance;
    }

    private Config getGenericConfig() {
        final Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        config.setProperty("hazelcast.jmx", "true")
                .setProperty("hazelcast.version.check.enabled", "false")
                .setProperty("hazelcast.memcache.enabled","false")
                .setProperty("hazelcast.redo.giveup.threshold","10")
                .setProperty("hazelcast.logging.type","slf4j");

        return config;
    }
}
