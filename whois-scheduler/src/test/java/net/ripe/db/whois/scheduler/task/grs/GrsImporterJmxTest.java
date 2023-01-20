package net.ripe.db.whois.scheduler.task.grs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GrsImporterJmxTest {
    @Mock GrsImporter grsImporter;
    @InjectMocks GrsImporterJmx subject;

    @BeforeEach
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
        assertThat(result, is(nullValue()));
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
        assertThat(result, is(nullValue()));
    }
}
