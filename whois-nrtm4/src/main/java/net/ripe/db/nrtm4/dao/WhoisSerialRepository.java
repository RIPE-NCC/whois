package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.Timestamp;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Repository
@Transactional
public class WhoisSerialRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisSerialRepository.class);

    private final JdbcTemplate jdbcTemplate;

    WhoisSerialRepository(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void getSerialEntriesFromLast(final RowCallbackHandler rowCallbackHandler) {
        JdbcRpslObjectOperations.getSerialEntriesFromLast(jdbcTemplate, rowCallbackHandler);
    }

    public Map<RpslObjectKey, List<ObjectData>> findUpdates(final long fromTimestamp) {
        try {
            return jdbcTemplate.query(
                    "(SELECT pkey, object_type, timestamp, object_id, sequence_id FROM history WHERE timestamp > ?) " +
                        "UNION ALL " +
                        "(SELECT pkey, object_type, timestamp, object_id, sequence_id FROM last WHERE timestamp > ?)",
                    (rs, rowNum) -> {
                        try {
                            return new ObjectData(
                                rs.getString(1),                        // pkey
                                ObjectTypeIds.getType(rs.getInt(2)),    // objectType
                                rs.getLong(3),                          // timestamp
                                rs.getInt(4),                           // objectId
                                rs.getInt(5));                          // sequenceId
                        } catch (IllegalArgumentException e) {
                            // invalid object type
                            return null;
                        }
                    }, fromTimestamp, fromTimestamp)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ObjectData::getKey));
        } catch (DataAccessException e) {
            LOGGER.warn("Unable to retrieve object versions since {} due to {}", Timestamp.fromSeconds(fromTimestamp).toLocalDateTime(), e.getMessage());
            return Collections.emptyMap();
        }
    }

}
