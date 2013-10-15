package net.ripe.db.whois.scheduler.task.export.dao;

import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
class JdbcExportDao implements ExportDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcExportDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcExportDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int getMaxSerial() {
        return jdbcTemplate.queryForInt("SELECT max(serial_id) FROM serials");
    }

    @Override
    public void exportObjects(final ExportCallbackHandler exportCallbackHandler) {
        JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE sequence_id != 0 ",
                new ExportRowCallbackHandler(exportCallbackHandler));
    }

    private static final class ExportRowCallbackHandler implements RowCallbackHandler {
        private final ExportCallbackHandler exportCallbackHandler;

        private ExportRowCallbackHandler(final ExportCallbackHandler exportCallbackHandler) {
            this.exportCallbackHandler = exportCallbackHandler;
        }

        @Override
        public void processRow(final ResultSet rs) throws SQLException {
            final int objectId = rs.getInt(1);
            RpslObject object = null;
            try {
                object = RpslObject.parse(objectId, rs.getBytes(2));
            } catch (RuntimeException e) {
                LOGGER.warn("Unable to parse RPSL object with object_id: {}", objectId);
            }

            if (object != null) {
                exportCallbackHandler.exportObject(object);
            }
        }
    }
}
