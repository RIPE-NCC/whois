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
public class ApnicGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;

    ApnicGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new ApnicGrsSource("APNIC-GRS", "", sourceContext, dateTimeProvider);
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
        final File file = new File(getClass().getResource("/grs/apnic.test.gz").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getObjects(), hasSize(0));
        assertThat(objectHandler.getLines(), hasSize(2));
        assertThat(objectHandler.getLines(), contains((List<String>)
                Lists.newArrayList(
                        "as-block:     AS7467 - AS7722\n",
                        "descr:        APNIC ASN block\n",
                        "remarks:      These AS numbers are further assigned by APNIC\n",
                        "remarks:      to APNIC members and end-users in the APNIC region\n",
                        "admin-c:      HM20-AP\n",
                        "tech-c:       HM20-AP\n",
                        "mnt-by:       APNIC-HM\n",
                        "mnt-lower:    APNIC-HM\n",
                        "changed:      hm-changed@apnic.net 20020926\n",
                        "source:       APNIC\n"),

                Lists.newArrayList(
                        "as-block:     AS18410 - AS18429\n",
                        "descr:        TWNIC-TW-AS-BLOCK8\n",
                        "remarks:      These AS numbers are further assigned by TWNIC\n",
                        "remarks:      to TWNIC members\n",
                        "admin-c:      TWA2-AP\n",
                        "tech-c:       TWA2-AP\n",
                        "mnt-by:       MAINT-TW-TWNIC\n",
                        "mnt-lower:    MAINT-TW-TWNIC\n",
                        "changed:      hm-changed@apnic.net 20021220\n",
                        "changed:      hostmaster@twnic.net.tw 20050624\n",
                        "source:       APNIC\n")
        ));
    }
}
