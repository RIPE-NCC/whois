package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class LegacyAutnumDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LegacyAutnumDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<CIString> readLegacyAutnums() {
        return jdbcTemplate.query("SELECT autnum FROM legacy_autnums", new RowMapper<CIString>() {
            @Override
            public CIString mapRow(final ResultSet resultSet, final int i) throws SQLException {
                return CIString.ciString("AS" + resultSet.getString("autnum"));
            }
        });
    }

    public void store(final List<String> legacyAutnums) {
        jdbcTemplate.batchUpdate("INSERT INTO legacy_autnums (autnum) VALUES(?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setString(1, legacyAutnums.get(i).trim());
            }

            @Override
            public int getBatchSize() {
                return legacyAutnums.size();
            }
        });
    }
}
