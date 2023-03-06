package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.DeltaFile;
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
import java.util.Optional;


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

    public Optional<DeltaFile> getByName(final String sessionId, final String name) {
        final String sql = "" +
            "SELECT df.id, df.version_id, df.type, df.name, df.hash, df.payload, df.created " +
            "FROM delta_file df " +
            "JOIN version_info v ON v.id = df.version_id " +
            "WHERE v.session_id = ? " +
            "  AND df.name = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, sessionId, name));
    }

}
