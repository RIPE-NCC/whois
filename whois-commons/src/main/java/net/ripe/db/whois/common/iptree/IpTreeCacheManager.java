package net.ripe.db.whois.common.iptree;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.etree.IntersectingIntervalException;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.etree.MultiValueIntervalMap;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.etree.SynchronizedIntervalMap;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static net.ripe.db.whois.common.domain.serials.Operation.UPDATE;
import static net.ripe.db.whois.common.domain.serials.Operation.getByCode;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
public class IpTreeCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpTreeCacheManager.class);

    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;

    @Autowired
    public IpTreeCacheManager(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }

    private static final class IpTreeUpdate {
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

    private static final class CacheEntry {
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

        void update(final Iterable<IpTreeUpdate> updates, final long lastSerial, final CacheEntry cacheEntry) {
            for (final IpTreeUpdate ipTreeUpdate : updates) {
                try {
                    update(ipTreeUpdate);
                } catch (IntersectingIntervalException e) {
                    LOGGER.info("Skipping intersecting entry in {}: {}", cacheEntry.sourceConfiguration.getSource(), e.getMessage());
                } catch (RuntimeException e) {
                    LOGGER.info("Unable to update object {}: {}", ipTreeUpdate, e.getMessage());
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
                            LOGGER.debug("Ignoring domain: {}", domain.getValue());
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
                    intervalMap.put(ipEntry.getKey(), ipEntry);
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
        update(sourceConfiguration, sourceConfiguration.getJdbcTemplate());
    }

    public void updateTransactional(final SourceConfiguration sourceConfiguration) {
        update(sourceConfiguration, this.jdbcTemplate);
    }

    private void update(final SourceConfiguration sourceConfiguration, final JdbcTemplate jdbcTemplate) {
        final CIString source = sourceConfiguration.getSource().getName();
        final CacheEntry cacheEntry = cache.get(source);
        if (cacheEntry == null) {
            throw new IllegalArgumentException(String.format("No cached ipTree for source: %s", source));
        }

        // don't wait here if other thread is already busy updating the tree
        if (cacheEntry.updateLock.tryAcquire()) {
            try {
                update(jdbcTemplate, cacheEntry);
            } catch (DataAccessException e) {
                LOGGER.warn("Unable to update {} due to {}", sourceConfiguration, e.getMessage());
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
            if( cacheEntry.sourceConfiguration.getSource().isTest()) {
                LOGGER.info("Database went away; serial in trees: {}; serial in DB: {}", fromExclusive, toInclusive);
                // For the test source, we reload the database every night, so in this case we do need a full rebuild of the ipTree.
                rebuild(jdbcTemplate, cacheEntry);
            } else {
                LOGGER.debug("IpTree is ahead of local database; serial in trees: {}; serial in DB: {}", fromExclusive, toInclusive);
                //
                // Situation typically appears when:
                // - a batch-update (=multiple updates in single transaction) is performed and
                // - the "IpTreeUpdater"-scheduled-task kicks in on that node before replication from master to local database has completed.
                //
                // In this particular situation the in-memory tree is ahead of the local database.
                // Regular replication events will eventually solve this situation.
                // We do not consider this situation an error: So there is no nned to rebuild the ipTree.
                //        Especially since the full-tree-rebuild takes long and makes consequent updates fail.
                //
            }
        } else {
            LOGGER.debug("Local database is ahead of IpTree; serial in trees: {}; serial in DB: {}", fromExclusive, toInclusive);

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
                                    getByCode(rs.getInt(4))
                            );
                        }
                    },
                    fromExclusive, toInclusive,
                    ObjectTypeIds.getId(INETNUM),
                    ObjectTypeIds.getId(INET6NUM),
                    ObjectTypeIds.getId(ObjectType.ROUTE),
                    ObjectTypeIds.getId(ROUTE6),
                    ObjectTypeIds.getId(DOMAIN)
            );

            cacheEntry.nestedIntervalMaps.update(ipTreeUpdates, toInclusive, cacheEntry);
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

        final List<IpTreeUpdate> ipTreeUpdates = Lists.newArrayList();
        ipTreeUpdates.addAll(jdbcTemplate.query("" +
                        "SELECT begin_in, end_in, object_id " +
                        "FROM inetnum ",
                new RowMapper<IpTreeUpdate>() {
                    @Override
                    public IpTreeUpdate mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new IpTreeUpdate(INETNUM,
                                new Ipv4Entry(new Ipv4Resource(rs.getLong(1), rs.getLong(2)), rs.getInt(3)).getKey().toRangeString(),
                                rs.getInt(3),
                                UPDATE);
                    }
                }
        ));

        ipTreeUpdates.addAll(jdbcTemplate.query("" +
                        "SELECT i6_msb, i6_lsb, prefix_length, object_id " +
                        "FROM inet6num ",
                new RowMapper<IpTreeUpdate>() {
                    @Override
                    public IpTreeUpdate mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new IpTreeUpdate(INET6NUM,
                                new Ipv6Entry(Ipv6Resource.parseFromStrings(rs.getString(1), rs.getString(2), rs.getInt(3)), rs.getInt(4)).getKey().toString(),
                                rs.getInt(4),
                                UPDATE);
                    }
                }
        ));

        ipTreeUpdates.addAll(jdbcTemplate.query("" +
                        "SELECT prefix, prefix_length, origin, object_id " +
                        "FROM route ",
                new RowMapper<IpTreeUpdate>() {
                    @Override
                    public IpTreeUpdate mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new IpTreeUpdate(ROUTE,
                                Ipv4Resource.parsePrefixWithLength(rs.getLong(1), rs.getInt(2)).toString() + rs.getString(3),
                                rs.getInt(4),
                                UPDATE);
                    }
                }
        ));

        ipTreeUpdates.addAll(jdbcTemplate.query("" +
                        "SELECT r6_msb, r6_lsb, prefix_length, object_id, origin " +
                        "FROM route6 ",
                new RowMapper<IpTreeUpdate>() {
                    @Override
                    public IpTreeUpdate mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new IpTreeUpdate(ROUTE6,
                                new Ipv6Entry(Ipv6Resource.parseFromStrings(rs.getString(1), rs.getString(2), rs.getInt(3)), rs.getInt(4)).getKey() + rs.getString(5),
                                rs.getInt(4),
                                UPDATE);
                    }
                }
        ));

        ipTreeUpdates.addAll(jdbcTemplate.query("" +
                        "SELECT domain, object_id " +
                        "FROM domain ",
                new RowMapper<IpTreeUpdate>() {
                    @Override
                    public IpTreeUpdate mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new IpTreeUpdate(DOMAIN,
                                rs.getString(1),
                                rs.getInt(2),
                                UPDATE);
                    }
                }
        ));

        nestedIntervalMaps.update(ipTreeUpdates, toInclusive, cacheEntry);

        cacheEntry.nestedIntervalMaps = nestedIntervalMaps;
    }

    private long getLastSerial(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("SELECT IFNULL(MAX(serial_id),0) FROM serials", Long.class);
    }
}
