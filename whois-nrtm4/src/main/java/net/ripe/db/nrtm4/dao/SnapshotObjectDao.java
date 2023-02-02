package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.function.Consumer;


@Repository
public class SnapshotObjectDao {

    private final JdbcTemplate jdbcTemplate;

    public SnapshotObjectDao(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void consumeAllObjects(final NrtmSource source, final Consumer<String> fn) {
        final String sql = """
            SELECT so.rpsl
            FROM snapshot_object so
            JOIN source s ON s.id = so.source_id
            WHERE s.name = ?
            ORDER BY so.object_id
            """;
        JdbcStreamingHelper.executeStreaming(
            jdbcTemplate,
            sql,
            pss -> pss.setString(1, source.name()),
            rs -> {
                fn.accept(rs.getString(1));
            });
    }

}
