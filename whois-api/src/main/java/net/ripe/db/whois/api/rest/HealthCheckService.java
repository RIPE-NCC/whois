package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.iptree.IpTreeCacheManager;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HealthCheckService {

    private final static String DB_HEALTH_CHECK_QUERY = "select object_id from last limit 1";

    private final AtomicBoolean databaseHealthy = new AtomicBoolean(true);
    private final AtomicBoolean filesystemHealthy = new AtomicBoolean(true);
    private final AtomicBoolean ipTreeHealthy = new AtomicBoolean(true);

    private final JdbcTemplate readTemplate;
    private final JdbcTemplate writeTemplate;
    private final IpTreeCacheManager ipTreeCacheManager;
    private final SourceContext sourceContext;

    private final File checkFile;

    @Autowired
    public HealthCheckService(@Qualifier("whoisSlaveDataSource") final DataSource readDataSource,
                              @Qualifier("sourceAwareDataSource") final DataSource writeDataSource,
                              final IpTreeCacheManager ipTreeCacheManager,
                              final SourceContext sourceContext,
                              @Value("${dir.var") final String filesystemRoot) {
        this.readTemplate = new JdbcTemplate(readDataSource);
        this.writeTemplate = new JdbcTemplate(writeDataSource);
        this.ipTreeCacheManager = ipTreeCacheManager;
        this.sourceContext = sourceContext;
        this.checkFile = new File(filesystemRoot, "check");
    }

    @GET
    @Path("/healthcheck")
    public Response check() {
        return databaseHealthy.get() && filesystemHealthy.get() && ipTreeHealthy.get()?
                Response.ok().build() :
                Response.serverError().build();
    }

    @Scheduled(fixedDelay = 60 * 1_000)
    void updateStatus() {
        try {
            readTemplate.queryForObject(DB_HEALTH_CHECK_QUERY, Integer.class);
            writeTemplate.queryForObject(DB_HEALTH_CHECK_QUERY, Integer.class);
            databaseHealthy.set(true);
        } catch (DataAccessException e) {
            databaseHealthy.set(false);
        }

        ipTreeHealthy.set(ipTreeCacheManager.check(sourceContext));

        try {
            FileUtils.touch(checkFile);
            filesystemHealthy.set(true);
        } catch (IOException ioe) {
            filesystemHealthy.set(false);
        }
    }

}
