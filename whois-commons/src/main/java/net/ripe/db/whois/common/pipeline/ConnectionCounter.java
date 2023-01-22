package net.ripe.db.whois.common.pipeline;

import jakarta.annotation.Nullable;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Count connections per IP address.
 */
public class ConnectionCounter {

    private final ConcurrentHashMap<InetAddress, Integer> connections = new ConcurrentHashMap<>();

    @Nullable
    public Integer increment(final InetAddress remoteAddress) {
        Integer count;
        do {
            count = connections.putIfAbsent(remoteAddress, 1);
        } while (count != null && !connections.replace(remoteAddress, count, count + 1));
        return count;
    }

    public void decrement(final InetAddress remoteAddress) {
        Integer count;
        for (; ; ) {
            count = connections.get(remoteAddress);
            if (count == null) {
                break;
            } else {
                if (count == 1) {
                    if (connections.remove(remoteAddress, 1)) {
                        break;
                    }
                } else if (connections.replace(remoteAddress, count, count - 1)) {
                    break;
                }
            }
        }
    }
}
