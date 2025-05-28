package net.ripe.db.whois.common.hazelcast;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Set;

@Component
@Profile({WhoisProfile.TEST})
public class TestIpBlockManager implements IpBlockManager {

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final Set<IpInterval> ipBlockedSet;

    public TestIpBlockManager(@Value("${ipranges.blocked.list:}") final String blockedListIps) {
        ipBlockedSet = Sets.newHashSet();

        ipBlockedSet.addAll(getBlockedIntervals(blockedListIps));
    }

    @Override
    public void addBlockedListAddress(String address) {
        ipBlockedSet.add(IpInterval.parse(address));
    }

    @Override
    public void removeBlockedListAddress(String address) {
        ipBlockedSet.remove(IpInterval.parse(address));
    }

    @Override
    public String getBlockedList() {
        StringBuilder result = new StringBuilder();
        COMMA_JOINER.appendTo(result, ipBlockedSet);

        return String.format("The blocked list contains next IPs %s", result);
    }

    @Override
    public Set<IpInterval> getIpBlockedSet() {
        return ipBlockedSet;
    }

    @Override
    public boolean isBlockedIp(final InetAddress candidate) {
        final IpInterval<?> parsed = IpInterval.asIpInterval(candidate);
        return getIpBlockedSet().stream()
                    .anyMatch(ipRange -> ipRange.getClass().equals(parsed.getClass()) && ipRange.contains(parsed));
    }
}
