package net.ripe.db.whois.common.hazelcast;

import net.ripe.db.whois.common.ip.IpInterval;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface HazelcastBlockedIps {

    void addBlockedListAddress(final String address);

    void removeBlockedListAddress(String address);

    String getBlockedList();

    Set<IpInterval> getIpBlockedSet();

    default List<? extends IpInterval<?>> getBlockedIntervals(String blockedListIps) {
        return Stream.of(blockedListIps.split(",")).map(IpInterval::parse).toList();
    }
}
