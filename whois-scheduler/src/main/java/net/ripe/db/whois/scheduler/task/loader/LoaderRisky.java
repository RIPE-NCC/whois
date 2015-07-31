package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.io.RpslObjectFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.sanityCheck;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;

@Component
public class LoaderRisky extends Loader {

    @Autowired
    public LoaderRisky(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final ObjectLoader objectLoader) {
        super(dataSource, objectLoader);
    }

    @Override
    protected void resetDatabase() {
        sanityCheck(whoisTemplate);
        truncateTables(whoisTemplate);
        loadScripts(whoisTemplate, "whois_data.sql");
    }

    @Override
    protected void loadSplitFiles(Result result, String... filenames) {
        result.addText("Running in non transactional, unsafe mode\n");

        for (String filename : filenames) {
            File file = new File(filename);

            if (!file.isFile()) {
                result.addText(String.format("Argument '%s' is not a file\n", filename));
                continue;
            }

            if (!file.exists()) {
                result.addText(String.format("Argument '%s' does not exist\n", filename));
                continue;
            }

            // 2-pass loading: first create the skeleton objects only, and try creating the full objects in the second run
            // (when the foreign keys are already available)
            runPassRisky(result, filename, 1);
            runPassRisky(result, filename, 2);
        }
    }

    private void runPassRisky(final Result result, final String filename, final int pass) {
        // sadly Executors don't offer a bounded/blocking submit() implementation
        final int numThreads = Runtime.getRuntime().availableProcessors();
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numThreads*16);
        final ExecutorService executorService = new ThreadPoolExecutor(numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

        try {
            for (final String nextObject : new RpslObjectFileReader(filename)) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        objectLoader.processObject(nextObject, result, pass, LoaderMode.FAST_AND_RISKY);
                    }
                });
            }
        } catch (Exception e) {
            result.addText(String.format("Error reading '%s': %s\n", filename, e.getMessage()));
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                result.addText(e.getMessage() + "\n");
            }
        }
    }
}
