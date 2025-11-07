package net.ripe.db.whois.scheduler.task.loader;

import com.google.common.util.concurrent.Uninterruptibles;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.Nrtmv4Condition;
import net.ripe.db.nrtm4.scheduler.NrtmV4Jmx;
import net.ripe.db.whois.api.fulltextsearch.ElasticFullTextRebuild;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Bootstrap implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final LoaderRisky loaderRisky;
    private final LoaderSafe loaderSafe;
    private final SourceContext sourceContext;
    private final ElasticFullTextRebuild elasticFullTextRebuild;
    private NrtmV4Jmx nrtmV4Jmx;

    @Value("${bootstrap.dumpfile:}")
    private String[] dumpFileLocation;

    @Autowired
    public Bootstrap(final LoaderRisky loaderRisky, final LoaderSafe loaderSafe,
                     final ElasticFullTextRebuild elasticFullTextRebuild,
                     final SourceContext sourceContext) {
        this.loaderRisky = loaderRisky;
        this.loaderSafe = loaderSafe;
        this.sourceContext = sourceContext;
        this.elasticFullTextRebuild = elasticFullTextRebuild;
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

            final String result =  loaderRisky.loadSplitFiles(dumpFileLocation);

            buildFullTextIndexes();
            initializeNrtmv4();

            return result;
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    private void buildFullTextIndexes() {
        try {
            elasticFullTextRebuild.run();
        } catch (Exception e) {
            LOGGER.error("Failed to rebuild ElasticFullTextRebuild ", e);
        }
    }

    private void initializeNrtmv4() {
        if(nrtmV4Jmx == null) {
            return;
        }

        try {
            nrtmV4Jmx.runInitializerTask("Bootstrap");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Nrtmv4 ", e);
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
    @Scheduled(cron = "0 0 0 * * *")
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

    @Autowired(required = false)
    @Conditional(Nrtmv4Condition.class)
    public void setNrtmV4Jmx(final NrtmV4Jmx nrtmV4Jmx) {
        this.nrtmV4Jmx = nrtmV4Jmx;
    }
}
