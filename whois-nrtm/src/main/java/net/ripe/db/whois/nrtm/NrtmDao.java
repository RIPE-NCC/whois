package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NrtmDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public NrtmDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public SerialRange getSerials() {
        return jdbcTemplate.query("SELECT MIN(serial_id), MAX(serial_id) FROM serials", new ResultSetExtractor<SerialRange>() {
            @Override
            public SerialRange extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return new SerialRange(rs.getInt(1), rs.getInt(2));
            }
        });
    }

    @CheckForNull
    public SerialEntry getById(final int serialId) {
        try {
            return getSerialEntry(serialId);
        } catch (EmptyResultDataAccessException e) {
            LOGGER.warn("NrtmDao.getById({})", serialId, e);
            return null;
        }
    }

    public int getSerialAge(final int serialId) {
        try {
            final SerialEntry serialEntry = getSerialEntryWithoutBlobs(serialId);
            int effectiveTimestamp;

            if (serialEntry.getOperation() == Operation.DELETE || serialEntry.isAtLast()) {
                effectiveTimestamp = serialEntry.getLastTimestamp();
            } else {
                effectiveTimestamp = serialEntry.getHistoryTimestamp();
            }

            return (int) (System.currentTimeMillis() / 1000 - effectiveTimestamp);
        } catch (EmptyResultDataAccessException e) {
            return Integer.MAX_VALUE;
        }
    }

    // TODO: [AH] remove legacy references from this query once we deprecated legacy
    // TODO: [AH] fix legacy history to match rdp's approach
    // note: this is really kludgy, because legacy decreased serials.sequence_id by 1 on deletion, to make sure once
    //       could join from the deletion serials record directly to history.
    private SerialEntry getSerialEntry(final int serialId) {
        return jdbcTemplate.queryForObject("" +
                "SELECT serials.operation, serials.atlast, serials.object_id, last.timestamp, " +
                "COALESCE(legacy_history.timestamp, rdp_history.timestamp), " +
                "IF(LENGTH(last.object), last.object, COALESCE(legacy_history.object, rdp_history.object)) " +
                "FROM serials " +
                "LEFT JOIN last ON last.object_id = serials.object_id " +
                "LEFT JOIN history legacy_history ON legacy_history.object_id = serials.object_id AND legacy_history.sequence_id = serials.sequence_id " +
                "LEFT JOIN history rdp_history ON rdp_history.object_id = serials.object_id AND rdp_history.sequence_id = serials.sequence_id - 1 " +
                "WHERE serials.serial_id = ?", new RowMapper<SerialEntry>() {
            @Override
            public SerialEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
                    return new SerialEntry(Operation.getByCode(rs.getInt(1)), rs.getBoolean(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getBytes(6));
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Failed at serial_id " + serialId, e);
                }
            }
        }, serialId);
    }

    // exact same, but omit blob lookup for performance reasons
    private SerialEntry getSerialEntryWithoutBlobs(final int serialId) {
        return jdbcTemplate.queryForObject("" +
                "SELECT serials.operation, serials.atlast, serials.object_id, last.timestamp, " +
                "COALESCE(legacy_history.timestamp, rdp_history.timestamp) " +
                "FROM serials " +
                "LEFT JOIN last ON last.object_id = serials.object_id " +
                "LEFT JOIN history legacy_history ON legacy_history.object_id = serials.object_id AND legacy_history.sequence_id = serials.sequence_id " +
                "LEFT JOIN history rdp_history ON rdp_history.object_id = serials.object_id AND rdp_history.sequence_id = serials.sequence_id - 1 " +
                "WHERE serials.serial_id = ?", new RowMapper<SerialEntry>() {
            @Override
            public SerialEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new SerialEntry(Operation.getByCode(rs.getInt(1)), rs.getBoolean(2), rs.getInt(3), rs.getInt(4), rs.getInt(5));
            }
        }, serialId);
    }

    public static class SerialEntry {

        final private Operation operation;
        final private boolean atLast;
        final private int objectId;

        final private int lastTimestamp;
        final private int historyTimestamp;

        private RpslObject rpslObject;

        public SerialEntry(Operation operation, boolean atLast, int objectId, int lastTimestamp, int historyTimestamp) {
            this.operation = operation;
            this.atLast = atLast;
            this.objectId = objectId;
            this.lastTimestamp = lastTimestamp;
            this.historyTimestamp = historyTimestamp;
            rpslObject = null;
        }

        public SerialEntry(Operation operation, boolean atLast, int objectId, int lastTimestamp, int historyTimestamp, byte[] blob) {
            this(operation, atLast, objectId, lastTimestamp, historyTimestamp);
            rpslObject = RpslObject.parse(objectId, blob);
        }

        public RpslObject getRpslObject() {
            return rpslObject;
        }

        public Operation getOperation() {
            return operation;
        }

        public boolean isAtLast() {
            return atLast;
        }

        public int getLastTimestamp() {
            return lastTimestamp;
        }

        public int getHistoryTimestamp() {
            return historyTimestamp;
        }
    }

    public static class SerialRange {
        private final int begin;
        private final int end;

        public SerialRange(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        public int getBegin() {
            return begin;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return String.format("%d-%d", begin, end);
        }
    }
}
