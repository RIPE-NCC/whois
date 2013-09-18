package net.ripe.db.whois.logsearch;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.DigestUtils;

import javax.sql.DataSource;
import java.util.UUID;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;

public class LogsearchTestHelper {
    static void setupDatabase(final JdbcTemplate jdbcTemplate, final String propertyBase, final String name, final String... sql) {
        final String dbBaseName = "test_" + System.currentTimeMillis() + "_" + DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());
        final String dbName = dbBaseName + "_" + name;
        jdbcTemplate.execute("CREATE DATABASE " + dbName);

        loadScripts(new JdbcTemplate(createDataSource(dbName)), sql);

        System.setProperty(propertyBase + ".url", "jdbc:mysql://localhost/" + dbName);
        System.setProperty(propertyBase + ".username", "dbint");
        System.setProperty(propertyBase + ".password", "");
    }

    static DataSource createDataSource(final String databaseName) {
        try {
            final Class<? extends java.sql.Driver> driverClass = (Class<? extends java.sql.Driver>) Class.forName("com.mysql.jdbc.Driver");

            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
            dataSource.setDriverClass(driverClass);
            dataSource.setUrl("jdbc:mysql://localhost/" + databaseName);
            dataSource.setUsername("dbint");

            return dataSource;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    static void insertApiKey(final String apiKey, final DataSource dataSource) {
        new JdbcTemplate(dataSource).update(
                "INSERT INTO apikeys (apikey, uri_prefix, comment) VALUES(?, ?, ?)",
                apiKey, "/api/logs", "logsearch apikey");
    }
}
