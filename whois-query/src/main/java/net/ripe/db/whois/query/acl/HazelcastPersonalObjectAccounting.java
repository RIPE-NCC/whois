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

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@DeployedProfile
@Primary
@Component
public class HazelcastPersonalObjectAccounting implements PersonalObjectAccounting {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastPersonalObjectAccounting.class);

    private final IMap<String, Integer> counterMap;
    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public HazelcastPersonalObjectAccounting(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.counterMap =  hazelcastInstance.getMap("queriedPersonal");

        LOGGER.info("hazelcast instances {} members: {} " , this.hazelcastInstance.getName() , this.hazelcastInstance.getCluster().getMembers());
    }

    @Override
    public int getQueriedPersonalObjects(final String identifier) {
        Integer count = null;
        try {
            count = counterMap.get(identifier);
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
    public int accountPersonalObject(final String identifier, final int amount) {
        boolean isLocked = false;

        try {
            if (isLocked = counterMap.tryLock(identifier,3, TimeUnit.SECONDS)) {
                Integer count = counterMap.get(identifier);
                count = (count == null) ? amount : (count + amount);
                counterMap.put(identifier, count);

                return count;
            }

            //if cannot get a lock in specified time, return the current state not zero
            return counterMap.get(identifier);

        } catch (Exception e) {
            LOGGER.info("Unable to account personal object, allowed by default. Threw {}: {}", e.getClass().getName(), e.getMessage());
        } finally {
            //unlock only if it is locked by this instance
            if(isLocked) {
                unlockKey(identifier);
            }
        }
        return 0;
    }

    private void unlockKey(final String identifier) {
        try {
            counterMap.unlock(identifier);
        } catch(Exception e) {
            LOGGER.info("Unable to unlock object key {}. Threw {}: {}", identifier, e.getClass().getName(), e.getMessage());
        }

    }

    @Override
    public void resetAccounting() {
        LOGGER.debug("Reset person object counters ({} entries)", counterMap.size());
        counterMap.clear();
    }
}
