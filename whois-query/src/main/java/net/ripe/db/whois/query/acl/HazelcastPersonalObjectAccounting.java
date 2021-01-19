package net.ripe.db.whois.query.acl;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OperationTimeoutException;
import com.hazelcast.map.IMap;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@DeployedProfile
@Primary
@Component
public class HazelcastPersonalObjectAccounting implements PersonalObjectAccounting {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastPersonalObjectAccounting.class);

    private static IMap<InetAddress, Integer> counterMap;

    private static volatile HazelcastInstance instance;

    static synchronized void startHazelcast() {
        if (instance != null) {
            throw new IllegalStateException("Hazelcast already started");
        }

        //TODO: use  efs mount for hazelcast config, instead of java config. This is just for testing
        Config config = new Config("hz_instance_prepdev");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(true);
        config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface("10.0.*.*");

        instance = Hazelcast.newHazelcastInstance(config);
        instance.getCluster().addMembershipListener(new HazelcastMemberShipListner());

        LOGGER.info("hazelcast instances : " + instance.getName() +  " members: " + instance.getCluster().getMembers());
        LOGGER.info("hazelcast instances interfaces  : " + instance.getConfig().getNetworkConfig().getInterfaces());

        counterMap = instance.getMap("queriedPersonal");
    }

    static void shutdownHazelcast() {
        LOGGER.debug("Shutting down hazelcast instance");

        instance.getLifecycleService().shutdown();
        instance = null;
    }

    @PostConstruct
    public void startService() {
        startHazelcast();
    }

    @PreDestroy
    public void stopService() {
        shutdownHazelcast();
    }

    @Override
    public int getQueriedPersonalObjects(final InetAddress remoteAddress) {
        Integer count = null;
        try {
            count = counterMap.get(remoteAddress);
        } catch (OperationTimeoutException | IllegalStateException e) {
            // no answer from hazelcast, expected, don't rethrow
            LOGGER.debug("{}: {}", e.getClass().getName(), e.getMessage());
        }

        if (count == null) {
            return 0;
        }

        return count;
    }

    @Override
    public int accountPersonalObject(final InetAddress remoteAddress, final int amount) {
        try {
            counterMap.lock(remoteAddress, 3, TimeUnit.SECONDS);

            Integer count = counterMap.get(remoteAddress);
            count = (count == null)  ? amount :  count + amount;

            counterMap.put(remoteAddress, count);
            return count;
        } catch (Exception e) {
            LOGGER.info("Unable to account personal object, allowed by default. Threw {}: {}", e.getClass().getName(), e.getMessage());
        } finally {
            counterMap.unlock(remoteAddress);
        }
        return 0;
    }

    @Override
    public void resetAccounting() {
        LOGGER.debug("Reset person object counters ({} entries)", counterMap.size());
        counterMap.clear();
    }
}
