package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * `id`          int unsigned NOT NULL AUTO_INCREMENT,
 * `version_id`  int unsigned NOT NULL,
 * `serial_id`   int          NOT NULL,
 * `object_type` int          NOT NULL,
 * `pkey`        varchar(256) NOT NULL,
 * `payload`     longtext     NOT NULL,
 */
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
        return new SnapshotObject(keyHolder.getKeyAs(Long.class), versionId, serialId, objectType, primaryKey, payload);
    }

    public Optional<SnapshotObject> getByObjectTypeAndPrimaryKey(
        final ObjectType type,
        final String primaryKey
    ) {
        final String sql = "" +
            "SELECT " +
            "id, version_id, serial_id, object_type, pkey, payload " +
            "FROM snapshot_object " +
            "WHERE object_type = ? " +
            "  AND pkey = ? ";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rn) ->
                new SnapshotObject(
                    rs.getLong(1),
                    rs.getLong(2),
                    rs.getInt(3),
                    ObjectTypeIds.getType(rs.getInt(4)),
                    rs.getString(5),
                    rs.getString(6)
                ), ObjectTypeIds.getId(type), primaryKey));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public void update(
        final long versionId,
        final int serialId,
        final String primaryKey,
        final String payload
    ) {
        final String sql = "" +
            "UPDATE snapshot_object " +
            "SET " +
            "version_id = ?, " +
            "serial_id = ?, " +
            "payload = ? " +
            "WHERE pkey = ?";
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setLong(1, versionId);
            pst.setInt(2, serialId);
            pst.setString(3, payload);
            pst.setString(4, primaryKey);
            return pst;
        }, keyHolder);
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

    public Stream<String> getSnapshotAsStream(final NrtmSource source) {
        final String sql = "" +
            "SELECT so.payload " +
            "FROM snapshot_object so " +
            "JOIN version v ON v.id = so.version_id " +
            "JOIN source src ON src.id = v.source_id " +
            "WHERE src.name = ? " +
            "ORDER BY so.serial_id";

        return jdbcTemplate.queryForStream(sql, (rs, rn) -> rs.getString(1), source.name());
    }

}
