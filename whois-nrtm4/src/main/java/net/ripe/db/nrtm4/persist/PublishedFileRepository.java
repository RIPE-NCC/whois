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
public class PublishedFileRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<PublishedFile> rowMapper = (rs, rowNum) ->
        new PublishedFile(
            rs.getLong(1),
            rs.getLong(2),
            NrtmDocumentType.valueOf(rs.getString(3)),
            rs.getString(3),
            rs.getString(4),
            rs.getLong(5)
        );

    private final String publishedFileFields = "id, version_id, type, name, hash, created ";

    @Autowired
    public PublishedFileRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public PublishedFile save(
        final long versionId,
        final NrtmDocumentType type,
        final String name,
        final String hash
    ) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = System.currentTimeMillis();
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO published_file (version_id, type, name, hash, created) " +
                    "VALUES (?, ?, ?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, versionId);
                pst.setString(2, type.name());
                pst.setString(3, name);
                pst.setString(4, hash);
                pst.setLong(5, now);
                return pst;
            }, keyHolder
        );
        return new PublishedFile(keyHolder.getKeyAs(Long.class), versionId, type, name, hash, now);
    }
    public PublishedFile getByTypeAndVersionId(final NrtmDocumentType type, final long id) {
        final String sql = "" +
            "SELECT " + publishedFileFields +
            "FROM published_file " +
            "WHERE version_id = ? AND type = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id, type.name());
    }
}
