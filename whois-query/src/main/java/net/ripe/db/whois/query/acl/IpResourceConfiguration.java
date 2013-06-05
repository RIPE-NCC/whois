package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.IpResourceEntry;
import net.ripe.db.whois.common.domain.IpResourceTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.List;

@Component
public class IpResourceConfiguration {
    private static final int TREE_UPDATE_IN_SECONDS = 120;

    private static final int DEFAULT_LIMIT = 5000;

    private final Loader loader;

    private IpResourceTree<Boolean> denied;
    private IpResourceTree<Boolean> proxy;
    private IpResourceTree<Integer> limit;
    private IpResourceTree<Boolean> unlimitedConnections;

    @Autowired
    public IpResourceConfiguration(final Loader loader) {
        this.loader = loader;
    }

    public boolean isDenied(final InetAddress address) {
        final Boolean result = denied.getValue(IpInterval.asIpInterval(address));
        return result != null && result;
    }

    public boolean isProxy(final InetAddress address) {
        final Boolean result = proxy.getValue(IpInterval.asIpInterval(address));
        return result != null && result;
    }

    public int getLimit(final InetAddress address) {
        final Integer result = limit.getValue(IpInterval.asIpInterval(address));
        return result == null ? DEFAULT_LIMIT : result;
    }

    public boolean isUnlimitedConnections(final InetAddress address) {
        final Boolean result = unlimitedConnections.getValue(IpInterval.asIpInterval(address));
        return result != null && result;
    }

    @PostConstruct
    @Scheduled(fixedDelay = TREE_UPDATE_IN_SECONDS * 1000)
    public synchronized void reload() {
        denied = refreshEntries(loader.loadIpDenied());
        proxy = refreshEntries(loader.loadIpProxy());
        limit = refreshEntries(loader.loadIpLimit());
        unlimitedConnections = refreshEntries(loader.loadUnlimitedConnections());
    }

    private <V> IpResourceTree<V> refreshEntries(final List<IpResourceEntry<V>> entries) {
        final IpResourceTree<V> temp = new IpResourceTree<>();

        for (final IpResourceEntry<V> entry : entries) {
            temp.add(entry.getIpInterval(), entry.getValue());
        }

        return temp;
    }

    /**
     * Implement the Loader interface to load the values into the IpResourceConfiguration.
     */
    public interface Loader {
        /**
         * @return All IP denied entries.
         */
        List<IpResourceEntry<Boolean>> loadIpDenied();

        /**
         * @return All IP proxy entries.
         */
        List<IpResourceEntry<Boolean>> loadIpProxy();

        /**
         * @return All IP limit entries.
         */
        List<IpResourceEntry<Integer>> loadIpLimit();

        /**
         * @return All IP unlimited connections.
         */
        List<IpResourceEntry<Boolean>> loadUnlimitedConnections();
    }
}
