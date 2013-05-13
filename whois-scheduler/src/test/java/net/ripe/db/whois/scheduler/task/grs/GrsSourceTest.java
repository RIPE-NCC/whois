package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock GrsDao grsDao;
    GrsSource subject;

    @Before
    public void setUp() throws Exception {
        subject = new GrsSource("SOME-GRS", "", sourceContext, dateTimeProvider) {
            @Override
            void acquireDump(File file) throws IOException {
            }

            @Override
            void handleObjects(File file, ObjectHandler handler) throws IOException {
            }
        };
        subject.setDao(grsDao);
    }

    @Test
    public void downloadToFile_url() throws IOException {
        final URL resource = getClass().getResource("/TEST.db");
        final File file = File.createTempFile("grs", "test");

        subject.downloadToFile(resource, file);
    }

    @Test
    public void downloadToFile() throws IOException {
        final File file = File.createTempFile("grs", "test");
        final InputStream is = new ReaderInputStream(new StringReader("test"));

        subject.downloadToFile(is, file);
    }

    @Test(expected = IllegalStateException.class)
    public void downloadToFile_empty_file() throws IOException {
        final File file = File.createTempFile("grs", "test");
        final InputStream is = new ReaderInputStream(new StringReader(""));

        subject.downloadToFile(is, file);
    }

    @Test
    public void downloadToFile_file_already_exists() throws IOException {
        final File file = File.createTempFile("grs", "test");
        file.createNewFile();

        final InputStream is = new ReaderInputStream(new StringReader("overwrite"));
        subject.downloadToFile(is, file);
    }

    @Test
    public void string() {
        assertThat(subject.toString(), is("SOME-GRS"));
    }

    @Test
    public void handleLines_empty() throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader(""));
        final GrsSource.LineHandler lineHandler = mock(GrsSource.LineHandler.class);

        subject.handleLines(reader, lineHandler);

        verifyZeroInteractions(lineHandler);
    }

    @Test
    public void handleLines_throws_exception() throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader("line1\n\nline2"));
        final GrsSource.LineHandler lineHandler = mock(GrsSource.LineHandler.class);

        doThrow(NullPointerException.class).when(lineHandler).handleLines(anyList());
        subject.handleLines(reader, lineHandler);

        verify(lineHandler).handleLines(Lists.newArrayList("line1\n"));
        verify(lineHandler).handleLines(Lists.newArrayList("line2\n"));
    }

    @Test
    public void handleLines_start_of_line_comment() throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader("line1\n#line2\nline3"));
        final GrsSource.LineHandler lineHandler = mock(GrsSource.LineHandler.class);

        subject.handleLines(reader, lineHandler);

        verify(lineHandler).handleLines(Lists.newArrayList("line1\n", "line3\n"));
        verifyNoMoreInteractions(lineHandler);
    }

    @Test
    public void handleLines_multiple_newlines() throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader("line1\n\n\n\n\n\nline2\n\n\n\n\n"));
        final GrsSource.LineHandler lineHandler = mock(GrsSource.LineHandler.class);

        subject.handleLines(reader, lineHandler);

        verify(lineHandler).handleLines(Lists.newArrayList("line1\n"));
        verify(lineHandler).handleLines(Lists.newArrayList("line2\n"));
        verifyNoMoreInteractions(lineHandler);
    }

    @Test
    public void handleLines_rpsl_object() throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader("" +
                "person:         John Smith\n" +
                "address:        Example LTD\n" +
                "                High street 12\n" +
                "                St.Mery Mead\n" +
                "                Essex, UK\n" +
                "phone:          +44 1737 892 004\n" +
                "e-mail:         john.smith@example.com\n" +
                "nic-hdl:        JS1-TEST\n" +
                "remarks:        *******************************\n" +
                "remarks:        This object is only an example!\n" +
                "remarks:        *******************************\n" +
                "mnt-by:         EXAMPLE-MNT\n" +
                "abuse-mailbox:  abuse@example.com\n" +
                "changed:        john.smith@example.com 20051104\n" +
                "changed:        john.smith@example.com 20051105\n" +
                "source:         TEST\n" +
                "\n" +
                "mntner:         TEST-ROOT-MNT\n"));


        final List<List<String>> handledLines = Lists.newArrayList();
        subject.handleLines(reader, new GrsSource.LineHandler() {
            @Override
            public void handleLines(final List<String> lines) {
                handledLines.add(lines);
            }
        });

        assertThat(handledLines, hasSize(2));
        assertThat(handledLines.get(0), is((List<String>) Lists.newArrayList(
                "person:         John Smith\n", "" +
                "address:        Example LTD\n" +
                "                High street 12\n" +
                "                St.Mery Mead\n" +
                "                Essex, UK\n",
                "phone:          +44 1737 892 004\n",
                "e-mail:         john.smith@example.com\n",
                "nic-hdl:        JS1-TEST\n",
                "remarks:        *******************************\n",
                "remarks:        This object is only an example!\n",
                "remarks:        *******************************\n",
                "mnt-by:         EXAMPLE-MNT\n",
                "abuse-mailbox:  abuse@example.com\n",
                "changed:        john.smith@example.com 20051104\n",
                "changed:        john.smith@example.com 20051105\n",
                "source:         TEST\n")));

        assertThat(handledLines.get(1), is((List<String>) Lists.newArrayList(
                "mntner:         TEST-ROOT-MNT\n")));
    }

    @Test
    public void checkMD5_valid() throws IOException {
        final Resource resource = new ClassPathResource(String.format("grs/%s.gz", "delegated-ripencc-extended-latest"));
        GZIPInputStream gzipInputStream = null;

        try {
            gzipInputStream = new GZIPInputStream(resource.getInputStream());
            subject.checkMD5(gzipInputStream, new ReaderInputStream(new StringReader("MD5 (delegated-ripencc-extended-latest) = 8794b2c7a9d2e128f4530c67230b7f69")));
        } finally {
            IOUtils.closeQuietly(gzipInputStream);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMD5_invalid() throws IOException {
        final Resource resource = new ClassPathResource(String.format("grs/%s.gz", "delegated-apnic-extended-latest"));
        GZIPInputStream gzipInputStream = null;

        try {
            gzipInputStream = new GZIPInputStream(resource.getInputStream());
            subject.checkMD5(gzipInputStream, new ReaderInputStream(new StringReader("MD5 (delegated-ripencc-extended-latest) = 8794b2c7a9d2e128f4530c67230b7f69")));
        } finally {
            IOUtils.closeQuietly(gzipInputStream);
        }
    }
}
