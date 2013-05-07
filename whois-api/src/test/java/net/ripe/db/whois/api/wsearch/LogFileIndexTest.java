package net.ripe.db.whois.api.wsearch;

import net.ripe.db.whois.api.search.IndexTemplate;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LogFileIndexTest {
    @Rule public TemporaryFolder indexFolder = new TemporaryFolder();

    String logDir;
    private LogFileIndex subject;

    @Before
    public void setUp() throws Exception {
        logDir = new ClassPathResource("/log/update").getFile().getAbsolutePath();

        subject = new LogFileIndex(logDir, indexFolder.getRoot().getAbsolutePath());
        subject.init();
    }

    @Test
    public void check_nr_docs_in_index_after_rebuild() throws IOException, ParseException {
        assertThat(getNrDocuments(subject), is(0));

        subject.rebuild();
        assertThat(getNrDocuments(subject), is(23));

        subject.update();
        assertThat(getNrDocuments(subject), is(23));
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

        final Set<LoggedUpdateId> loggedUpdateIds = subject.searchLoggedUpdateIds("FAILED", null);
        assertThat(loggedUpdateIds, hasSize(11));
        assertThat(loggedUpdateIds, contains(
                new LoggedUpdateId("20120816", "102048.006601cd7b88$0824a380$c87e400a"),
                new LoggedUpdateId("20120816", "062632.006401cd7b67$4c0ada40$c87e400a"),
                new LoggedUpdateId("20120816", "162650.007101cd7bbb$279aa740$c87e400a"),
                new LoggedUpdateId("20120816", "083803.008c01cd7b79$adf9a210$4b45400a"),
                new LoggedUpdateId("20120816", "115733.20555419.71345111048502.JavaMail.mtce0001"),
                new LoggedUpdateId("20120816", "163251.009101cd7bbb$fe81c270$4b45400a"),
                new LoggedUpdateId("20120816", "161005.006b01cd7bb8$d40be280$c87e400a"),
                new LoggedUpdateId("20130305", "114444.1975357211.0.1362480283923.JavaMail.andre"),
                new LoggedUpdateId("20130305", "140319.syncupdate_127.0.0.1_1362488599134839000"),
                new LoggedUpdateId("20130306", "123623.428054357.0.1362569782886.JavaMail.andre"),
                new LoggedUpdateId("20130306", "123624.428054357.0.1362569782886.JavaMail.andre")
        ));
    }

    @Test
    public void search_contents_on_date() throws IOException, ParseException {
        subject.update();

        final Set<LoggedUpdateId> loggedUpdateIds = subject.searchLoggedUpdateIds("FAILED", new LocalDate(2013, 3, 6));
        assertThat(loggedUpdateIds, hasSize(2));
        assertThat(loggedUpdateIds, contains(
                new LoggedUpdateId("20130306", "123623.428054357.0.1362569782886.JavaMail.andre"),
                new LoggedUpdateId("20130306", "123624.428054357.0.1362569782886.JavaMail.andre")
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
