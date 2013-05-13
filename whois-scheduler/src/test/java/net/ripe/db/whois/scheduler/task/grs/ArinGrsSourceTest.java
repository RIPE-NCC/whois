package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.jdbc.DataSourceFactory;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArinGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DataSourceFactory dataSourceFactory;
    @Mock DateTimeProvider dateTimeProvider;

    ArinGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new ArinGrsSource("ARIN-GRS", "", sourceContext, dateTimeProvider);
        subject.setZipEntryName("arin_db.txt");
    }

    @Test
    public void acquire() throws IOException {
        final String download = "http://dump.test";

        subject = spy(subject);
        subject.setDownload(download);

        doNothing().when(subject).downloadToFile(any(URL.class), any(File.class));

        final File file = File.createTempFile("grs", "test");
        subject.acquireDump(file);

        verify(subject).downloadToFile(new URL(download), file);
    }

    @Test
    public void handleObjects() throws Exception {
        final File file = new File(getClass().getResource("/grs/arin.test.zip").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getLines(), hasSize(0));
        assertThat(objectHandler.getObjects(), hasSize(4));
        assertThat(objectHandler.getObjects(), contains(
                RpslObjectBase.parse("" +
                        "aut-num:        AS0\n" +
                        "org:            IANA\n" +
                        "as-name:        IANA-RSVD-0\n" +
                        "remarks:        Reserved - May be used to identify non-routed networks\n" +
                        "changed:        unread@ripe.net 20020913\n" +
                        "source:         ARIN\n"),

                RpslObjectBase.parse("" +
                        "inetnum:        192.104.33.0 - 192.104.33.255\n" +
                        "org:            THESPI\n" +
                        "netname:        SPINK\n" +
                        "status:         assignment\n" +
                        "changed:        unread@ripe.net 19910409\n" +
                        "source:         ARIN\n"),

                RpslObjectBase.parse("" +
                        "inet6num:       2001:4d0::/32\n" +
                        "org:            NASA\n" +
                        "netname:        NASA-PCCA-V6\n" +
                        "status:         allocation\n" +
                        "changed:        unread@ripe.net 20021114\n" +
                        "tech-c:         ZN7-ARIN\n" +
                        "source:         ARIN\n"),

                RpslObjectBase.parse("" +
                        "inet6num:       2001:468:400::/40\n" +
                        "org:            V6IU\n" +
                        "netname:        ABILENE-IU-V6\n" +
                        "status:         reallocation\n" +
                        "changed:        unread@ripe.net 20020531\n" +
                        "tech-c:         BS69-ARIN\n" +
                        "source:         ARIN\n")
        ));
    }
}
