package net.ripe.db.whois.api.httpserver.jmx;

import net.ripe.db.whois.common.hazelcast.BlockedIps;
import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
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

    private final BlockedIps blockedIps;

    public BlockListJmx(final BlockedIps blockedIps) {
        super(LOGGER);
        this.blockedIps = blockedIps;
    }

    @ManagedOperation(description = "adds an IP address to blocked list")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "address", description = "address to block"),
    })
    public String addBlockedListAddress(final String address) {
        blockedIps.addBlockedListAddress(address);
        return String.format("Ipaddress %s added to blocked list", address);
    }

    @ManagedOperation(description = "Remove an IP address to blocked list")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "address", description = "address to remove"),
    })
    public String removeBlockedListAddress(String address) {
        blockedIps.removeBlockedListAddress(address);
        return String.format("Ipaddress %s removed from blocked list", address);
    }

    @ManagedOperation(description = "Retrieve blocked IP list")
    public String getBlockedList() {
        return String.format("%s IPs are blocked", blockedIps.getBlockedList());
    }
}
