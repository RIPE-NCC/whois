package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.domain.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.UUID;


@Repository
public class NtrmVersionDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public NtrmVersionDao(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public VersionInformation findLastVersion(final NrtmSource source) {
        return jdbcTemplate.queryForObject(
                "select id, source, version, session_id, timestamp " +
                        "from version_information " +
                        "where source = ? " +
                        "order by version desc limit 1",
                rowMapper(),
                source.name());
    }

    public void createNew(final NrtmSource source) {

        jdbcTemplate.update(
                "insert into version_information (id, source, version, session_id, timestamp) " +
                        "values (?, ?, ?, ?, ?)",
                101L,
                source.name(),
                0L,
                UUID.randomUUID(),
                System.currentTimeMillis());
    }

    private RowMapper<VersionInformation> rowMapper() {
        return (rs, rowNum) -> new VersionInformation(
                rs.getLong(1),
                NrtmSource.valueOf(rs.getString(2)),
                rs.getLong(3),
                UUID.fromString(rs.getString(4)),
                Timestamp.from(rs.getString(5))
        );
    }

}