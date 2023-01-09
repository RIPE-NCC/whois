package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.function.Consumer;


/**
 * `id`          int unsigned NOT NULL AUTO_INCREMENT,
 * `version_id`  int unsigned NOT NULL,
 * `serial_id`   int          NOT NULL,
 * `object_type` int          NOT NULL,
 * `pkey`        varchar(256) NOT NULL,
 * `payload`     longtext     NOT NULL,
 */
@Repository
public class SnapshotObjectIteratorRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnapshotObjectIteratorRepository(@Qualifier("nrtmSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void snapshotCallbackFn(final NrtmSource source, final Consumer<String> fn) {
        final String sql = "" +
            "SELECT so.rpsl " +
            "FROM snapshot_object so " +
            "JOIN version_info v ON v.id = so.version_id " +
            "JOIN source src ON src.id = v.source_id " +
            "WHERE src.name = ? " +
            "ORDER BY so.object_id";
        JdbcStreamingHelper.executeStreaming(
            jdbcTemplate,
            sql,
            pss -> pss.setString(1, source.name()),
            rs -> {
                fn.accept(rs.getString(1));
            });
    }

    public void snapshotCallback(final NrtmSource source, final RowCallbackHandler rowCallbackHandler) {
        final String sql = "" +
            "SELECT so.rpsl " +
            "FROM snapshot_object so " +
            "JOIN version_info v ON v.id = so.version_id " +
            "JOIN source src ON src.id = v.source_id " +
            "WHERE src.name = ? " +
            "ORDER BY so.object_id";
        JdbcStreamingHelper.executeStreaming(
            jdbcTemplate,
            sql,
            pss -> pss.setString(1, source.name()),
            rowCallbackHandler);
    }

}
