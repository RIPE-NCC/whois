package net.ripe.db.whois.common.domain;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.etree.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class IpRanges {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpRanges.class);

    private Set<Interval> ripeRanges;

    @Value("${ipranges.trusted}")
    public void setRipeRanges(final String... ripeRanges) {
        final Set<Interval> ipResources = Sets.newLinkedHashSetWithExpectedSize(ripeRanges.length);
        for (final String ripeRange : ripeRanges) {
            ipResources.add(IpInterval.parse(ripeRange));
        }

        this.ripeRanges = ipResources;
        LOGGER.info("Trusted ranges: {}", this.ripeRanges);
    }

    public boolean isInRipeRange(final Interval ipResource) {
        for (final Interval resource : ripeRanges) {
            if (resource.getClass().equals(ipResource.getClass()) && resource.contains(ipResource)) {
                return true;
            }
        }

        return false;
    }
}
