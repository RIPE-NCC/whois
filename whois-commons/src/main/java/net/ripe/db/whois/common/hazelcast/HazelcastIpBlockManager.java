package net.ripe.db.whois.common.hazelcast;

import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


import java.net.InetAddress;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@Profile({WhoisProfile.DEPLOYED})
public class HazelcastIpBlockManager implements IpBlockManager {

    private static final Logger LOGGER = getLogger(HazelcastIpBlockManager.class);

    private final ISet<IpInterval> ipBlockedSet;

    public HazelcastIpBlockManager(final HazelcastInstance hazelcastInstance,
                                   @Value("${ipranges.blocked.list:}") final String blockedListIps) {
        ipBlockedSet = hazelcastInstance.getSet("ipBlockedSet");
        ipBlockedSet.addAll(getBlockedIntervals(blockedListIps));
    }

    @Override
    public void addBlockedListAddress(final String address) {
        ipBlockedSet.add(IpInterval.parse(address));
        LOGGER.info("Ipaddress {} added to blocked list", address);
    }

    @Override
    public void removeBlockedListAddress(String address) {
        ipBlockedSet.remove(IpInterval.parse(address));
        LOGGER.info("Ipaddress {} removed from blocked list", address);
    }

    @Override
    public String getBlockedList() {
        return StringUtils.join(ipBlockedSet, ',');
    }

    @Override
    public ISet<IpInterval> getIpBlockedSet() {
        return ipBlockedSet;
    }

    @Override
    public boolean isBlockedIp(final InetAddress candidate) {
        final IpInterval<?> parsed = IpInterval.asIpInterval(candidate);
        try {
            return getIpBlockedSet().stream()
                    .anyMatch(ipRange -> ipRange.getClass().equals(parsed.getClass()) && ipRange.contains(parsed));
        } catch (Exception ex){
            LOGGER.error("Failed to check if remote address is in block list due to", ex);
        }
        return false;
    }
}
