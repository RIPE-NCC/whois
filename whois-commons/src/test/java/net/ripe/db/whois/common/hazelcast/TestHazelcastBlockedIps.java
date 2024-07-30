package net.ripe.db.whois.common.hazelcast;

import com.google.common.base.Joiner;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.eclipse.jetty.util.StringUtil;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile({WhoisProfile.TEST})
public class TestHazelcastBlockedIps implements HazelcastBlockedIps {

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final Set<IpInterval> ipBlockedSet;

    public TestHazelcastBlockedIps(@Value("${ipranges.blocked.list:}") final String blockedListIps) {
        ipBlockedSet = Sets.newSet();

        for (final String address : StringUtil.csvSplit(blockedListIps)) {
            addBlockedListAddress(address);
        }
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
}
