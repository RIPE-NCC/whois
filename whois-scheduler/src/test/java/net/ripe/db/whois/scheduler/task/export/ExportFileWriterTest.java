package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExportFileWriterTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock FilenameStrategy filenameStrategy;
    @Mock DecorationStrategy decorationStrategy;
    @Mock ExportFilter exportFilter;

    ExportFileWriter subject;

    @Before
    public void setUp() throws Exception {
        when(filenameStrategy.getFilename(any(ObjectType.class))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return ((ObjectType) invocation.getArguments()[0]).getName();
            }
        });

        when(decorationStrategy.decorate(any(RpslObject.class))).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return (RpslObject) invocation.getArguments()[0];
            }
        });

        when(exportFilter.shouldExport(any(RpslObject.class))).thenReturn(true);

        subject = new ExportFileWriter(folder.getRoot(), filenameStrategy, decorationStrategy, exportFilter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void write() throws IOException {
        subject.write(RpslObject.parse("mntner: DEV-MNT1"), Collections.EMPTY_LIST);
        subject.write(RpslObject.parse("mntner: DEV-MNT2"), Collections.EMPTY_LIST);
        subject.write(RpslObject.parse("mntner: DEV-MNT3"), Collections.EMPTY_LIST);
        subject.write(RpslObject.parse("mntner: DEV-MNT4"), Collections.EMPTY_LIST);
        subject.write(RpslObject.parse("inetnum: 193.0.0.0 - 193.0.0.10"), Collections.EMPTY_LIST);
        subject.write(RpslObject.parse("route: 193.0.0.0 - 193.0.0.10\norigin: AS12"), Lists.newArrayList(new Tag(CIString.ciString("foo"), 3, "bar")));
        subject.close();

        final File[] files = folder.getRoot().listFiles();
        Assert.assertNotNull(files);
        Assert.assertThat(files.length, Matchers.is(21));

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
                        "origin:         AS12\n\n" +
                        QueryMessages.tagInfoStart("193.0.0.0 - 193.0.0.10AS12") +
                        QueryMessages.tagInfo("foo", "bar"));
            }
        }
    }

    private void checkFile(final File file, final String expectedContents) throws IOException {
        final String content = FileCopyUtils.copyToString(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), Charsets.ISO_8859_1));
        Assert.assertThat(content, Matchers.is(QueryMessages.termsAndConditionsDump() + "\n" + expectedContents));
    }

    @Test(expected = RuntimeException.class)
    public void unexisting_folder() throws IOException {
        new ExportFileWriter(new File(folder.getRoot().getAbsolutePath() + "does not exist"), filenameStrategy, decorationStrategy, exportFilter);
    }
}
