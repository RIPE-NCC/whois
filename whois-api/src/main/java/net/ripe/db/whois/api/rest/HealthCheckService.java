package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.iptree.IpTreeCacheManager;
import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "Loadbalancer", description = "Loadbalancer switch")

/**
 * in jmxterm, run with:
 *      bean net.ripe.db.whois:name=Loadbalancer
 *      run setLoadbalancerEnabled true|false
 *
 */


@Path("/healthcheck")
public class HealthCheckService extends JmxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);

    private final static String DB_HEALTH_CHECK_QUERY = "select object_id from last limit 1";

    private final AtomicBoolean databaseHealthy = new AtomicBoolean(true);
    private final AtomicBoolean filesystemHealthy = new AtomicBoolean(true);
    private final AtomicBoolean ipTreeHealthy = new AtomicBoolean(true);

    private final JdbcTemplate readTemplate;
    private final JdbcTemplate writeTemplate;
    private final IpTreeCacheManager ipTreeCacheManager;
    private final SourceContext sourceContext;

    private final File checkFile;
    private boolean loadbalancerEnabled;

    @Autowired
    public HealthCheckService(@Qualifier("whoisSlaveDataSource") final DataSource readDataSource,
                              @Qualifier("sourceAwareDataSource") final DataSource writeDataSource,
                              final IpTreeCacheManager ipTreeCacheManager,
                              final SourceContext sourceContext,
                              @Value("${dir.var:}") final String filesystemRoot) {
        super(LOGGER);

        this.readTemplate = new JdbcTemplate(readDataSource);
        this.writeTemplate = new JdbcTemplate(writeDataSource);
        this.ipTreeCacheManager = ipTreeCacheManager;
        this.sourceContext = sourceContext;
        this.loadbalancerEnabled = false;

        if (StringUtils.isNotBlank(filesystemRoot)) {
            this.checkFile = new File(filesystemRoot, "lock");
            LOGGER.info("File system health check file {}", checkFile);
        } else {
            this.checkFile = null;
            LOGGER.info("File system health check is disabled");
        }
    }

    @GET
    public Response check() {
        boolean isHealthy = loadbalancerEnabled && databaseHealthy.get() && filesystemHealthy.get() && ipTreeHealthy.get();

        return isHealthy ?
                Response.ok().entity("OK").build() :
                Response.status(Status.SERVICE_UNAVAILABLE).entity("DISABLED").build();
    }

    @ManagedAttribute(description = "true to enable on load balancer. False otherwise")
    public void setLoadbalancerEnabled(boolean loadbalancerEnabled) {
        this.loadbalancerEnabled = loadbalancerEnabled;
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

        ipTreeHealthy.set(ipTreeCacheManager.check(sourceContext));
        if (!ipTreeHealthy.get()) {
            LOGGER.info("IP Tree failed health check");
        }

        if (checkFile != null) {
            try {
                FileUtils.touch(checkFile);
                filesystemHealthy.set(true);
            } catch (IOException ioe) {
                LOGGER.info("Failed to touch check file: {}", ioe.getMessage());
                filesystemHealthy.set(false);
            }
        }
    }

}
