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
    private final IMap<InetAddress, Integer> remoteAddrCounterMap;
    private final IMap<String, Integer> ssoCounterMap;
    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public HazelcastPersonalObjectAccounting(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.remoteAddrCounterMap =  hazelcastInstance.getMap("remoteAddrQueriedPersonal");
        this.ssoCounterMap =  hazelcastInstance.getMap("ssoQueriedPersonal");

        LOGGER.info("hazelcast instances {} members: {} " , this.hazelcastInstance.getName() , this.hazelcastInstance.getCluster().getMembers());
    }

    @Override
    public int getQueriedPersonalObjects(final InetAddress remoteAddress) {
        Integer count = null;
        try {
            count = remoteAddrCounterMap.get(remoteAddress);
        } catch (OperationTimeoutException | IllegalStateException e) {
            // no answer from hazelcast, expected, don't rethrow
            LOGGER.debug("{}: {}", e.getClass().getName(), e.getMessage());
        }

        return count == null ? 0 : count;
    }

    @Override
    public int getQueriedPersonalObjects(final String ssoId) {
        Integer count = null;
        try {
            count = ssoCounterMap.get(ssoId);
        } catch (OperationTimeoutException | IllegalStateException e) {
            // no answer from hazelcast, expected, don't rethrow
            LOGGER.debug("{}: {}", e.getClass().getName(), e.getMessage());
        }

        return count == null ? 0 : count;
    }

    @Override
    public int accountPersonalObject(final InetAddress remoteAddress, final int amount) {
        boolean isLocked = false;

        try {
            if (isLocked = remoteAddrCounterMap.tryLock(remoteAddress,3, TimeUnit.SECONDS)) {
                Integer count = remoteAddrCounterMap.get(remoteAddress);
                count = (count == null) ? amount : (count + amount);
                remoteAddrCounterMap.put(remoteAddress, count);

                return count;
            }

            //if cannot get a lock in specified time, return the current state not zero
            return remoteAddrCounterMap.get(remoteAddress);

        } catch (Exception e) {
            LOGGER.info("Unable to account personal object, allowed by default. Threw {}: {}", e.getClass().getName(), e.getMessage());
        } finally {
            //unlock only if it is locked by this instance
            if(isLocked) {
                unlockKey(remoteAddress);
            }
        }
        return 0;
    }

    @Override
    public int accountPersonalObject(final String ssoId, final int amount) {
        return accountForId(ssoId, amount);
    }

    private int accountForId(final String ssoId, final int amount) {
        boolean isLocked = false;

        try {
            if (isLocked = ssoCounterMap.tryLock(ssoId,3, TimeUnit.SECONDS)) {
                Integer count = ssoCounterMap.get(ssoId);
                count = (count == null) ? amount : (count + amount);
                ssoCounterMap.put(ssoId, count);

                return count;
            }

            //if cannot get a lock in specified time, return the current state not zero
            return ssoCounterMap.get(ssoId);

        } catch (Exception e) {
            LOGGER.info("Unable to account personal object, allowed by default. Threw {}: {}", e.getClass().getName(), e.getMessage());
        } finally {
            //unlock only if it is locked by this instance
            if(isLocked) {
                unlockKey(ssoId);
            }
        }
        return 0;
    }

    private void unlockKey(final InetAddress remoteAddress) {
        try {
            remoteAddrCounterMap.unlock(remoteAddress);
        } catch(Exception e) {
            LOGGER.info("Unable to unlock object key {}. Threw {}: {}", remoteAddress, e.getClass().getName(), e.getMessage());
        }
    }

    private void unlockKey(final String ssoId) {
        try {
            ssoCounterMap.unlock(ssoId);
        } catch(Exception e) {
            LOGGER.info("Unable to unlock object key {}. Threw {}: {}", ssoId, e.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public void resetAccounting() {
        LOGGER.debug("Reset person object counters ({} entries)", remoteAddrCounterMap.size());
        remoteAddrCounterMap.clear();
        ssoCounterMap.clear();
    }
}
