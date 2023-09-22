package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;


@Repository
public class NrtmSourceDao {

    private final JdbcTemplate jdbcTemplate;
    private final List<String> sources;
    private final String source;

    NrtmSourceDao(
        @Qualifier("nrtmMasterDataSource") final DataSource dataSource,
        @Value("${whois.source}") final String source,
        @Value("${whois.nonauth.source}") final String nonauthSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.source = source.toUpperCase();
        sources = List.of(
            source.toUpperCase(),
            nonauthSource.toUpperCase()
        );
    }

    public void createSources() {
        final String sql = """
            INSERT INTO source (name)
            VALUES (?)
            """;
        for (final String source : sources) {
            jdbcTemplate.update(sql, source);
        }
    }

    public Optional<NrtmSource> getWhoisSource() {
        final String sql = """
            SELECT id, name
            FROM source
            WHERE name = ?
            """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                sql,
                (rs, rn) -> new NrtmSource(rs.getLong(1), CIString.ciString(rs.getString(2))),
                this.source));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<NrtmSource> getSources() {
        final String sql = """
            SELECT id, name
            FROM source
            """;
        try {
            return jdbcTemplate.query(sql, (rs, rn) -> new NrtmSource(rs.getLong(1), CIString.ciString(rs.getString(2))));
        } catch (final EmptyResultDataAccessException e) {
            return List.of();
        }
    }

}
