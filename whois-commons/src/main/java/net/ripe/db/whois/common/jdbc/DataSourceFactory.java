package net.ripe.db.whois.common.jdbc;

import jakarta.sql.DataSource;

public interface DataSourceFactory {
    DataSource createDataSource(String url, String username, String password);
}
