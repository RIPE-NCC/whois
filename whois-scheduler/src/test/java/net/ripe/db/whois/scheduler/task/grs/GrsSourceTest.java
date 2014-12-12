package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock GrsDao grsDao;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock Downloader downloader;
    GrsSource subject;

    @Before
    public void setUp() throws Exception {
        subject = new GrsSource("SOME-GRS", sourceContext, dateTimeProvider, authoritativeResourceData, downloader) {
            @Override
            void acquireDump(Path path) throws IOException {
            }

            @Override
            void handleObjects(File file, ObjectHandler handler) throws IOException {
            }
        };
        subject.setDao(grsDao);
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

        doThrow(NullPointerException.class).when(lineHandler).handleLines(anyListOf(String.class));
        subject.handleLines(reader, lineHandler);

        verify(lineHandler).handleLines(Lists.newArrayList("line1\n"));
        verify(lineHandler).handleLines(Lists.newArrayList("line2\n"));
    }

    @Test
    public void handleLines_start_of_line_comment() throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader("line1\n#line2\nline3\n%line4\nline5"));
        final GrsSource.LineHandler lineHandler = mock(GrsSource.LineHandler.class);

        subject.handleLines(reader, lineHandler);

        verify(lineHandler).handleLines(Lists.newArrayList("line1\n", "line3\n", "line5\n"));
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
}
