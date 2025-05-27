package net.ripe.db.whois.common.hazelcast;

import com.google.common.net.InetAddresses;
import io.netty.util.internal.StringUtil;
import net.ripe.db.whois.common.ip.IpInterval;
import org.apache.commons.compress.utils.Lists;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface IpBlockManager {

    void addBlockedListAddress(final String address);

    void removeBlockedListAddress(String address);

    String getBlockedList();

    Set<IpInterval> getIpBlockedSet();

    default List<? extends IpInterval<?>> getBlockedIntervals(String blockedListIps) {
        if (StringUtil.isNullOrEmpty(blockedListIps)){
            return Lists.newArrayList();
        }
        return Stream.of(blockedListIps.split(",")).map(IpInterval::parse).toList();
    }

    boolean isBlockedIp(final InetAddress candidate);

    default boolean isBlockedIp(final String candidate) {
        return isBlockedIp(InetAddresses.forString(candidate));
    }
}
