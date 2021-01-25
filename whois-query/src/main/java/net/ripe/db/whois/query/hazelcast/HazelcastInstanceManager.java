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

@Configuration
@DeployedProfile
public class HazelcastInstanceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastInstanceManager.class);

    final String interfaces;

    @Autowired
    public HazelcastInstanceManager( @Value("${hazelcast.config.interaces:10.*.*.*}") final String interfaces) {
        this.interfaces = interfaces;
    }

    @Bean
    @Profile(WhoisProfile.RIPE_DEPLOYED)
    public HazelcastInstance hazelcastInstance() {
      LOGGER.info("Creating hazelcast instance with Ripe deployed profile");
      final HazelcastInstance instance =  Hazelcast.newHazelcastInstance(null);
      instance.getCluster().addMembershipListener(new HazelcastMemberShipListner());

      return instance;
    }

    @Bean
    @Profile(WhoisProfile.AWS_DEPLOYED)
    @Primary
    public HazelcastInstance hazelcastAwsInstance() {
        LOGGER.info("Creating hazelcast instance with AWS deployed profile, interfaces {}", interfaces);

        final Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(true);
        config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface(interfaces);

        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        instance.getCluster().addMembershipListener(new HazelcastMemberShipListner());

        return instance;
    }
}
