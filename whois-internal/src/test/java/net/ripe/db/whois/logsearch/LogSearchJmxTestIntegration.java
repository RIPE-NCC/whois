package net.ripe.db.whois.logsearch;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.logsearch.NewLogFormatProcessor;
import net.ripe.db.whois.internal.logsearch.jmx.LogFileUpdateJmx;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class LogSearchJmxTestIntegration extends AbstractLogSearchTest {
    @Autowired
    private NewLogFormatProcessor newLogFormatProcessor;
    @Autowired
    private LogFileUpdateJmx logFileUpdateJmx;

    @Test
    public void search_by_updateId() throws Exception {
        addToIndex(LogFileHelper.createTarredLogFile(logDirectory, "20130102", "100102", "random", "mntner: UPD-MNT"));

        final String results = logFileUpdateJmx.searchByUpdateId(".*");

        assertThat(results, containsString("Found 1 updates matching regex .*\n"));
        assertThat(results, containsString("001.msg-in.txt.gz"));
    }

    // helper methods

    private void addToIndex(final File file) throws IOException {
        if (file.isDirectory()) {
            newLogFormatProcessor.incrementalUpdate();
        } else {
            if (file.getAbsolutePath().endsWith(".tar")) {
                newLogFormatProcessor.addFileToIndex(file.getAbsolutePath());
            } else {
                newLogFormatProcessor.incrementalUpdate();
            }
        }
    }


}
