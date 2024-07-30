package net.ripe.db.whois.common.hazelcast;

import com.google.common.base.Joiner;
import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import net.ripe.db.whois.common.ip.IpInterval;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HazelcastBlockList {

    private static final Logger LOGGER = getLogger(HazelcastBlockList.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final ISet<IpInterval> ipBlockedSet;

    public HazelcastBlockList(final HazelcastInstance hazelcastInstance,
                        @Value("${ipranges.blocked.list:}") final String blockedListIps) {

        ipBlockedSet = hazelcastInstance.getSet("ipBlockedSet");

        for (final String address : StringUtil.csvSplit(blockedListIps)) {
            addBlockedListAddress(address);
        }

        LOGGER.info("hazelcast instances {} members: {} " , hazelcastInstance.getName() , hazelcastInstance.getCluster().getMembers());
    }

    public void addBlockedListAddress(final String address) {
        ipBlockedSet.add(IpInterval.parse(address));
        LOGGER.info("Ipaddress {} added to blocked list", address);
    }

    public void removeBlockedListAddress(String address) {
        ipBlockedSet.remove(IpInterval.parse(address));
        LOGGER.info("Ipaddress {} removed from blocked list", address);
    }

    public String getBlockedList() {
        StringBuilder result = new StringBuilder();
        COMMA_JOINER.appendTo(result, ipBlockedSet);

        LOGGER.info("The blocked list contains next IPs {}", result);
        return String.format("The blocked list contains next IPs %s", result);
    }

    public ISet<IpInterval> getIpBlockedSet() {
        return ipBlockedSet;
    }

}
