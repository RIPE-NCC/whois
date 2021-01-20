package net.ripe.db.whois.query.acl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OperationTimeoutException;
import com.hazelcast.map.IMap;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@DeployedProfile
@Primary
@Component
public class HazelcastPersonalObjectAccounting implements PersonalObjectAccounting {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastPersonalObjectAccounting.class);

    private IMap<InetAddress, Integer> counterMap;
    private HazelcastInstance hazelcastInstance;

    @Autowired
    public HazelcastPersonalObjectAccounting(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.counterMap =  hazelcastInstance.getMap("queriedPersonal");

        LOGGER.info("hazelcast instances : " + this.hazelcastInstance.getName() +  " members: " + this.hazelcastInstance.getCluster().getMembers());
    }

    @PreDestroy
    public void stopService() {
        hazelcastInstance.getLifecycleService().shutdown();
        hazelcastInstance = null;
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
            counterMap.unlock(remoteAddress);

            return count;
        } catch (IllegalStateException e) {
            LOGGER.info("Unable to account personal object, allowed by default. Threw {}: {}", e.getClass().getName(), e.getMessage());
        }
        return 0;
    }

    @Override
    public void resetAccounting() {
        LOGGER.debug("Reset person object counters ({} entries)", counterMap.size());
        counterMap.clear();
    }
}
