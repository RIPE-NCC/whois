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

    private Set<Interval> trusted;

    @Value("${ipranges.trusted}")
    public void setTrusted(final String... trusted) {
        final Set<Interval> ipResources = Sets.newLinkedHashSetWithExpectedSize(trusted.length);
        for (final String trustedRange : trusted) {
            ipResources.add(IpInterval.parse(trustedRange));
        }

        this.trusted = ipResources;
        LOGGER.info("Trusted ranges: {}", this.trusted);
    }

    public boolean isTrusted(final Interval ipResource) {
        for (final Interval resource : trusted) {
            if (resource.getClass().equals(ipResource.getClass()) && resource.contains(ipResource)) {
                return true;
            }
        }

        return false;
    }
}
