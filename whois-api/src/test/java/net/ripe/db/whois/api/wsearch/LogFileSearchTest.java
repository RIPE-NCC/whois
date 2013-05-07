package net.ripe.db.whois.api.wsearch;

import com.google.common.collect.Sets;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogFileSearchTest {
    LoggedUpdateId loggedUpdateWithPasswordId;
    LoggedUpdateId loggedUpdateWithOverrideId;

    @Mock LogFileIndex logFileIndex;
    LogFileSearch subject;

    @Before
    public void setUp() throws Exception {
        loggedUpdateWithPasswordId = new LoggedUpdateId("20130306", "123623.428054357.0.1362569782886.JavaMail.andre");
        loggedUpdateWithOverrideId = new LoggedUpdateId("20130306", "123624.428054357.0.1362569782886.JavaMail.andre");

        subject = new LogFileSearch(new ClassPathResource("/log/update").getFile().getAbsolutePath(), logFileIndex);
    }

    @Test
    public void searchLoggedUpdateIds() throws IOException, ParseException {
        when(logFileIndex.searchLoggedUpdateIds("query", null)).thenReturn(Sets.newHashSet(loggedUpdateWithPasswordId));

        final Set<LoggedUpdateId> updateIds = subject.searchLoggedUpdateIds("query", null);
        assertThat(updateIds, contains(loggedUpdateWithPasswordId));
    }

    @Test
    public void writeLoggedUpdates() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.writeLoggedUpdates(loggedUpdateWithPasswordId, writer);

        final String output = writer.toString();
        assertThat(output, containsString("date     : 20130306"));
        assertThat(output, containsString("type     : UPDATE"));
        assertThat(output, containsString("type     : ACK"));

        assertThat(output, containsString("as-block:       AS222 - AS333\n"));
    }

    @Test
    public void writeLoggedUpdates_filters_password() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.writeLoggedUpdates(loggedUpdateWithPasswordId, writer);

        final String output = writer.toString();
        assertThat(output, containsString("password: FILTERED"));
    }

    @Test
    public void writeLoggedUpdates_filters_override() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.writeLoggedUpdates(loggedUpdateWithOverrideId, writer);

        final String output = writer.toString();
        assertThat(output, containsString("override: FILTERED"));
    }

}
