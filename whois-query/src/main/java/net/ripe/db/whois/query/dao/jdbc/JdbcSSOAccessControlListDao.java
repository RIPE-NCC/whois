package net.ripe.db.whois.query.dao.jdbc;

import net.ripe.db.whois.common.DateUtil;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
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
public class JdbcSSOAccessControlListDao implements SSOAccessControlListDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpAccessControlListDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcSSOAccessControlListDao(@Qualifier("aclDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void saveAclEvent(String ssoId, final LocalDate date, final int limit, final BlockEvent.Type type) {

        try {
            jdbcTemplate.update(
                    "INSERT INTO acl_sso_event (sso_id, event_time, daily_limit, event_type) VALUES (?, ?, ?, ?)",
                    ssoId,
                    DateUtil.toDate(date),
                    limit,
                    type.name()
            );
        } catch (DataIntegrityViolationException e) {
            LOGGER.debug("Attempt to create temporary block twice: sso_id {}, date {}", ssoId, date);
        }
    }


    private static class BlockEventsExtractor implements ResultSetExtractor<List<BlockEvents>> {
        @Override
        public List<BlockEvents> extractData(final ResultSet rs) throws SQLException, DataAccessException {
            final Map<String, List<BlockEvent>> blockEventsMap = new HashMap<>();
            while (rs.next()) {
                final String ssoId = rs.getString("sso_id");
                final LocalDateTime time = Timestamp.from(rs.getTimestamp("event_time")).toLocalDateTime();
                final int limit = rs.getInt("daily_limit");
                final BlockEvent.Type type = BlockEvent.Type.valueOf(rs.getString("event_type"));

                List<BlockEvent> blockEvents = blockEventsMap.get(ssoId);
                if (blockEvents == null) {
                    blockEvents = new ArrayList<>();
                    blockEventsMap.put(ssoId, blockEvents);
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
                "SELECT sso_id, event_time, daily_limit, event_type FROM acl_sso_event WHERE event_time >= ? ORDER BY sso_id, event_time ASC",
                new BlockEventsExtractor(),
                DateUtil.toDate(blockTime)
        );
    }

    @Override
    public void savePermanentBlock(final String ssoId, final LocalDate date, final int limit, final String comment) {

        saveAclEvent(ssoId, date, limit, BlockEvent.Type.BLOCK_PERMANENTLY);

        jdbcTemplate.update(
                "INSERT INTO acl_sso_denied (sso_id, comment, denied_date) VALUES (?, ?, ?)",
                ssoId, comment, DateUtil.toDate(date));
    }

    @Override
    public void removeBlockEventsBefore(final LocalDate date) {
        jdbcTemplate.update("DELETE FROM acl_sso_event WHERE event_time < ?", DateUtil.toDate(date));
    }

    @Override
    public void removePermanentBlocksBefore(final LocalDate date) {
        jdbcTemplate.update("DELETE FROM acl_sso_denied WHERE denied_date < ?", DateUtil.toDate(date));
    }

    @Override
    public List<String> loadSSODenied() {
        return jdbcTemplate.queryForList("SELECT sso_id FROM acl_sso_denied", String.class);
    }
}
