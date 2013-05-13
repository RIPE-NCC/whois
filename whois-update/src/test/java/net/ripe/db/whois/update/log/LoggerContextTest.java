package net.ripe.db.whois.update.log;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoggerContextTest {
    @Mock Update update;
    @Mock DateTimeProvider dateTimeProvider;
    @InjectMocks LoggerContext subject;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        subject.init(folder.getRoot());

        when(dateTimeProvider.getCurrentDateTime()).thenReturn(new LocalDateTime());
        when(update.getUpdate()).thenReturn(update);
    }

    @Test
    public void checkDirs() {
        subject.remove();

        final File f = new File(folder.getRoot(), "test/");
        subject.setBaseDir(f.getAbsolutePath());
        subject.checkDirs();
        subject.init("folder");

        assertThat(f.exists(), is(true));
    }

    @Test
    public void getFile() throws Exception {
        assertThat(subject.getFile("test.txt").getName(), is("001.test.txt.gz"));
        assertThat(subject.getFile("test.txt").getName(), is("002.test.txt.gz"));
        assertThat(subject.getFile("test.txt").getName(), is("003.test.txt.gz"));
    }

    @Test
    public void log() throws Exception {
        final File file = subject.log("test.txt", new LogCallback() {
            @Override
            public void log(final OutputStream outputStream) throws IOException {
                outputStream.write("test".getBytes());
            }
        });

        final InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(folder.getRoot(), "001.test.txt.gz"))));
        final String contents = new String(FileCopyUtils.copyToByteArray(is), Charsets.UTF_8);

        assertThat(file.getName(), is("001.test.txt.gz"));
        assertThat(contents, is("test"));
    }

    @Test(expected = IllegalStateException.class)
    public void log_throws_exception() {
        subject.log("filename", new LogCallback() {
            @Override
            public void log(final OutputStream outputStream) throws IOException {
                throw new IOException();
            }
        });
    }

    @Test
    public void log_update() throws IOException {
        final String content = "mntner: DEV-ROOT-MNT";
        final RpslObject object = RpslObject.parse(content);

        when(update.getOperation()).thenReturn(Operation.DELETE);
        when(update.getParagraph()).thenReturn(new Paragraph(content));
        when(update.getSubmittedObject()).thenReturn(object);

        subject.logUpdateStarted(update);
        subject.logQuery(new StatementInfo("sql"), new ResultInfo(Collections.<List<String>>emptyList()));
        subject.logUpdateCompleted(update);
        subject.remove();

        final InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(folder.getRoot(), "000.audit.xml.gz"))));
        final String contents = new String(FileCopyUtils.copyToByteArray(is), Charsets.UTF_8);

        assertThat(contents, containsString("" +
                "            <key>[mntner] DEV-ROOT-MNT</key>\n" +
                "            <operation>DELETE</operation>\n" +
                "            <reason/>\n" +
                "            <paragraph><![CDATA[mntner: DEV-ROOT-MNT]]></paragraph>\n" +
                "            <object><![CDATA[mntner:         DEV-ROOT-MNT\n"));

        assertThat(contents, containsString("" +
                "            <query>\n" +
                "                <sql><![CDATA[sql]]></sql>\n" +
                "                <params/>\n" +
                "                <results/>\n" +
                "            </query>\n"));
    }

    @Test
    public void log_query_no_context_should_not_fail() {
        subject.logQuery(new StatementInfo("sql"), new ResultInfo(Collections.<List<String>>emptyList()));
    }

    @Test(expected = IllegalStateException.class)
    public void logUpdateComplete_no_context_should_fail() {
        subject.logUpdateCompleted(update);
    }

    @Test
    public void logException() throws IOException {
        final String content = "mntner: DEV-ROOT-MNT";
        final RpslObject object = RpslObject.parse(content);

        when(update.getOperation()).thenReturn(Operation.DELETE);
        when(update.getParagraph()).thenReturn(new Paragraph(content));
        when(update.getSubmittedObject()).thenReturn(object);

        subject.logUpdateStarted(update);
        subject.logUpdateFailed(update, new NullPointerException());
        subject.remove();

        final InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(folder.getRoot(), "000.audit.xml.gz"))));
        final String contents = new String(FileCopyUtils.copyToByteArray(is), Charsets.UTF_8);

        assertThat(contents, containsString("" +
                "            <exception>\n" +
                "                <class>java.lang.NullPointerException</class>\n" +
                "                <message><![CDATA[null]]></message>\n" +
                "                <stacktrace><![CDATA[java.lang.NullPointerException\n"));
    }

    @Test
    public void init_with_null_should_not_fail() throws Exception {
        LoggerContext context = new LoggerContext(dateTimeProvider);
        context.setBaseDir(folder.getRoot().getCanonicalPath());

        context.init((String) null);

        context.remove();
    }
}
