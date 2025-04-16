package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RipeAuthoritativeResourceImportTask extends AbstractAutoritativeResourceImportTask implements DailyScheduledTask {

    protected static final String TASK_NAME = "RipeAuthoritativeResourceImport";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String rsngBaseUrl;
    private final Client client;
    private final boolean useSingleApi;
    private final String apiKey;
    private final ExecutorService executorService;

    @Autowired
    public RipeAuthoritativeResourceImportTask(final ResourceDataDao resourceDataDao,
                                               @Value("${grs.import.enabled:false}") final boolean grsImportEnabled,
                                               @Value("${rsng.base.url:}") final String rsngBaseUrl,
                                               @Value("${rsng.use.single.api:true}") final boolean useSingleApi,
                                               @Value("${rsng.stats.api.key:}") final String apiKey) {
        super(grsImportEnabled && !StringUtils.isBlank(rsngBaseUrl), resourceDataDao);
        this.rsngBaseUrl = rsngBaseUrl;
        this.client = ClientBuilder.newBuilder()
                .property(ClientProperties.CONNECT_TIMEOUT, 10_000)
                .property(ClientProperties.READ_TIMEOUT, 60_000)
                .register(GZipEncoder.class)
                .register(DeflateEncoder.class)
                .build();

        // there are 3 separate api calls
        this.executorService = Executors.newFixedThreadPool(3);
        this.useSingleApi = useSingleApi;
        this.apiKey = apiKey;

        LOGGER.info("Authoritative resource RSNG import task is {}abled", grsImportEnabled && !StringUtils.isBlank(rsngBaseUrl)? "en" : "dis");
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
        final AuthoritativeResource authoritativeResource;

        if (useSingleApi) {
            authoritativeResource  = new AuthoritativeResourceJsonLoader(LOGGER).load(
                    OBJECT_MAPPER.readValue(
                            client.target(rsngBaseUrl)
                                    .path("/rsng-stat/stat/rirstats")
                                    .request()
                                    .header(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                                    .get(String.class),
                            JsonNode.class)
            );
        } else {
            authoritativeResource = new RsngAuthoritativeResourceWorker(LOGGER, rsngBaseUrl, client, executorService, apiKey).load();
        }

        LOGGER.info("Downloaded {}; asn: {}, ipv4: {}, ipv6: {}", sourceName, authoritativeResource.getNrAutNums(), authoritativeResource.getNrInetnums(), authoritativeResource.getNrInet6nums());
        return authoritativeResource;
    }
}
