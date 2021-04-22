package net.ripe.db.whois.scheduler.task.loader;

import com.google.common.util.concurrent.Uninterruptibles;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Bootstrap implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final LoaderRisky loaderRisky;
    private final LoaderSafe loaderSafe;
    private final SourceContext sourceContext;

    private final FullTextIndex fullTextIndex;

    @Value("${bootstrap.dumpfile:}")
    private String[] dumpFileLocation;

    @Autowired
    public Bootstrap(final LoaderRisky loaderRisky, final LoaderSafe loaderSafe,
                     final SourceContext sourceContext, final FullTextIndex fullTextIndex) {
        this.loaderRisky = loaderRisky;
        this.loaderSafe = loaderSafe;
        this.sourceContext = sourceContext;
        this.fullTextIndex = fullTextIndex;
    }

    public void setDumpFileLocation(final String... testDumpFileLocation) {
        this.dumpFileLocation = testDumpFileLocation;
    }

    public String bootstrap() {
        if (dumpFileLocation == null || dumpFileLocation.length == 0 || dumpFileLocation[0] == null || dumpFileLocation[0].length() == 0) {
            return "Bootstrap is not enabled (dump file undefined)";
        }
        try {
            sourceContext.setCurrentSourceToWhoisMaster();
            loaderRisky.resetDatabase();

            // wait until trees pick up empty DB to avoid case where few updates done and new objects added to text dump result in
            // treeupdaters not recognising rebuild is needed
            Uninterruptibles.sleepUninterruptibly(IpTreeUpdater.TREE_UPDATE_IN_SECONDS, TimeUnit.SECONDS);

            final String result = loaderRisky.loadSplitFiles(dumpFileLocation);

            fullTextIndex.rebuild();

            return result;
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    public String loadTextDumpSafe(String[] dumpfile) {
        return loadTextDump(dumpfile, loaderSafe);
    }

    public String loadTextDumpRisky(String[] dumpfile) {
        return loadTextDump(dumpfile, loaderRisky);
    }

    private String loadTextDump(String[] dumpfile, final Loader loader) {
        try {
            sourceContext.setCurrentSourceToWhoisMaster();
            return loader.loadSplitFiles(dumpfile);
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = RUN_TIMEZONE)
    @SchedulerLock(name = "Bootstrap")
    public void run() {
        try {
            final String bootstrap = bootstrap();
            if (!StringUtils.isBlank(bootstrap)) {
                LOGGER.info(bootstrap);
            }
        } catch (Exception e) {
            LOGGER.error("Exception caught", e);
        }
    }
}
