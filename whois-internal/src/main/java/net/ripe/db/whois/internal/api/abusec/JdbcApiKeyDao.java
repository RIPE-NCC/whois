package net.ripe.db.whois.internal.api.abusec;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcApiKeyDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcApiKeyDao(@Qualifier("aclDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<String> getUrisForApiKey(final String apiKey) {
        final List<String> apikeys = jdbcTemplate.query("" +
                "SELECT uri_prefix " +
                "FROM apikeys " +
                "WHERE apikey = ?",
                new RowMapper<String>() {
                    @Override
                    public String mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return rs.getString("uri_prefix");
                    }
                },
                apiKey);
        return apikeys;
    }

    public Map<String, String> getAll() {
        return jdbcTemplate.query("SELECT uri_prefix, apikey FROM apikeys", new ResultSetExtractor<Map<String, String>>() {
            @Override
            public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                final Map<String, String> results = Maps.newHashMap();
                while (rs.next()) {
                    results.put(rs.getString(1), rs.getString(2));
                }
                return results;
            }
        });
    }
}
