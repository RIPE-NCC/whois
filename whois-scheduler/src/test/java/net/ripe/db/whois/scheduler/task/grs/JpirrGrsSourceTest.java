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
public class JpirrGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;

    JpirrGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new JpirrGrsSource("JPIRR-GRS", "", "http://127.0.0.1/", sourceContext, dateTimeProvider);
    }

    @Test
    public void acquire() throws IOException {
        final String download = "http://dump.test";

        subject = spy(subject);

        doNothing().when(subject).downloadToFile(any(URL.class), any(File.class));

        final File file = File.createTempFile("grs", "test");
        subject.acquireDump(file);

        verify(subject).downloadToFile(new URL(download), file);
    }

    @Test
    public void handleObjects() throws Exception {
        final File file = new File(getClass().getResource("/grs/jpirr.test.gz").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getObjects(), hasSize(0));
        assertThat(objectHandler.getLines(), hasSize(2));
        assertThat(objectHandler.getLines(), contains((List<String>)
                Lists.newArrayList("" +
                        "route:      219.23.0.0/16\n",
                        "descr:      Description\n",
                        "origin:     AS76\n",
                        "mnt-by:     MNT-AS76\n",
                        "changed:    foo@fo.bogus.co.jp 20061016\n",
                        "changed:    foo@fo.bogus.co.jp 20070402\n",
                        "changed:    foo@fo.bogus.co.jp 20070718\n",
                        "source:     JPIRR\n"),

                Lists.newArrayList("" +
                        "as-set:     AS-BOGUS\n",
                        "descr:      Description\n",
                        "members:    AS74\n",
                        "notify:     test@bogus.jp\n",
                        "mnt-by:     MNT-AS18\n",
                        "changed:    test@bogus.jp 20130109\n",
                        "source:     JPIRR\n")
        ));
    }
}
