package net.ripe.db.whois.scheduler.task.grs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GrsImporterJmxTest {
    @Mock GrsImporter grsImporter;
    @InjectMocks GrsImporterJmx subject;

    @Before
    public void setUp() throws Exception {
        subject.setGrsDefaultSources("ARIN-GRS,APNIC-GRS");
    }

    @Test
    public void getGrsDefaultSources() {
        final String defaultSources = subject.getGrsDefaultSources();
        assertThat(defaultSources, is("ARIN-GRS,APNIC-GRS"));
    }

    @Test
    public void grsImport() {
        final String result = subject.grsImport("ARIN-GRS,APNIC-GRS", "comment");

        verify(grsImporter).grsImport("ARIN-GRS,APNIC-GRS", false);
        assertThat(result, is("GRS import started"));
    }

    @Test
    public void grsImport_throws_exception() {
        doThrow(new RuntimeException("Oops")).when(grsImporter).grsImport(anyString(), anyBoolean());
        final String result = subject.grsImport("ARIN-GRS,APNIC-GRS", "comment");

        verify(grsImporter).grsImport("ARIN-GRS,APNIC-GRS", false);
        assertNull(result);
    }

    @Test
    public void grsRebuild() {
        final String result = subject.grsRebuild("ARIN-GRS,APNIC-GRS", "grsrebuildnow", "comment");

        verify(grsImporter).grsImport("ARIN-GRS,APNIC-GRS", true);
        assertThat(result, is("GRS rebuild started"));
    }

    @Test
    public void grsRebuild_invalid_passphrase() {
        final String result = subject.grsRebuild("ARIN-GRS,APNIC-GRS", "??", "comment");

        assertThat(result, containsString("passphrase: grsrebuildnow"));
    }

    @Test
    public void grsRebuild_throws_exception() {
        doThrow(new RuntimeException("Oops")).when(grsImporter).grsImport(anyString(), anyBoolean());
        final String result = subject.grsRebuild("ARIN-GRS,APNIC-GRS", "grsrebuildnow", "comment");

        verify(grsImporter).grsImport("ARIN-GRS,APNIC-GRS", true);
        assertNull(result);
    }
}
