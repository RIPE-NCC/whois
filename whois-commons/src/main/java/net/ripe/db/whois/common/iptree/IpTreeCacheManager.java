package net.ripe.db.whois.common.iptree;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.Domain;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.etree.*;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Component
public class IpTreeCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpTreeCacheManager.class);

    private final SourceContext sourceContext;

    @Autowired
    public IpTreeCacheManager(final SourceContext sourceContext) {
        this.sourceContext = sourceContext;
    }

    private static class IpTreeUpdate {
        private final ObjectType objectType;
        private final String pkey;
        private final int objectId;
        private final Operation operation;

        private IpTreeUpdate(final ObjectType objectType, final String pkey, final int objectId, final Operation operation) {
            this.objectType = objectType;
            this.pkey = pkey;
            this.objectId = objectId;
            this.operation = operation;
        }

        @Override
        public String toString() {
            return String.format("(objectType=%s, pkey=%s)", objectType, pkey);
        }
    }

    private static class CacheEntry {
        final SourceConfiguration sourceConfiguration;
        final Semaphore updateLock = new Semaphore(1);
        NestedIntervalMaps nestedIntervalMaps = new NestedIntervalMaps();

        private CacheEntry(final SourceConfiguration sourceConfiguration) {
            this.sourceConfiguration = sourceConfiguration;
        }
    }

    static class NestedIntervalMaps {
        final IntervalMap<Ipv4Resource, Ipv4Entry> ipv4TreeCache = SynchronizedIntervalMap.synchronizedMap(new NestedIntervalMap<Ipv4Resource, Ipv4Entry>());
        final IntervalMap<Ipv6Resource, Ipv6Entry> ipv6TreeCache = SynchronizedIntervalMap.synchronizedMap(new NestedIntervalMap<Ipv6Resource, Ipv6Entry>());
        final IntervalMap<Ipv4Resource, Ipv4RouteEntry> ipv4RouteTreeCache = SynchronizedIntervalMap.synchronizedMap(new MultiValueIntervalMap<Ipv4Resource, Ipv4RouteEntry>());
        final IntervalMap<Ipv6Resource, Ipv6RouteEntry> ipv6RouteTreeCache = SynchronizedIntervalMap.synchronizedMap(new MultiValueIntervalMap<Ipv6Resource, Ipv6RouteEntry>());
        final IntervalMap<Ipv4Resource, Ipv4Entry> ipv4DomainTreeCache = SynchronizedIntervalMap.synchronizedMap(new NestedIntervalMap<Ipv4Resource, Ipv4Entry>());
        final IntervalMap<Ipv6Resource, Ipv6Entry> ipv6DomainTreeCache = SynchronizedIntervalMap.synchronizedMap(new NestedIntervalMap<Ipv6Resource, Ipv6Entry>());

        volatile long lastSerial = Long.MIN_VALUE;

        void update(final Iterable<IpTreeUpdate> updates, final long lastSerial) {
            for (final IpTreeUpdate ipTreeUpdate : updates) {
                try {
                    update(ipTreeUpdate);
                } catch (RuntimeException e) {
                    LOGGER.warn("Unable to update object {}: {}", ipTreeUpdate, e.getMessage());
                }
            }

            this.lastSerial = lastSerial;
        }

        private void update(final IpTreeUpdate ipTreeUpdate) {
            switch (ipTreeUpdate.objectType) {
                case INETNUM:
                    update(ipv4TreeCache, new Ipv4Entry(Ipv4Resource.parse(ipTreeUpdate.pkey), ipTreeUpdate.objectId), ipTreeUpdate.operation);
                    break;
                case INET6NUM:
                    update(ipv6TreeCache, new Ipv6Entry(Ipv6Resource.parse(ipTreeUpdate.pkey), ipTreeUpdate.objectId), ipTreeUpdate.operation);
                    break;
                case ROUTE:
                    update(ipv4RouteTreeCache, Ipv4RouteEntry.parse(ipTreeUpdate.pkey, ipTreeUpdate.objectId), ipTreeUpdate.operation);
                    break;
                case ROUTE6:
                    update(ipv6RouteTreeCache, Ipv6RouteEntry.parse(ipTreeUpdate.pkey, ipTreeUpdate.objectId), ipTreeUpdate.operation);
                    break;
                case DOMAIN:
                    final Domain domain = Domain.parse(ipTreeUpdate.pkey);
                    switch (domain.getType()) {
                        case INADDR:
                            update(ipv4DomainTreeCache, new Ipv4Entry((Ipv4Resource) domain.getReverseIp(), ipTreeUpdate.objectId), ipTreeUpdate.operation);
                            break;
                        case IP6:
                            update(ipv6DomainTreeCache, new Ipv6Entry((Ipv6Resource) domain.getReverseIp(), ipTreeUpdate.objectId), ipTreeUpdate.operation);
                            break;
                        default:
                            LOGGER.debug("Ignoring domain: {}", domain);
                            break;
                    }

                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unexpected object type: %s", ipTreeUpdate.objectType));
            }
        }

        <K extends Interval<K>, V extends IpEntry<K>> void update(final IntervalMap<K, V> intervalMap, final V ipEntry, final Operation operation) {
            switch (operation) {
                case UPDATE:
                    try {
                        intervalMap.put(ipEntry.getKey(), ipEntry);
                    } catch (IntersectingIntervalException e) {
                        LOGGER.warn("Skipping intersecting entry {}, should be cleaned up in database", ipEntry);
                    }

                    break;
                case DELETE:
                    intervalMap.remove(ipEntry.getKey(), ipEntry);
            }
        }

        IntervalMap<Ipv4Resource, Ipv4Entry> getIpv4TreeCache() {
            return ipv4TreeCache;
        }

        IntervalMap<Ipv6Resource, Ipv6Entry> getIpv6TreeCache() {
            return ipv6TreeCache;
        }

        IntervalMap<Ipv4Resource, Ipv4RouteEntry> getIpv4RouteTreeCache() {
            return ipv4RouteTreeCache;
        }

        IntervalMap<Ipv6Resource, Ipv6RouteEntry> getIpv6RouteTreeCache() {
            return ipv6RouteTreeCache;
        }

        IntervalMap<Ipv4Resource, Ipv4Entry> getIpv4DomainTreeCache() {
            return ipv4DomainTreeCache;
        }

        IntervalMap<Ipv6Resource, Ipv6Entry> getIpv6DomainTreeCache() {
            return ipv6DomainTreeCache;
        }
    }

    private final Map<CIString, CacheEntry> cache = Maps.newHashMap();

    void rebuild(final SourceConfiguration sourceConfiguration) {
        final CIString source = sourceConfiguration.getSource().getName();
        final CacheEntry existingEntry = cache.get(source);
        if (existingEntry != null && !sourceConfiguration.equals(existingEntry.sourceConfiguration)) {
            throw new IllegalArgumentException(String.format("Cannot rebuild %s using different source configuration: %s", existingEntry.sourceConfiguration, sourceConfiguration));
        }

        final CacheEntry cacheEntry = new CacheEntry(sourceConfiguration);
        rebuild(sourceConfiguration.getJdbcTemplate(), cacheEntry);
        cache.put(source, cacheEntry);
    }

    public void update(final SourceConfiguration sourceConfiguration) {
        final CIString source = sourceConfiguration.getSource().getName();
        final CacheEntry cacheEntry = cache.get(source);
        if (cacheEntry == null) {
            throw new IllegalArgumentException(String.format("No cached ipTree for source: %s", source));
        }

        // don't wait here if other thread is already busy updating the tree
        if (cacheEntry.updateLock.tryAcquire()) {
            try {
                update(sourceConfiguration.getJdbcTemplate(), cacheEntry);
            } finally {
                cacheEntry.updateLock.release();
            }
        }
    }

    private void update(final JdbcTemplate jdbcTemplate, final CacheEntry cacheEntry) {
        final long fromExclusive = cacheEntry.nestedIntervalMaps.lastSerial;
        final long toInclusive = getLastSerial(jdbcTemplate);

        if (fromExclusive == toInclusive) {
            LOGGER.debug("No update of IpTree needed (serial {} unchanged)", fromExclusive);
        } else if (fromExclusive > toInclusive) {
            LOGGER.warn("Database went away; serial in trees: {}; serial in DB: {}", fromExclusive, toInclusive);
            rebuild(jdbcTemplate, cacheEntry);
        } else {
            final List<IpTreeUpdate> ipTreeUpdates = jdbcTemplate.query("" +
                    "SELECT last.object_type, last.pkey, last.object_id, serials.operation " +
                    "FROM serials " +
                    "LEFT JOIN last ON last.object_id = serials.object_id " +
                    "WHERE serials.serial_id > ? " +
                    "AND serials.serial_id <= ? " +
                    "AND last.object_type in (?, ?, ?, ?, ?) " +
                    "AND ((serials.operation = 1 AND serials.sequence_id = 1) OR serials.operation = 2) " +
                    "ORDER BY serials.serial_id ASC",
                    new RowMapper<IpTreeUpdate>() {
                        @Override
                        public IpTreeUpdate mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                            return new IpTreeUpdate(
                                    ObjectTypeIds.getType(rs.getInt(1)),
                                    rs.getString(2),
                                    rs.getInt(3),
                                    Operation.getByCode(rs.getInt(4))
                            );
                        }
                    },
                    fromExclusive, toInclusive,
                    ObjectTypeIds.getId(ObjectType.INETNUM),
                    ObjectTypeIds.getId(ObjectType.INET6NUM),
                    ObjectTypeIds.getId(ObjectType.ROUTE),
                    ObjectTypeIds.getId(ObjectType.ROUTE6),
                    ObjectTypeIds.getId(ObjectType.DOMAIN));

            cacheEntry.nestedIntervalMaps.update(ipTreeUpdates, toInclusive);
        }
    }

    Map<SourceConfiguration, Long> getLastSerials() {
        final Map<SourceConfiguration, Long> lastSerials = Maps.newHashMap();

        for (final CacheEntry cacheEntry : cache.values()) {
            cacheEntry.updateLock.acquireUninterruptibly();
            try {
                lastSerials.put(cacheEntry.sourceConfiguration, cacheEntry.nestedIntervalMaps.lastSerial);
            } finally {
                cacheEntry.updateLock.release();
            }
        }

        return lastSerials;
    }

    NestedIntervalMaps get(final CIString source) {
        final CIString alias = sourceContext.getAlias(source);
        final CIString actualSource = alias != null ? alias : source;

        final CacheEntry cacheEntry = cache.get(actualSource);
        if (cacheEntry == null) {
            throw new IllegalArgumentException(String.format("No cached ipTree for source: %s", actualSource));
        }

        return cacheEntry.nestedIntervalMaps;
    }

    private void rebuild(final JdbcTemplate jdbcTemplate, final CacheEntry cacheEntry) {
        final NestedIntervalMaps nestedIntervalMaps = new NestedIntervalMaps();

        final long toInclusive = getLastSerial(jdbcTemplate);

        final List<IpTreeUpdate> ipTreeUpdates = jdbcTemplate.query("" +
                "SELECT object_type, pkey, object_id " +
                "FROM last " +
                "WHERE object_type in (?, ?, ?, ?, ?) " +
                "AND sequence_id != 0 ",
                new RowMapper<IpTreeUpdate>() {
                    @Override
                    public IpTreeUpdate mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new IpTreeUpdate(
                                ObjectTypeIds.getType(rs.getInt(1)),
                                rs.getString(2),
                                rs.getInt(3),
                                Operation.UPDATE
                        );
                    }
                },
                ObjectTypeIds.getId(ObjectType.INETNUM),
                ObjectTypeIds.getId(ObjectType.INET6NUM),
                ObjectTypeIds.getId(ObjectType.ROUTE),
                ObjectTypeIds.getId(ObjectType.ROUTE6),
                ObjectTypeIds.getId(ObjectType.DOMAIN));

        nestedIntervalMaps.update(ipTreeUpdates, toInclusive);

        cacheEntry.nestedIntervalMaps = nestedIntervalMaps;
    }

    private int getLastSerial(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForInt("SELECT MAX(serial_id) FROM serials");
    }
}