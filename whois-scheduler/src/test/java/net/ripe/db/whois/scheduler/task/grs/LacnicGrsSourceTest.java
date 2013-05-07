package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LacnicGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;

    LacnicGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new LacnicGrsSource("LACNIC-GRS", "", sourceContext, dateTimeProvider);
    }

    @Test
    public void handleObjects() throws Exception {
        final File file = new File(getClass().getResource("/grs/lacnic.test").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getLines(), hasSize(0));
        assertThat(objectHandler.getObjects(), hasSize(3));
        assertThat(objectHandler.getObjects(), contains(
                RpslObjectBase.parse("" +
                        "aut-num:        AS278\n" +
                        "descr:          Description\n" +
                        "country:        MX\n" +
                        "changed:        unread@ripe.net 19890331 # created\n" +
                        "changed:        unread@ripe.net 20110503 # changed\n" +
                        "source:         LACNIC\n"),

                RpslObjectBase.parse("" +
                        "inetnum:        24.232.16/24\n" +
                        "status:         reallocated\n" +
                        "descr:          Description\n" +
                        "country:        AR\n" +
                        "tech-c:\n" +
                        "changed:        unread@ripe.net 19990312 # created\n" +
                        "changed:        unread@ripe.net 19990312 # changed\n" +
                        "source:         LACNIC\n"),

                RpslObjectBase.parse("" +
                        "inet6num:       2001:1200:2000::/48\n" +
                        "status:         reallocated\n" +
                        "descr:          Description\n" +
                        "country:        MX\n" +
                        "tech-c:         IIM\n" +
                        "abuse-c:        IIM\n" +
                        "changed:        unread@ripe.net 20061106 # created\n" +
                        "changed:        unread@ripe.net 20061106 # changed\n" +
                        "source:         LACNIC\n")
        ));
    }
}
