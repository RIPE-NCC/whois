package net.ripe.db.whois.common;

import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.domain.IpRanges;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceMode {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceMode.class);

    public enum AccessType {FULL, READONLY, NONE};

    private final IpRanges ipRanges;

    private AccessType world = AccessType.FULL;
    private AccessType trusted = AccessType.FULL;

    // TODO: [AH] this is only supported for non-http services at the moment (aka query, nrtm) for which one has to use a 'dumb' OSI layer2 load balancer
    private long shutdownTime = 0;

    @Autowired
    public MaintenanceMode(final IpRanges ipRanges) {
        this.ipRanges = ipRanges;
    }

    @Value("${maintenance:full,full}")
    public void set(String mode) {
        AccessType world = null, trusted = null;
        try {
            String[] modes = StringUtils.split(mode, ',');
            world = AccessType.valueOf(modes[0].trim());
            trusted = AccessType.valueOf(modes[1].trim());
        } catch (RuntimeException e) {}

        if (world != null && trusted != null) {
            set(world, trusted);
        }
    }

    public void set(AccessType world, AccessType trusted) {
        LOGGER.warn(String.format("Access type change: world %s -> %s, trusted: %s -> %s", this.world, world, this.trusted, trusted));

        this.world = world;
        this.trusted = trusted;
    }

    public void setShutdown() {
        shutdownTime = System.currentTimeMillis();
    }

    public long shutdownInitiated() {
        return System.currentTimeMillis() - shutdownTime;
    }

    /* for services where client IP address is not available/does not apply */
    public boolean allowRead() {
        return world != AccessType.NONE;
    }

    public boolean allowRead(Interval ipResource) {
        if (shutdownTime > 0 && ipRanges.isLoadbalancer(ipResource)) return false;
        if (world != AccessType.NONE) return true;

        if (ipRanges.isTrusted(ipResource)) {
            return trusted != AccessType.NONE;
        }

        return false;
    }

    /* for services where client IP address is not available/does not apply */
    public boolean allowUpdate() {
        return world == AccessType.FULL;
    }

    public boolean allowUpdate(Interval ipResource) {
        if (world == AccessType.FULL) return true;

        if (ipRanges.isTrusted(ipResource)) {
            return trusted == AccessType.FULL;
        }

        return false;
    }

    public AccessType getFor(Interval ipResource) {
        if (ipRanges.isTrusted(ipResource)) {
            return trusted;
        } else {
            return world;
        }
    }
}
