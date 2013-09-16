package net.ripe.db.whois.scheduler.task.loader;

import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.*;

@Component
public class Loader {
    private static final int NUM_LOADER_THREADS = 8;
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

    public String loadSplitFiles(String... entries) {
        Result result = new Result();
        for (String entry : entries) {
            File file = new File(entry);
            if (!file.isFile()) {
                result.addText(String.format("Argument '%s' is not a file\n", entry));
                continue;
            }

            if (!file.exists()) {
                result.addText(String.format("Argument '%s' does not exist\n", entry));
                continue;
            }

            // 2-pass loading: first create the skeleton objects only, and try creating the full objects in the second run
            // (when the foreign keys are already available)
            runPass(result, entry, 1);
            runPass(result, entry, 2);
        }
        result.addText(String.format("FINISHED\n%d succeeded, %d failed\n", result.getSuccess(), result.getFail()));
        return result.toString();
    }

    private String getNextObject(BufferedReader reader) throws IOException {
        String line;
        StringBuilder partialObject = new StringBuilder(1024);
        while ((line = reader.readLine()) != null) {
            if (StringUtils.isEmptyOrWhitespaceOnly(line)) {
                return partialObject.toString();
            } else {
                if (line.charAt(0) != '#') {
                    partialObject.append(line).append('\n');
                }
            }
        }

        if (partialObject.length() > 0) {
            return partialObject.toString();
        } else {
            return null; // terminator
        }
    }

    private void runPass(Result result, String entry, int pass) {
        InputStream in;

        try {
            in = new FileInputStream(entry);
            if (entry.endsWith(".gz")) {
                in = new GZIPInputStream(in);
            }
        } catch (IOException e) {
            result.addText(String.format("Error opening '%s': %s\n", entry, e.getMessage()));
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // sadly Executors don't offer a bounded/blocking submit() implementation
        final ExecutorService executorService = new ThreadPoolExecutor(NUM_LOADER_THREADS, NUM_LOADER_THREADS,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(256), new ThreadPoolExecutor.CallerRunsPolicy());

        try {
            for (String nextObject = getNextObject(reader); nextObject != null; nextObject = getNextObject(reader)) {
                executorService.submit(new RpslObjectProcessor(nextObject, result, pass));
            }
        } catch (IOException e) {
            result.addText(String.format("Error reading '%s': %s\n", entry, e.getMessage()));
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
            }
        }

    }

    class RpslObjectProcessor implements Runnable {
        private final String rpslObject;
        private final Result result;
        private final int pass;

        RpslObjectProcessor(String rpslObject, Result result, int pass) {
            this.rpslObject = rpslObject;
            this.result = result;
            this.pass = pass;
        }

        @Override
        public void run() {
            objectLoader.processObject(rpslObject, result, pass);
        }
    }
}
