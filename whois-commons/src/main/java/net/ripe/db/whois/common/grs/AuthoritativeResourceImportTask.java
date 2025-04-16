package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthoritativeResourceImportTask extends AbstractAutoritativeResourceImportTask implements DailyScheduledTask, EmbeddedValueResolverAware {

    static final String TASK_NAME = "AuthoritativeResourceImport";


    private static final Splitter PROPERTY_LIST_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    private StringValueResolver valueResolver;
    private final Downloader downloader;
    private final String downloadDir;
    private final Set<String> sourceNames;

    @Autowired
    public AuthoritativeResourceImportTask(@Value("${grs.sources}") final String grsSourceNames,
                                           final ResourceDataDao resourceDataDao,
                                           final Downloader downloader,
                                           @Value("${dir.grs.import.download:}") final String downloadDir,
                                           @Value("${grs.import.enabled:false}") final boolean enabled,
                                           @Value("${rsng.base.url:}") final String rsngBaseUrl) {
        super(enabled, resourceDataDao);

        final boolean rsngImportDisabled = StringUtils.isBlank(rsngBaseUrl);

        this.sourceNames = PROPERTY_LIST_SPLITTER.splitToStream(grsSourceNames)
                .map(input -> input.toLowerCase().replace("-grs", ""))
                .filter(source -> !SOURCE_NAME_RIPE.equalsIgnoreCase(source) || rsngImportDisabled)
                .collect(Collectors.toSet());

        this.downloader = downloader;
        this.downloadDir = downloadDir;

        LOGGER.info("Authoritative resource import task is {}abled", enabled? "en" : "dis");
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    /**
     * Run at 00.15 so we don't miss the the delegated stats file which is normally published around midnight.
     */
    @Override
    @Scheduled(cron = "0 15 0 * * *", zone = EUROPE_AMSTERDAM)
    @SchedulerLock(name = TASK_NAME)
    public void run() {
        doImport(sourceNames);
    }

    protected AuthoritativeResource fetchAuthoritativeResource(final String sourceName) throws IOException {
        final Logger logger = LoggerFactory.getLogger(String.format("%s_%s", getClass().getName(), sourceName));
        final String resourceDataUrl = valueResolver.resolveStringValue(String.format("${grs.import.%s.resourceDataUrl:}", sourceName));
        if (StringUtils.isBlank(resourceDataUrl)) {
            return AuthoritativeResource.unknown();
        }

        final Path resourceDataFile = Paths.get(downloadDir, sourceName + "-RES");

        downloader.downloadToWithMd5Check(logger, new URL(resourceDataUrl), resourceDataFile);
        final AuthoritativeResource authoritativeResource = AuthoritativeResource.loadFromFile(logger, sourceName, resourceDataFile);
        logger.info("Downloaded {}; asn: {}, ipv4: {}, ipv6: {}", sourceName, authoritativeResource.getNrAutNums(), authoritativeResource.getNrInetnums(), authoritativeResource.getNrInet6nums());
        return authoritativeResource;
    }
}
