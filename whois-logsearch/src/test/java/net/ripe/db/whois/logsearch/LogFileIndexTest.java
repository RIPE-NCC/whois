package net.ripe.db.whois.logsearch;

import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.logsearch.logformat.DailyLogEntry;
import net.ripe.db.whois.logsearch.logformat.LoggedUpdate;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Ignore
public class LogFileIndexTest {
    @Rule public TemporaryFolder indexFolder = new TemporaryFolder();

    private String logDir;
    private LogFileIndex subject;
    private DateTimeProvider dateTimeProvider;

    @Before
    public void setUp() throws Exception {
        logDir = new ClassPathResource("/log/update").getFile().getAbsolutePath();
        dateTimeProvider = new TestDateTimeProvider();
        final int resultLimit = 100;
//        subject = new LogFileIndex(logDir, legacyLogDir, indexFolder.getRoot().getAbsolutePath(), resultLimit, dateTimeProvider);
        subject.init();
    }

    @Test
    public void check_nr_docs_in_index_after_rebuild() throws IOException, ParseException {
        assertThat(getNrDocuments(subject), is(0));

        subject.rebuild();
        assertThat(getNrDocuments(subject), is(23));              // TODO: is 0

        subject.update();
        assertThat(getNrDocuments(subject), is(23));              // TODO: is 0
    }

    @Test
    public void check_nr_docs_in_index_after_update() throws IOException, ParseException {
        assertThat(getNrDocuments(subject), is(0));

        subject.update();
        assertThat(getNrDocuments(subject), is(23));

        subject.update();
        assertThat(getNrDocuments(subject), is(23));
    }

    @Test
    public void search_contents() throws IOException, ParseException {
        subject.update();

        final Set<? extends LoggedUpdate> loggedUpdateIds = subject.searchByDateRangeAndContent("FAILED", null, null);
        assertThat(loggedUpdateIds, hasSize(11));
        assertThat(loggedUpdateIds, containsInAnyOrder(
                (LoggedUpdate)new DailyLogEntry(logDir + "/20120816.tar/102048.006601cd7b88$0824a380$c87e400a", "20120816"),
                new DailyLogEntry(logDir + "/20120816.tar/062632.006401cd7b67$4c0ada40$c87e400a", "20120816"),
                new DailyLogEntry(logDir + "/20120816.tar/162650.007101cd7bbb$279aa740$c87e400a", "20120816"),
                new DailyLogEntry(logDir + "/20120816.tar/083803.008c01cd7b79$adf9a210$4b45400a", "20120816"),
                new DailyLogEntry(logDir + "/20120816.tar/115733.20555419.71345111048502.JavaMail.mtce0001", "20120816"),
                new DailyLogEntry(logDir + "/20120816.tar/163251.009101cd7bbb$fe81c270$4b45400a", "20120816"),
                new DailyLogEntry(logDir + "/20120816.tar/161005.006b01cd7bb8$d40be280$c87e400a", "20120816"),
                new DailyLogEntry(logDir + "/20130305.tar/114444.1975357211.0.1362480283923.JavaMail.andre", "20130305"),
                new DailyLogEntry(logDir + "/20130305.tar/140319.syncupdate_127.0.0.1_1362488599134839000", "20130305"),
                new DailyLogEntry(logDir + "/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-out.txt.gz", "20130306"),
                new DailyLogEntry(logDir + "/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-out.txt.gz", "20130306")
        ));
    }

    @Test
    public void search_contents_on_date() throws IOException, ParseException {
        subject.update();

        final Set<? extends LoggedUpdate> loggedUpdateIds = subject.searchByDateRangeAndContent("FAILED", new LocalDate(2013, 3, 6), null);
        assertThat(loggedUpdateIds, hasSize(2));
        assertThat(loggedUpdateIds, contains(
                (LoggedUpdate)new DailyLogEntry(logDir + "/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-out.txt.gz", "20130306"),
                new DailyLogEntry(logDir + "/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-out.txt.gz", "20130306")
        ));
    }

    @Test
    public void search_contents_on_date_range() throws IOException, ParseException {
        subject.update();

        final Set<LoggedUpdate> loggedUpdateIds = subject.searchByDateRangeAndContent("FAILED", new LocalDate(2013, 3, 1), new LocalDate(2013, 3, 6));
        assertThat(loggedUpdateIds, hasSize(4));
        assertThat(loggedUpdateIds, containsInAnyOrder(
                (LoggedUpdate)new DailyLogEntry(logDir + "/20130305.tar/114444.1975357211.0.1362480283923.JavaMail.andre", "20130305"),
                new DailyLogEntry(logDir + "/20130305.tar/140319.syncupdate_127.0.0.1_1362488599134839000", "20130305"),
                new DailyLogEntry(logDir + "/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-out.txt.gz", "20130306"),
                new DailyLogEntry(logDir + "/20130306/123623.428054357.0.1362569782886.JavaMail.andre/002.msg-out.txt.gz", "20130306")
        ));
    }

    @Test
    public void search_contents_on_date_range_only_one_day() throws IOException, ParseException {
        subject.update();

        final Set<LoggedUpdate> loggedUpdateIds = subject.searchByDateRangeAndContent("FAILED", new LocalDate(2013, 3, 5), new LocalDate(2013, 3, 5));
        assertThat(loggedUpdateIds, hasSize(2));
        assertThat(loggedUpdateIds, contains(
                (LoggedUpdate)new DailyLogEntry(logDir + "/20130305.tar/114444.1975357211.0.1362480283923.JavaMail.andre", "20130305"),
                new DailyLogEntry(logDir + "/20130305.tar/140319.syncupdate_127.0.0.1_1362488599134839000", "20130305")
        ));
    }

    @Test
    public void search_contents_on_date_range_reversed() throws IOException, ParseException {
        subject.update();

        final Set<? extends LoggedUpdate> loggedUpdateIds = subject.searchByDateRangeAndContent("FAILED", new LocalDate(2013, 3, 6), new LocalDate(2013, 3, 3));
        assertThat(loggedUpdateIds, hasSize(0));
    }

    @Test
    public void search_contents_on_one_date() throws IOException, ParseException {
        subject.update();

        final Set<LoggedUpdate> loggedUpdateIds = subject.searchByDateRangeAndContent("FAILED", null, new LocalDate(2013, 3, 5));
        assertThat(loggedUpdateIds, hasSize(2));
        assertThat(loggedUpdateIds, contains(
                (LoggedUpdate)new DailyLogEntry(logDir + "/20130305.tar/114444.1975357211.0.1362480283923.JavaMail.andre", "20130305"),
                new DailyLogEntry(logDir + "/20130305.tar/140319.syncupdate_127.0.0.1_1362488599134839000", "20130305")
        ));
    }

    private Integer getNrDocuments(final LogFileIndex logFileIndex) throws IOException {
        return logFileIndex.search(new IndexTemplate.SearchCallback<Integer>() {
            @Override
            public Integer search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {
                return indexReader.numDocs();
            }
        });
    }
}
