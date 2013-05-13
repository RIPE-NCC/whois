package net.ripe.db.whois.common.source;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class SourceConfiguration {
    private final Source source;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public SourceConfiguration(final Source source, final DataSource dataSource) {
        this.source = source;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Source getSource() {
        return source;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
