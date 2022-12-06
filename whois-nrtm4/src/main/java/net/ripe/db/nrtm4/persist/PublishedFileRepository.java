package net.ripe.db.nrtm4.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;


@Repository
public class PublishedFileRepository {

    private final JdbcTemplate jdbcTemplate;
//    private final RowMapper<DeltaFileModel> rowMapper = (rs, rowNum) ->
//        new DeltaFileModel(
//            rs.getLong(1),
//            rs.getLong(2),
//            rs.getString(3),
//            rs.getString(4),
//            rs.getLong(5)
//        );

    @Autowired
    public PublishedFileRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public PublishedFile save(
        final long versionId,
        final String name,
        final String hash
    ) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = System.currentTimeMillis();
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO published_file (version_id, name, hash, created) " +
                    "VALUES (?, ?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, versionId);
                pst.setString(2, name);
                pst.setString(3, hash);
                pst.setLong(4, now);
                return pst;
            }, keyHolder
        );
        return new PublishedFile(keyHolder.getKeyAs(Long.class), versionId, name, hash, now);
    }

}
