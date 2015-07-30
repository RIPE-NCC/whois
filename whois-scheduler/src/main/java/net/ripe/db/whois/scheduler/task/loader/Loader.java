package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.io.RpslObjectFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class Loader {
    private final JdbcTemplate whoisTemplate;
    private final ObjectLoader objectLoader;

    @Autowired
    public Loader(@Qualifier("sourceAwareDataSource") final DataSource dataSource, ObjectLoader objectLoader) {
        this.objectLoader = objectLoader;
        this.whoisTemplate = new JdbcTemplate(dataSource);
    }

    public void resetDatabase() {
        sanityCheck(whoisTemplate);
        truncateTables(whoisTemplate);
        loadScripts(whoisTemplate, "whois_data.sql");
    }

    public String loadSplitFiles(String... filenames) {
        Result result = new Result();
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
            runPass(result, filename, 1);
            runPass(result, filename, 2);
        }
        //TODO [TP]: the logging is a bit misleading. success number is correct, failed can be double because...
        //TODO ...for a single object failure in two passes counts as 2 failures
        result.addText(String.format("FINISHED\n%d succeeded, %d failed\n", result.getSuccess(), result.getFail()));
        return result.toString();
    }

    private void runPass(final Result result, final String filename, final int pass) {
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
                        objectLoader.processObject(nextObject, result, pass);
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
