package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.io.RpslObjectFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;

@Component
public class LoaderSafe extends Loader {
    private final static int FILE_SIZE_LIMIT_IN_MB = 10;

    @Autowired
    public LoaderSafe(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final ObjectLoader objectLoader) {
        super(dataSource, objectLoader);
    }

    @Override
    protected void resetDatabase() {
        throw new UnsupportedOperationException("Reset database in transactional mode is not supported yet");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    protected void loadSplitFiles(Result result, String... filenames) {
        result.addText("Running in transactional, safe mode\n");

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

            if (file.length() > FILE_SIZE_LIMIT_IN_MB * 1024 * 1024) {
                result.addText(String.format("Max file size is %d MB \n", FILE_SIZE_LIMIT_IN_MB));
                continue;
            }

            // 2-pass loading: first create the skeleton objects only, and try creating the full objects in the second run
            // (when the foreign keys are already available)
            runPassSafe(result, filename, 1);
            runPassSafe(result, filename, 2);
        }
    }

    private void runPassSafe(final Result result, final String filename, final int pass) {
        try {
            for (final String nextObject : new RpslObjectFileReader(filename)) {
                objectLoader.processObject(nextObject, result, pass, LoaderMode.SAFE);
            }
        } catch (Exception e) {
            result.addText(String.format("Error reading '%s': %s\n", filename, e.getMessage()));
        }
    }
}
