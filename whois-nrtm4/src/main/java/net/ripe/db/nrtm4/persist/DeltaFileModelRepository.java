package net.ripe.db.nrtm4.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;


@Repository
public class DeltaFileModelRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<DeltaFileModel> rowMapper = (rs, rowNum) ->
            new DeltaFileModel(
                    rs.getLong(1),
                    rs.getLong(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getInt(6),
                    rs.getLong(7)
            );

    @Autowired
    public DeltaFileModelRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public DeltaFileModel findLastChange() {
        final String sql = "" +
                "select id, version_id, name, payload, hash, last_serial_id, created from delta_file " +
                "where last_serial_id = (select max(last_serial_id) from delta_file)";
        return jdbcTemplate.queryForObject(sql, rowMapper);
    }

}
