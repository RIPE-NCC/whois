package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import net.javacrumbs.shedlock.core.SchedulerLock;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

@Component
public class RipeAuthoritativeResourceImportTask extends AbstractAutoritativeResourceImportTask implements DailyScheduledTask {

    protected static final String TASK_NAME = "RipeAuthoritativeResourceImport";

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String rsngBaseUrl;

    private Client client;

    @Autowired
    public RipeAuthoritativeResourceImportTask(final ResourceDataDao resourceDataDao,
                                               @Value("${grs.import.enabled}") final boolean enabled,
                                               @Value("${rsng.base.url:}") final String rsngBaseUrl) {
        super(enabled && !StringUtils.isBlank(rsngBaseUrl), resourceDataDao);
        this.rsngBaseUrl = rsngBaseUrl;
        this.client = ClientBuilder.newBuilder()
                .property(ClientProperties.CONNECT_TIMEOUT, 10_000)
                .property(ClientProperties.READ_TIMEOUT, 10_000)
                .build();

        LOGGER.info("Authoritative resource RSNG import task is {}abled", enabled? "en" : "dis");
    }

    /**
     * Run every 15 minutes.
     */
    @Override
    @Scheduled(cron = "0 5/15 * * * *")
    @SchedulerLock(name = TASK_NAME)
    public void run() {
        doImport(Sets.newHashSet(SOURCE_NAME_RIPE));
    }

    @Override
    protected AuthoritativeResource fetchAuthoritativeResource(String sourceName) throws IOException {
        final AuthoritativeResource authoritativeResource = new AuthoritativeResourceJsonLoader(LOGGER).load(
            OBJECT_MAPPER.readValue(
                client.target(rsngBaseUrl)
                .path("/rsng-stat/stat/rirstats")
                .request()
                .get(String.class),
            JsonNode.class)
        );


        LOGGER.info("Downloaded {}; asn: {}, ipv4: {}, ipv6: {}", sourceName, authoritativeResource.getNrAutNums(), authoritativeResource.getNrInetnums(), authoritativeResource.getNrInet6nums());
        return authoritativeResource;
    }

}
