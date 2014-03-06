package net.ripe.db.whois.scheduler.task.loader;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Bootstrap implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final Loader loader;
    private final SourceContext sourceContext;

    @Value("${bootstrap.dumpfile:}")
    private String[] dumpFileLocation;

    @Autowired
    public Bootstrap(final Loader loader, SourceContext sourceContext) {
        this.sourceContext = sourceContext;
        this.loader = loader;
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
            loader.resetDatabase();

            // wait until trees pick up empty DB to avoid case where few updates done and new objects added to text dump result in
            // treeupdaters not recognising rebuild is needed
            Uninterruptibles.sleepUninterruptibly(IpTreeUpdater.TREE_UPDATE_IN_SECONDS, TimeUnit.SECONDS);

            return loader.loadSplitFiles(dumpFileLocation);
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    public String loadTextDump(String[] dumpfile) {
        try {
            sourceContext.setCurrentSourceToWhoisMaster();
            return loader.loadSplitFiles(dumpfile);
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    @Override
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
