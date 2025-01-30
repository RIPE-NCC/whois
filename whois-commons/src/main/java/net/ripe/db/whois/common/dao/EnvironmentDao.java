package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class EnvironmentDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EnvironmentDao(@Qualifier("internalsSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Environment getCurrentEnvironment() {
        return jdbcTemplate.queryForObject("SELECT name FROM environment LIMIT 1",
                new RowMapper<Environment>() {
                    @Override
                    public Environment mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return Environment.valueOf(rs.getString(1));
                    }
                });
    }
}
