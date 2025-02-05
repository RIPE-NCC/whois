package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
public class LacnicGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock Downloader downloader;

    LacnicGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @BeforeEach
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new LacnicGrsSource("LACNIC-GRS", sourceContext, dateTimeProvider, authoritativeResourceData, downloader, "", "", "");
    }

    @Test
    public void handleObjects() throws Exception {
        final File file = new File(getClass().getResource("/grs/lacnic.test").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getLines(), hasSize(0));
        assertThat(objectHandler.getObjects(), hasSize(3));
        assertThat(objectHandler.getObjects(), contains(
                RpslObject.parse("" +
                        "aut-num:        AS278\n" +
                        "descr:          FUNDAÇÃO DE AMPARO À PESQUISA DO ESTADO SÃO PAULO\n" +
                        "country:        MX\n" +
                        "created:        19890331\n" +
                        "source:         LACNIC\n"),

                RpslObject.parse("" +
                        "inetnum:        24.232.16/24\n" +
                        "status:         reallocated\n" +
                        "descr:          Description\n" +
                        "country:        AR\n" +
                        "tech-c:\n" +
                        "created:        19990312\n" +
                        "source:         LACNIC\n"),

                RpslObject.parse("" +
                        "inet6num:       2001:1200:2000::/48\n" +
                        "status:         reallocated\n" +
                        "descr:          Description\n" +
                        "country:        MX\n" +
                        "tech-c:         IIM\n" +
                        "abuse-c:        IIM\n" +
                        "created:        20061106\n" +
                        "source:         LACNIC\n")
        ));
    }
}
