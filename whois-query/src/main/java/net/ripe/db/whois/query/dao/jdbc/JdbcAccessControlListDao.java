package net.ripe.db.whois.query.dao.jdbc;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateUtil;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.domain.IpResourceEntry;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcAccessControlListDao implements AccessControlListDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlListDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcAccessControlListDao(@Qualifier("aclDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void saveAclEvent(IpInterval<?> interval, final LocalDate date, final int limit, final BlockEvent.Type type) {
        String prefix = interval.toString();

        try {
            jdbcTemplate.update(
                    "INSERT INTO acl_event (prefix, event_time, daily_limit, event_type) VALUES (?, ?, ?, ?)",
                    prefix,
                    DateUtil.toDate(date),
                    limit,
                    type.name()
            );
        } catch (DataIntegrityViolationException e) {
            LOGGER.debug("Attempt to create temporary block twice: prefix {}, date {}", prefix, date);
        }
    }


    private static class BlockEventsExtractor implements ResultSetExtractor<List<BlockEvents>> {
        @Override
        public List<BlockEvents> extractData(final ResultSet rs) throws SQLException, DataAccessException {
            final Map<String, List<BlockEvent>> blockEventsMap = new HashMap<>();
            while (rs.next()) {
                final String prefix = rs.getString("prefix");
                final LocalDateTime time = Timestamp.from(rs.getTimestamp("event_time")).toLocalDateTime();
                final int limit = rs.getInt("daily_limit");
                final BlockEvent.Type type = BlockEvent.Type.valueOf(rs.getString("event_type"));

                List<BlockEvent> blockEvents = blockEventsMap.get(prefix);
                if (blockEvents == null) {
                    blockEvents = new ArrayList<>();
                    blockEventsMap.put(prefix, blockEvents);
                }
                blockEvents.add(new BlockEvent(time, limit, type));
            }

            final List<BlockEvents> result = new ArrayList<>(blockEventsMap.size());
            for (Map.Entry<String, List<BlockEvent>> blockEventEntry : blockEventsMap.entrySet()) {
                result.add(new BlockEvents(blockEventEntry.getKey(), blockEventEntry.getValue()));
            }

            return result;
        }
    }

    @Override
    public List<BlockEvents> getTemporaryBlocks(final LocalDate blockTime) {
        return jdbcTemplate.query(
                "SELECT prefix, event_time, daily_limit, event_type FROM acl_event WHERE event_time >= ? ORDER BY prefix, event_time ASC",
                new BlockEventsExtractor(),
                DateUtil.toDate(blockTime)
        );
    }

    @Override
    public void savePermanentBlock(final IpInterval<?> ipInterval, final LocalDate date, final int limit, final String comment) {
        String prefix = ipInterval.toString();

        saveAclEvent(ipInterval, date, limit, BlockEvent.Type.BLOCK_PERMANENTLY);

        jdbcTemplate.update(
                "INSERT INTO acl_denied (prefix, comment, denied_date) VALUES (?, ?, ?)",
                prefix, comment, DateUtil.toDate(date));
    }

    @Override
    public void removeBlockEventsBefore(final LocalDate date) {
        jdbcTemplate.update("DELETE FROM acl_event WHERE event_time < ?", DateUtil.toDate(date));
    }

    @Override
    public void removePermanentBlocksBefore(final LocalDate date) {
        jdbcTemplate.update("DELETE FROM acl_denied WHERE denied_date < ?", DateUtil.toDate(date));
    }

    @Override
    public List<IpResourceEntry<Boolean>> loadIpDenied() {
        return jdbcTemplate.query("SELECT prefix FROM acl_denied", new BooleanEntryMapper());
    }

    @Override
    public List<IpResourceEntry<Boolean>> loadIpProxy() {
        return jdbcTemplate.query("SELECT prefix FROM acl_proxy", new BooleanEntryMapper());
    }

    @Override
    public List<IpResourceEntry<Integer>> loadIpLimit() {
        return jdbcTemplate.query("SELECT prefix, daily_limit FROM acl_limit", new DailyLimitMapperResultSetExtractor());
    }

    @Override
    public List<IpResourceEntry<Boolean>> loadUnlimitedConnections() {
        return jdbcTemplate.query("SELECT prefix FROM acl_limit where unlimited_connections != 0", new BooleanEntryMapper());
    }

    private String getCanonicalPrefix(String prefix) {
        IpInterval<?> ipInterval = IpInterval.parse(prefix);
        return ipInterval.toString();
    }

    private static class BooleanEntryMapper implements RowMapper<IpResourceEntry<Boolean>> {
        @Override
        public IpResourceEntry<Boolean> mapRow(ResultSet rs, int rowNum) throws SQLException {
            String prefix = rs.getString(1);

            return new IpResourceEntry<>(IpInterval.parse(prefix), Boolean.TRUE);
        }
    }

    private static class DailyLimitMapper implements RowMapper<IpResourceEntry<Integer>> {
        @Override
        public IpResourceEntry<Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
            String prefix = rs.getString(1);
            int limit = rs.getInt(2);

            return new IpResourceEntry<>(IpInterval.parse(prefix), limit);
        }
    }

    private static class DailyLimitMapperResultSetExtractor implements ResultSetExtractor<List<IpResourceEntry<Integer>>> {
        private final DailyLimitMapper dailyLimitMapper = new DailyLimitMapper();

        @Override
        public List<IpResourceEntry<Integer>> extractData(final ResultSet rs) throws SQLException, DataAccessException {
            final List<IpResourceEntry<Integer>> result = Lists.newArrayList();

            int rowNum = 0;
            while (rs.next()) {
                try {
                    result.add(dailyLimitMapper.mapRow(rs, rowNum));
                } catch (RuntimeException e) {
                    LOGGER.error("Skipping limit because of error in row {}", rowNum, e);
                } finally {
                    rowNum++;
                }
            }

            return result;
        }
    }
}
