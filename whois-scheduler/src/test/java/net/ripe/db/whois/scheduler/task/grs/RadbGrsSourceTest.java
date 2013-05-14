package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RadbGrsSourceTest {
    private static final String download = "http://127.0.0.1/";

    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;

    RadbGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new RadbGrsSource("RADB-GRS", "", download, sourceContext, dateTimeProvider);
    }

    @Test
    public void acquire() throws IOException {
        subject = spy(subject);

        doNothing().when(subject).downloadToFile(any(URL.class), any(File.class));

        final File file = File.createTempFile("grs", "test");
        subject.acquireDump(file);

        verify(subject).downloadToFile(new URL(download), file);
    }

    @Test
    public void handleObjects() throws Exception {
        final File file = new File(getClass().getResource("/grs/radb.test.gz").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getObjects(), hasSize(0));
        assertThat(objectHandler.getLines(), hasSize(2));
        assertThat(objectHandler.getLines(), contains((List<String>)
                Lists.newArrayList(
                        "aut-num:       AS1263\n",
                        "as-name:       TEST-AS\n",
                        "descr:         TEST-AS\n",
                        "admin-c:       Not available\n",
                        "tech-c:        See TEST-MNT\n",
                        "mnt-by:        TEST-MNT\n",
                        "changed:       test@foo.edu 19950201\n",
                        "source:        RADB\n"),

                Lists.newArrayList(
                        "route:         167.96.0.0/16\n", "" +
                        "descr:         Company 2\n" +
                        "               Address 2\n" +
                        "               Postcode\n" +
                        "               State\n" +
                        "               Country\n",
                        "origin:        AS2900\n",
                        "member-of:     RS-TEST\n",
                        "mnt-by:        TEST-MNT\n",
                        "changed:       test@bar.net 19950506\n",
                        "source:        RADB\n")
        ));
    }
}
