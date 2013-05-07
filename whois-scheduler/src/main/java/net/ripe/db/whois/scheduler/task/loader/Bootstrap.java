package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
            return null;
        }
        try {
            sourceContext.setCurrentSourceToWhoisMaster();
            loader.resetDatabase();

            // wait until trees pick up empty DB to avoid case where few updates done and new objects added to text dump result in
            // treeupdaters not recognising rebuild is needed
            try {
                Thread.sleep((IpTreeUpdater.TREE_UPDATE_IN_SECONDS) * 1000);
            } catch (InterruptedException ignored) {}

            final String result = loader.loadSplitFiles(dumpFileLocation);
            return result;
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    public String loadTextDump(String[] dumpfile) {
        try {
            sourceContext.setCurrentSourceToWhoisMaster();
            final String result = loader.loadSplitFiles(dumpfile);
            return result;
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.info(bootstrap());
        } catch (Exception e) {
            LOGGER.error("Exception caught", e);
        }
    }
}
