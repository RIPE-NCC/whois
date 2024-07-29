package net.ripe.db.whois.common.hazelcast;

import com.google.common.base.Joiner;
import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "BlockedList", description = "Whois black list")
public class BlockListJmx extends JmxBase {

    private static final Logger LOGGER = getLogger(BlockListJmx.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final ISet<Ipv4Resource> ipv4blockedSet;
    private final ISet<Ipv6Resource> ipv6blockedSet;
    private final HazelcastInstance hazelcastInstance;

    public BlockListJmx(final HazelcastInstance hazelcastInstance, @Value("${ipranges.untrusted:}") final String untrustedIpRanges) {
        super(LOGGER);
        this.hazelcastInstance = hazelcastInstance;
        ipv4blockedSet = hazelcastInstance.getSet("ipv4blockedSet");
        ipv6blockedSet = hazelcastInstance.getSet("ipv6blockedSet");

        for (final String address : StringUtil.csvSplit(untrustedIpRanges)) {
            addBlockedListAddress(address);
        }

        LOGGER.info("hazelcast instances {} members: {} " , this.hazelcastInstance.getName() , this.hazelcastInstance.getCluster().getMembers());
    }

    @ManagedOperation(description = "adds an IP address to blocked list")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "address", description = "address to block"),
    })
    public String addBlockedListAddress(final String address) {
        if (address.contains(".")) {
            ipv4blockedSet.add(Ipv4Resource.parse(address));
        } else {
            ipv6blockedSet.add(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} added to blocked list", address);
        return String.format("Ipaddress %s added to blocked list", address);
    }

    @ManagedOperation(description = "Remove an IP address to blocked list")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "address", description = "address to remove"),
    })
    public String removeBlockedListAddress(String address) {
        if (address.contains(".")){
            ipv4blockedSet.remove(Ipv4Resource.parse(address));
        } else {
            ipv6blockedSet.remove(Ipv6Resource.parse(address));
        }
        LOGGER.info("Ipaddress {} removed from blocked list", address);
        return String.format("Ipaddress %s removed from blocked list", address);
    }

    @ManagedOperation(description = "Retrieve blocked list")
    public String getBlockedList() {
        StringBuilder result = new StringBuilder();
        COMMA_JOINER.appendTo(result, ipv4blockedSet);

        if (!ipv6blockedSet.isEmpty()){
            result.append(',');
            COMMA_JOINER.appendTo(result, ipv6blockedSet);
        }

        LOGGER.info("The blocked list contains next IPs {}", result);
        return String.format("The blocked list contains next IPs %s", result);
    }

    public ISet<Ipv4Resource> getIpv4blockedSet() {
        return ipv4blockedSet;
    }

    public ISet<Ipv6Resource> getIpv6blockedSet() {
        return ipv6blockedSet;
    }
}
