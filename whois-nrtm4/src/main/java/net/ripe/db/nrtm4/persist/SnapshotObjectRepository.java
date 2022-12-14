package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.stream.Stream;


@Repository
public class SnapshotObjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnapshotObjectRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public SnapshotObject insert(
        final long versionId,
        final int serialId,
        final ObjectType objectType,
        final String primaryKey,
        final String payload
    ) {
        final String sql = "" +
            "INSERT INTO snapshot_object (version_id, serial_id, object_type, pkey, payload) " +
            "VALUES (?, ?, ?, ?, ?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setLong(1, versionId);
            pst.setInt(2, serialId);
            pst.setInt(3, ObjectTypeIds.getId(objectType));
            pst.setString(4, primaryKey);
            pst.setString(5, payload);
            return pst;
        }, keyHolder);
        return new SnapshotObject(keyHolder.getKeyAs(Long.class), serialId, objectType, payload);
    }

    public void delete(final ObjectType objectType, final String primaryKey) {
        final String sql = "" +
            "DELETE FROM snapshot_object " +
            "WHERE object_type = ? " +
            "  AND pkey = ?";
        jdbcTemplate.update(connection -> {
            final PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, ObjectTypeIds.getId(objectType));
            pst.setString(2, primaryKey);
            return pst;
        });
    }

    public Stream<String> streamSnapshots(final NrtmSource source) {
        final String sql = "" +
            "SELECT payload " +
            "FROM snapshot_object " +
            "JOIN version ON version.id = snapshot_object.version_id " +
            "JOIN source ON source.id = version.source_id " +
            "WHERE source.name = ? " +
            "ORDER BY snapshot_object.serial_id";

        final Stream.Builder<String> builder = Stream.builder();

        jdbcTemplate.query(sql, rs -> {
            builder.add(rs.getString(1));
        }, source.name());
        return builder.build();
    }

}
