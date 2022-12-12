package net.ripe.db.nrtm4.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;


@Repository
public class DeltaFileRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<DeltaFile> rowMapper = (rs, rowNum) ->
        new DeltaFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getString(4),
            rs.getString(5),
            rs.getLong(6)
        );

    private final String deltaFileFields = "id, version_id, type, name, hash, payload, created ";

    @Autowired
    public DeltaFileRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public DeltaFile save(
        final long versionId,
        final String name,
        final String hash,
        final String payload
    ) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = System.currentTimeMillis();
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO delta_file (version_id, name, hash, payload, created) " +
                    "VALUES (?, ?, ?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, versionId);
                pst.setString(2, name);
                pst.setString(3, hash);
                pst.setString(4, payload);
                pst.setLong(5, now);
                return pst;
            }, keyHolder
        );
        return new DeltaFile(keyHolder.getKeyAs(Long.class), versionId, name, hash, payload, now);
    }
    public DeltaFile getByVersionId(final long id) {
        final String sql = "" +
            "SELECT " + deltaFileFields +
            "FROM delta_file " +
            "WHERE version_id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }
}
