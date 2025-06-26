package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.io.RpslObjectFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Component
public class LoaderSafe implements Loader {

    private final static Logger LOGGER = LoggerFactory.getLogger(LoaderSafe.class);
    private final ObjectLoader objectLoader;

    private final int maximumSize;

    @Autowired
    public LoaderSafe(final ObjectLoader objectLoader, @Value("${dump.total.size.limit:15}") final int maximumSize) {
        this.objectLoader = objectLoader;
        this.maximumSize = maximumSize;
    }

    @Override
    public void resetDatabase() {
        throw new UnsupportedOperationException("Reset database in transactional mode is not supported yet");
    }

    @Override
    public String loadSplitFiles(final String... filenames) {
        final Result result = new Result();
        try {
            loadSplitFiles(Arrays.asList(filenames), result);
            result.addText("Ran in transactional, safe mode: committing DB changes\n");
        } catch (final Exception e) {
            if (e.getMessage() != null){
                result.addText(String.format("\n%s\n", e.getMessage()));
            }
            result.addText("Ran in transactional, safe mode: rolling back DB changes\n");
        } finally {
            result.addText(String.format("FINISHED\n%d succeeded\n%d failed in pass 1\n%d failed in pass 2\n",
                    result.getSuccess(), result.getFailPass1(), result.getFailPass2()));
        }
        return result.toString();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    private void loadSplitFiles(final List<String> filenames, final Result result) {

        try {
            validateFiles(filenames);

            LOGGER.info("[Dump] Successfully validated. Starting Dump");

            for (final String filename : filenames) {
                // 2-pass loading: first create the skeleton objects only, and try creating the full objects in the second run
                // (when the foreign keys are already available)
                runPassSafe(result, filename, 1);
                LOGGER.info("[Dump] First step completed");
                runPassSafe(result, filename, 2);
                LOGGER.info("[Dump] Second step completed");
            }
            LOGGER.info("[Dump] Successfully Finished");
        } catch (Exception e) {
            LOGGER.error("[Dump] There was an error");
            //throw an exception here, so that transaction gets rolled back.
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void validateFiles(final List<String> filenames) {

        if (filenames == null || filenames.size() == 0) {
            throw new IllegalArgumentException("no file arguments provided");
        }

        long totalSize = 0;
        for (final String filename : filenames) {
            final File file = new File(filename);

            if (!file.isFile()) {
                throw new IllegalArgumentException(String.format("Argument '%s' is not a file\n", filename));
            }

            if (!file.exists()) {
                throw new IllegalArgumentException(String.format("Argument '%s' does not exist\n", filename));
            }

            totalSize += file.length();
        }

        if (totalSize > (long) maximumSize * 1024 * 1024) {
            throw new IllegalArgumentException(
                    String.format("Max total files' size should not be more than %d MB, \n" +
                    "but supplied files have a total size of %.2f MB.\n", maximumSize, (double)totalSize/1024/1024));
        }
    }

    private void runPassSafe(final Result result, final String filename, final int pass) {
        try {
            for (final String nextObject : new RpslObjectFileReader(filename)) {
                objectLoader.processObject(nextObject, result, pass, LoaderMode.SAFE);
            }
        } catch (IllegalArgumentException e) {
            result.addText(String.format("Error reading '%s': %s\n", filename, e.getMessage()));
            throw e;
        }
    }
}
