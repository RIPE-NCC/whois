package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.io.RpslObjectFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.sanityCheck;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;

@Component
public class LoaderRisky implements Loader {

    private final JdbcTemplate whoisTemplate;
    private final ObjectLoader objectLoader;

    @Autowired
    public LoaderRisky(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final ObjectLoader objectLoader) {
        this.objectLoader = objectLoader;
        this.whoisTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void resetDatabase() {
        sanityCheck(whoisTemplate);
        truncateTables(whoisTemplate);
        loadScripts(whoisTemplate, "whois_data.sql");
    }

    @Override
    public String loadSplitFiles(String... filenames) {
        final Result result = new Result();
        try {
            validateFiles(Arrays.asList(filenames));

            for (String filename : filenames) {
                // 2-pass loading: first create the skeleton objects only, and try creating the full objects in the second run
                // (when the foreign keys are already available)
                runPassRisky(result, filename, 1);
                runPassRisky(result, filename, 2);
            }
        } catch (Exception e) {
            result.addText(String.format("\n%s\n", e.getMessage()));
        } finally {
            result.addText(String.format("FINISHED\n%d succeeded\n%d failed in pass 1\n%d failed in pass 2\n",
                    result.getSuccess(), result.getFailPass1(), result.getFailPass2()));
            if (result.getFailPass1() > 0 || result.getFailPass2() > 0 ){
                result.addText("Ran in non transactional, unsafe mode: no rollback for DB changes\n");
            }
        }
        return result.toString();
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

    @Override
    public void validateFiles(final List<String> filenames) {

        if (filenames == null || filenames.size() == 0) {
            throw new IllegalArgumentException("no file arguments provided");
        }

        for (final String filename : filenames) {
            final File file = new File(filename);

            if (!file.isFile()) {
                throw new IllegalArgumentException(String.format("Argument '%s' is not a file\n", filename));
            }

            if (!file.exists()) {
                throw new IllegalArgumentException(String.format("Argument '%s' does not exist\n", filename));
            }
        }
    }
}
