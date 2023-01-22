package net.ripe.db.whois.common.jdbc;

import javax.sql.DataSource;

public interface DataSourceFactory {
    DataSource createDataSource(String url, String username, String password);
}
