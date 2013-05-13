package net.ripe.db.whois.common.jdbc;

import com.google.common.collect.Lists;
import com.mchange.v2.c3p0.DataSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * Shuts down data sources when the application is stopped.
 */
@Component
public class DataSourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class);

    private final List<DataSource> dataSources;

    @Autowired
    public DataSourceManager(final DataSource... dataSources) {
        this.dataSources = Lists.newArrayList(dataSources);
    }

    @PreDestroy
    public void stopService() {
        for (final DataSource dataSource : dataSources) {
            try {
                DataSources.destroy(dataSource);
            } catch (SQLException e) {
                LOGGER.error("Got an exception during destruction of the connection pool", e);
            }
        }
    }
}
