package net.ripe.db.whois.nrtm.client;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.nrtm.NrtmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringValueResolver;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NrtmImporterTest {

    @Mock private NrtmClientFactory nrtmClientFactory;
    @Mock private SourceContext sourceContext;
    @Mock private StringValueResolver valueResolver;
    private NrtmImporter subject;

    @BeforeEach
    public void setup() {
        final boolean enabled = true;
        final String sources = "1-GRS";
        subject = new NrtmImporter(nrtmClientFactory, sourceContext, enabled, sources);
        subject.setEmbeddedValueResolver(valueResolver);
    }

    @Test
    public void invalid_source() {
        assertThrows(NrtmException.class, () -> {
            when(sourceContext.isVirtual(CIString.ciString("1-GRS"))).thenReturn(true);

            subject.checkSources();
        });

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
