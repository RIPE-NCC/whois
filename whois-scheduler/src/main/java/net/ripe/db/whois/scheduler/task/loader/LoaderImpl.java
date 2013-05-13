package net.ripe.db.whois.scheduler.task.loader;

import com.mysql.jdbc.StringUtils;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.*;
import java.util.zip.GZIPInputStream;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.*;

@Component
public class LoaderImpl implements Loader {
    private final UpdateLockDao updateLockDao;
    private final JdbcTemplate whoisTemplate;
    private final ObjectLoader objectLoader;

    @Autowired
    public LoaderImpl(@Qualifier("sourceAwareDataSource") final DataSource dataSource,
                      UpdateLockDao updateLockDao, ObjectLoader objectLoader) {
        this.updateLockDao = updateLockDao;
        this.objectLoader = objectLoader;
        this.whoisTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void resetDatabase() {
        sanityCheck(whoisTemplate);
        updateLockDao.setUpdateLock();
        truncateTables(whoisTemplate);
        loadScripts(whoisTemplate, "whois_data.sql");
    }

    @Override
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
        String line;
        StringBuilder partialObject = new StringBuilder(1024);

        try {
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isEmptyOrWhitespaceOnly(line)) {
                    try {
                        objectLoader.processObject(partialObject.toString(), result, pass);
                    } catch (TransactionException e) {}     // error is already added to result
                    partialObject = new StringBuilder(1024);
                } else {
                    if (line.charAt(0) != '#') {
                        partialObject.append(line).append('\n');
                    }
                }
            }
        } catch (IOException e) {
            result.addText(String.format("Error reading '%s': %s\n", entry, e.getMessage()));
            return;
        }

        if (partialObject.length() > 0) {
            objectLoader.processObject(partialObject.toString(), result, pass);
        }
    }
}
