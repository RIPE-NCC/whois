package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectTemplateDependentTest;
import net.ripe.db.whois.common.rpsl.ObjectTemplateProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LacnicGrsSourceWithoutChangedTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock Downloader downloader;

    LacnicGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @BeforeClass
    public static void beforeClass() throws Exception {
        new ObjectTemplateProvider(new ChangedAttrFeatureToggle(false));
    }

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new LacnicGrsSource("LACNIC-GRS", sourceContext, dateTimeProvider, authoritativeResourceData, downloader, "", "");
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
                        "descr:          Description\n" +
                        "country:        MX\n" +
                        "source:         LACNIC\n"),

                RpslObject.parse("" +
                        "inetnum:        24.232.16/24\n" +
                        "status:         reallocated\n" +
                        "descr:          Description\n" +
                        "country:        AR\n" +
                        "tech-c:\n" +
                        "source:         LACNIC\n"),

                RpslObject.parse("" +
                        "inet6num:       2001:1200:2000::/48\n" +
                        "status:         reallocated\n" +
                        "descr:          Description\n" +
                        "country:        MX\n" +
                        "tech-c:         IIM\n" +
                        "abuse-c:        IIM\n" +
                        "source:         LACNIC\n")
        ));
    }
}
