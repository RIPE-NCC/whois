package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.google.common.collect.Sets;
import net.javacrumbs.shedlock.core.SchedulerLock;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Component
public class RipeAuthoritativeResourceImportTask extends AbstractAutoritativeResourceImportTask implements DailyScheduledTask {

    protected static final String TASK_NAME = "RipeAuthoritativeResourceImport";
    private final String baseUrl;

    private Client client;

    @Autowired
    public RipeAuthoritativeResourceImportTask(final ResourceDataDao resourceDataDao,
                                               @Value("${grs.import.enabled}") final boolean enabled,
                                               @Value("${rsng.base.url}") final String baseUrl) {
        super(enabled, resourceDataDao);
        this.baseUrl = baseUrl;
        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeatures.class)
                .property(ClientProperties.CONNECT_TIMEOUT, 10_000)
                .property(ClientProperties.READ_TIMEOUT, 10_000)
                .build();
    }

    /**
     * Run every 15 minutes.
     */
    @Override
    @Scheduled(cron = "0 5/15 * * * *")
    @SchedulerLock(name = TASK_NAME)
    public void run() {
        doImport(Sets.newHashSet("ripe"));
    }

    @Override
    protected AuthoritativeResource fetchAuthoritativeResource(String sourceName) {
        final AuthoritativeResource authoritativeResource = new AuthoritativeResourceJsonLoader(LOGGER).load(client.target(baseUrl)
                .path("/rsng-stat/stat/rirstats")
                .request()
                .get(JsonNode.class));


        LOGGER.info("Downloaded {}; asn: {}, ipv4: {}, ipv6: {}", sourceName, authoritativeResource.getNrAutNums(), authoritativeResource.getNrInetnums(), authoritativeResource.getNrInet6nums());
        return authoritativeResource;
    }

}
