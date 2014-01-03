package net.ripe.db.whois.logsearch;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.logsearch.LegacyLogFormatProcessor;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Category(IntegrationTest.class)
public class LogSearchLegacyFormatTestIntegration extends AbstractLogSearchTest {
    @Autowired
    private LegacyLogFormatProcessor legacyLogFormatProcessor;

    @Test
    public void legacy_one_logfile() throws Exception {
        addToIndex(LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox"));

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_one_logfile_no_duplicates() throws Exception {
        final File logfile = LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        addToIndex(logfile);
        addToIndex(logfile);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_log_directory_one_logfile() throws Exception {
        LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        addToIndex(logDirectory);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_log_directory_multiple_logfiles() throws Exception {
        LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        LogFileHelper.createBzippedLogFile(logDirectory, "20100102", "the quick brown fox");
        addToIndex(logDirectory);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 2 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_log_query_does_not_return_section_separator() throws Exception {
        String update="\n" +
                ">>> time: Fri Jun  3 00:00:01 2011 SYNC UPDATE (78.110.160.234) <<<\n" +
                "inetnum:        10.00.00.00 - 10.00.00.255\n" +
                "\n" +
                ">>> time: Fri Jun  3 00:00:02 2011 SYNC UPDATE (78.110.160.234) <<<\n" +
                "\n" +
                "inetnum: 78.00.00.00 - 78.110.169.223\n" +
                "\n" +
                ">>> time: Fri Jun  3 00:00:03 2011 SYNC UPDATE (78.110.160.234) <<<\n" +
                "\n" +
                "inetnum: 100.00.00.00 - 100.00.00.255\n" +
                "\n";

        LogFileHelper.createBzippedLogFile(logDirectory, "20110603", update);
        addToIndex(logDirectory);

        final String response = getUpdates("78.110.169.223");

        assertThat(response, containsString(">>> time: Fri Jun  3 00:00:02 2011 SYNC UPDATE (78.110.160.234) <<<"));
        assertThat(response, not(containsString(">>> time: Fri Jun  3 00:00:01 2011 SYNC UPDATE (78.110.160.234) <<<")));
        assertThat(response, not(containsString(">>> time: Fri Jun  3 00:00:03 2011 SYNC UPDATE (78.110.160.234) <<<")));
    }

    @Test
    public void legacy_log_directory_no_duplicates() throws Exception {
        LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        addToIndex(logDirectory);
        addToIndex(logDirectory);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void override_is_filtered() throws Exception {
        addToIndex(LogFileHelper.createBzippedLogFile(logDirectory, "20100101",
                "REQUEST FROM:127.0.0.1\n" +
                        "PARAMS:\n" +
                        "DATA=\n" +
                        "\n" +
                        "inet6num:      2001::/64\n" +
                        "source:        RIPE\n" +
                        "override: username,password,remark\n"));

        assertThat(getUpdates("2001::/64"), containsString("override: username, FILTERED, remark\n"));
    }

    private void addToIndex(final File file) throws IOException {
        if (file.isDirectory()) {
            legacyLogFormatProcessor.addDirectoryToIndex(file.getAbsolutePath());
        } else {
            legacyLogFormatProcessor.addFileToIndex(file.getAbsolutePath());
        }
    }
}
