package net.ripe.db.whois.nrtm.client;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StringValueResolver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NrtmImporterTest {

    @Mock private NrtmClientFactory nrtmClientFactory;
    @Mock private SourceContext sourceContext;
    @Mock private StringValueResolver valueResolver;
    private NrtmImporter subject;

    @Before
    public void setup() {
        final boolean enabled = true;
        final String sources = "1-GRS";
        subject = new NrtmImporter(nrtmClientFactory, sourceContext, enabled, sources);
        subject.setEmbeddedValueResolver(valueResolver);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_source() {
        when(sourceContext.isVirtual(CIString.ciString("1-GRS"))).thenReturn(true);

        subject.checkSources();
    }

    @Test
    public void start() {
        when(valueResolver.resolveStringValue("${nrtm.import.1-GRS.source}")).thenReturn("RIPE");
        when(valueResolver.resolveStringValue("${nrtm.import.1-GRS.host}")).thenReturn("localhost");
        when(valueResolver.resolveStringValue("${nrtm.import.1-GRS.port}")).thenReturn("1044");
        when(nrtmClientFactory.createNrtmClient(any(NrtmSource.class))).thenReturn(mock(NrtmClientFactory.NrtmClient.class));

        subject.start();

        verify(nrtmClientFactory).createNrtmClient(any(NrtmSource.class));
    }

    @Test
    public void start_not_enabled() {
        final boolean enabled = false;
        final String sources = "";
        subject = new NrtmImporter(nrtmClientFactory, sourceContext, enabled, sources);

        verify(nrtmClientFactory, never()).createNrtmClient(any(NrtmSource.class));
    }
}
