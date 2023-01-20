package net.ripe.db.whois.update.log;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoggerContextTest {
    @Mock Update update;
    @Mock DateTimeProvider dateTimeProvider;
    @InjectMocks LoggerContext subject;

    @TempDir
    public File folder;


    @BeforeEach
    public void setUp() throws Exception {
        // need to reinit static threadlocal
        try {
            subject.remove();
        } catch (IllegalStateException ignored) {}
        subject.init(folder.getAbsoluteFile());

        lenient().when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
        lenient().when(update.getUpdate()).thenReturn(update);
    }

    @Test
    public void checkDirs() {
        subject.remove();

        final File f = new File(folder.getAbsolutePath(), "test/");
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

        final InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(folder.getAbsolutePath(), "001.test.txt.gz"))));
        final String contents = new String(FileCopyUtils.copyToByteArray(is), StandardCharsets.UTF_8);

        assertThat(file.getName(), is("001.test.txt.gz"));
        assertThat(contents, is("test"));
    }

    @Test
    public void log_throws_exception() {
        assertThrows(IllegalStateException.class, () -> {
            subject.log("filename", new LogCallback() {
                @Override
                public void log(final OutputStream outputStream) throws IOException {
                    throw new IOException();
                }
            });
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

        final InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(folder.getAbsolutePath(), "000.audit.xml.gz"))));
        final String contents = new String(FileCopyUtils.copyToByteArray(is), StandardCharsets.UTF_8);

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

    @Test
    public void logUpdateComplete_no_context_should_fail() {
        assertThrows(IllegalStateException.class, () -> {
            subject.logUpdateCompleted(update);
        });
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

        final InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File(folder.getAbsolutePath(), "000.audit.xml.gz"))));
        final String contents = new String(FileCopyUtils.copyToByteArray(is), StandardCharsets.UTF_8);

        assertThat(contents, containsString("" +
                "            <exception>\n" +
                "                <class>java.lang.NullPointerException</class>\n"));
        assertThat(contents, containsString("" +
                "                <message><![CDATA[null]]></message>\n"));
        assertThat(contents, containsString("" +
                "                <stacktrace><![CDATA[java.lang.NullPointerException\n"));
    }

    @Test
    public void init_with_null_should_not_fail() throws Exception {
        LoggerContext context = new LoggerContext(dateTimeProvider);
        context.remove();
        context.setBaseDir(folder.getAbsolutePath());

        context.init((String) null);

        context.remove();
    }

    @Test
    public void init_filename_too_long() throws Exception {
        LoggerContext context = new LoggerContext(dateTimeProvider);
        context.remove();
        context.setBaseDir(folder.getAbsolutePath());

        context.init(
                "!&!GAAAAAAAAABroW3yuncHTIoNeDX08wSswoAAABgAAAAAAAAAa6Ft8rp3B0yKDXg19LMErCTLPAAAAAAAEAAAAETr6edDTvV" +
                "Lsahq+LasU3W8AAAAIE1FRFQgU3VydmV5IFJlcXVlc3QgZm9yIFNpdGUgU2VsZWN0aW9uIG1hZ2F6aW5lJ3MgIkNhbmFkYSdzI" +
                "EJlc3QgTG9jYXRpb25zIiByYW5raW5ncyBTZXB0ZW1iZXIgMjAxMSAtICggQXdhcmRzIHdpbGwgYmUgUHJlc2VudGVkIGF0IEV" +
                "EQUMgMjAxMSAxMC1xIC0xeC80LCAyMDExIGluIFBldGVyYm9yb3VnaCBPbnRhcjlvKSAgIAA=");
        context.checkDirs();

        assertThat(context.getFile("test").getCanonicalPath(), containsString("MSAt/001.test.gz"));
    }

    @Test
    public void init_filename_illegal_path() throws Exception {
        LoggerContext context = new LoggerContext(dateTimeProvider);
        context.remove();
        context.setBaseDir(folder.getAbsolutePath());

        context.init("/../../../../../../../");
        context.checkDirs();

        assertThat(context.getFile("test").getCanonicalPath(), containsString(".............../001.test.gz"));
    }

    @Test
    public void init_filename_illegal_characters() throws Exception {
        LoggerContext context = new LoggerContext(dateTimeProvider);
        context.remove();
        context.setBaseDir(folder.getAbsolutePath());

        context.init("2001:2002::\t\n\\x0B\f\r//?<>\\*|\"");
        context.checkDirs();

        assertThat(context.getFile("test").getCanonicalPath(), containsString(".2001:2002::x0B/001.test.gz"));
    }
}
