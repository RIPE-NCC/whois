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
public class DeltaFileModelRepository {

    // TODO: do we really need to store these in the db?
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<DeltaFileModel> rowMapper = (rs, rowNum) ->
        new DeltaFileModel(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getString(4),
            rs.getString(5),
            rs.getLong(6)
        );

    @Autowired
    public DeltaFileModelRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

//    public Optional<DeltaFileModel> findLastChange() {
//        final String sql = "" +
//            "SELECT id, version_id, name, payload, hash, created FROM delta_file " +
//            "WHERE last_serial_id = (SELECT MAX(last_serial_id) FROM delta_file)";
//        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper));
//    }

    public DeltaFileModel save(
        final Long versionId,
        final String name,
        final String payload,
        final String hash
    ) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = System.currentTimeMillis();
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO delta_file (version_id, name, payload, hash, created) " +
                    "VALUES (?, ?, ?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, versionId);
                pst.setString(2, name);
                pst.setString(3, payload);
                pst.setString(4, hash);
                pst.setLong(5, now);
                return pst;
            }, keyHolder
        );
        return new DeltaFileModel(keyHolder.getKeyAs(Long.class), versionId, name, payload, hash, now);
    }

}
