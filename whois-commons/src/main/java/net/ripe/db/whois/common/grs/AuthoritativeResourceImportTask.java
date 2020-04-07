package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.javacrumbs.shedlock.core.SchedulerLock;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.apache.commons.lang.StringUtils;
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

@Component
public class AuthoritativeResourceImportTask implements DailyScheduledTask, EmbeddedValueResolverAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceImportTask.class);

    private boolean enabled;

    protected static final String TASK_NAME = "AuthoritativeResourceImport";
    private static final Splitter PROPERTY_LIST_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    private final ResourceDataDao resourceDataDao;
    private StringValueResolver valueResolver;
    private final Downloader downloader;
    private final String downloadDir;
    private final Set<String> sourceNames;

    @Autowired
    public AuthoritativeResourceImportTask(@Value("${grs.sources}") final String grsSourceNames,
                                           final ResourceDataDao resourceDataDao,
                                           final Downloader downloader,
                                           @Value("${dir.grs.import.download:}") final String downloadDir,
                                           @Value("${grs.import.enabled}") final boolean enabled)
    {
        this.sourceNames = Sets.newHashSet(Iterables.transform(PROPERTY_LIST_SPLITTER.split(grsSourceNames), input -> input.toLowerCase().replace("-grs", "")));
        this.resourceDataDao = resourceDataDao;
        this.downloader = downloader;
        this.downloadDir = downloadDir;
        this.enabled = enabled;
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    /**
     * Run at 00.15 so we don't miss the the delegated stats file which is normally published around midnight.
     */
    @Override
    @Scheduled(cron = "0 15 0 * * *")
    @SchedulerLock(name = TASK_NAME)
    public void run() {
        if (!enabled) {
            LOGGER.info("Authoritative resource import task is disabled");
            return;
        }

        for (final String sourceName : sourceNames) {
            try {
                final AuthoritativeResource authoritativeResource = downloadAuthoritativeResource(sourceName);
                resourceDataDao.store(sourceName, authoritativeResource);
            } catch (Exception e) {
                LOGGER.warn("Exception processing " + sourceName, e);
            }
        }
    }

    private AuthoritativeResource downloadAuthoritativeResource(final String sourceName) throws IOException {
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
