package net.ripe.db.whois.logsearch;

import com.google.common.collect.Sets;
import net.ripe.db.whois.logsearch.logformat.DailyLogEntry;
import net.ripe.db.whois.logsearch.logformat.LoggedUpdate;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogFileSearchTest {
    LoggedUpdate loggedUpdateWithPasswordId;
    LoggedUpdate loggedUpdateWithOverride;

    @Mock LogFileIndex logFileIndex;
    LogFileSearch subject;

    @Before
    public void setUp() throws Exception {
        final String logDir = new ClassPathResource("/log/update").getFile().getAbsolutePath();
        loggedUpdateWithPasswordId = new DailyLogEntry(logDir + "/20130306/123623.428054357.0.1362569782886.JavaMail.andre/001.msg-in.txt.gz", "20130306");
        loggedUpdateWithOverride = new DailyLogEntry(logDir + "/20130306/123624.428054357.0.1362569782886.JavaMail.andre/001.msg-in.txt.gz", "20130306");

        subject = new LogFileSearch(new ClassPathResource("/log/update").getFile().getAbsolutePath(), logFileIndex);
    }

    @Test
    public void searchLoggedUpdateIds() throws IOException, ParseException {
        when(logFileIndex.searchByDateRangeAndContent("query", null, null)).thenReturn(Sets.newHashSet(loggedUpdateWithPasswordId));

        final Set<LoggedUpdate> updateIds = subject.searchLoggedUpdateIds("query", null, null);
        assertThat(updateIds, contains(loggedUpdateWithPasswordId));
    }

    @Test
    public void writeLoggedUpdates() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.writeLoggedUpdate(loggedUpdateWithPasswordId, writer);

        final String output = writer.toString();
        assertThat(output, containsString("date     : 20130306"));

        assertThat(output, containsString("as-block:       AS222 - AS333\n"));
    }

    @Test
    public void writeLoggedUpdates_filters_password() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.writeLoggedUpdate(loggedUpdateWithPasswordId, writer);

        final String output = writer.toString();
        assertThat(output, containsString("password: FILTERED"));
        assertThat(output, not(containsString("dbm")));
    }

    @Test
    public void writeLoggedUpdates_filters_override() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.writeLoggedUpdate(loggedUpdateWithOverride, writer);

        final String output = writer.toString();
        assertThat(output, containsString("override: FILTERED"));
        assertThat(output, containsString("override: dbm2, FILTERED, reason"));
        assertThat(output, containsString("override: dbm3, FILTERED"));
        assertThat(output, not(containsString("password")));
    }
}
