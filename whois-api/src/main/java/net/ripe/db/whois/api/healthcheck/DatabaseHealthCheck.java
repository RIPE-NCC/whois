package net.ripe.db.whois.api.healthcheck;

import net.ripe.db.whois.common.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DatabaseHealthCheck implements HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHealthCheck.class);

    private final static String DB_HEALTH_CHECK_QUERY = "select object_id from last limit 1";

    private final AtomicBoolean databaseHealthy = new AtomicBoolean(true);
    private final JdbcTemplate readTemplate;
    private final JdbcTemplate writeTemplate;

    @Autowired
    public DatabaseHealthCheck(@Qualifier("whoisSlaveDataSource") final DataSource readDataSource,
                              @Qualifier("whoisMasterDataSource") final DataSource writeDataSource) {
        this.readTemplate = new JdbcTemplate(readDataSource);
        this.writeTemplate = new JdbcTemplate(writeDataSource);
        readTemplate.setQueryTimeout(5);
        writeTemplate.setQueryTimeout(5);
    }

    @Override
    public boolean check() {
        return databaseHealthy.get();
    }

    @Scheduled(fixedDelay = 60 * 1_000)
    void updateStatus() {
        try {
            readTemplate.queryForObject(DB_HEALTH_CHECK_QUERY, Integer.class);
            writeTemplate.queryForObject(DB_HEALTH_CHECK_QUERY, Integer.class);
            databaseHealthy.set(true);
        } catch (DataAccessException e) {
            LOGGER.info("Database connection failed health check: {}", e.getMessage());
            databaseHealthy.set(false);
        }
    }

}
