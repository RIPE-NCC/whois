package net.ripe.db.whois.common.hazelcast;

import net.ripe.db.whois.common.ip.IpInterval;

import java.util.Set;

public interface HazelcastBlockedIps {

    void addBlockedListAddress(final String address);

    void removeBlockedListAddress(String address);

    String getBlockedList();

    Set<IpInterval> getIpBlockedSet();

}
