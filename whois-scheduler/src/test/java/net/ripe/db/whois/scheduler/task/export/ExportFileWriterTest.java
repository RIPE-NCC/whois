package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExportFileWriterTest {
    @TempDir
    public File folder;

    @Mock FilenameStrategy filenameStrategy;
    @Mock DecorationStrategy decorationStrategy;
    @Mock ExportFilter exportFilter;

    ExportFileWriter subject;

    @BeforeEach
    public void setUp() throws Exception {
        when(filenameStrategy.getFilename(any(ObjectType.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return ((ObjectType) invocation.getArguments()[0]).getName();
            }
        });

        subject = new ExportFileWriter(folder.getAbsoluteFile(), filenameStrategy, decorationStrategy, exportFilter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void write() throws IOException {
        when(decorationStrategy.decorate(any(RpslObject.class))).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return (RpslObject) invocation.getArguments()[0];
            }
        });
        when(exportFilter.shouldExport(any(RpslObject.class))).thenReturn(true);

        subject.write(RpslObject.parse("mntner: DEV-MNT1"));
        subject.write(RpslObject.parse("mntner: DEV-MNT2"));
        subject.write(RpslObject.parse("mntner: DEV-MNT3"));
        subject.write(RpslObject.parse("mntner: DEV-MNT4"));
        subject.write(RpslObject.parse("inetnum: 193.0.0.0 - 193.0.0.10"));
        subject.write(RpslObject.parse("route: 193.0.0.0 - 193.0.0.10\norigin: AS12"));
        subject.close();

        final File[] files = folder.listFiles();
        assertThat(files, is(not(nullValue())));
        assertThat(files.length, is(21));

        for (final File file : files) {
            final String fileName = file.getName();
            if (fileName.endsWith("mntner.gz")) {
                checkFile(file, "" +
                        "mntner:         DEV-MNT1\n" +
                        "\n" +
                        "mntner:         DEV-MNT2\n" +
                        "\n" +
                        "mntner:         DEV-MNT3\n" +
                        "\n" +
                        "mntner:         DEV-MNT4\n");
            } else if (fileName.endsWith("inetnum.gz")) {
                checkFile(file, "" +
                        "inetnum:        193.0.0.0 - 193.0.0.10\n");
            } else if (fileName.endsWith("route.gz")) {
                checkFile(file, "" +
                        "route:          193.0.0.0 - 193.0.0.10\n" +
                        "origin:         AS12\n");
            }
        }
    }

    private void checkFile(final File file, final String expectedContents) throws IOException {
        final String content = FileCopyUtils.copyToString(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), StandardCharsets.ISO_8859_1));

        assertThat(content, is(QueryMessages.termsAndConditionsDump() + "\n" + expectedContents));
    }

    @Test
    public void unexisting_folder() {
        assertThrows(RuntimeException.class, () -> {
            new ExportFileWriter(new File(folder.getAbsolutePath() + "does not exist"), filenameStrategy, decorationStrategy, exportFilter);

        });
    }
}
