package net.ripe.db.whois.common.grs;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataJmxTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock AuthoritativeResourceDataValidator authoritativeResourceDataValidator;
    @InjectMocks AuthoritativeResourceDataJmx subject;

    @Test
    public void refreshCache() {
        final String msg = subject.refreshCache("comment");
        assertThat(msg, is("Refreshed caches"));

        verify(authoritativeResourceData).refreshAuthoritativeResourceCache();
    }

    @Test
    public void checkOverlaps_existing_file() throws IOException {
        final File file = File.createTempFile("overlaps", "test");
        final String msg = subject.checkOverlaps(file.getAbsolutePath(), "");
        assertThat(msg, startsWith("Abort, file already exists"));

        verifyZeroInteractions(authoritativeResourceDataValidator);
    }

    @Test
    public void checkOverlaps_unwriteable_file() throws IOException {
        final File file = new File("/some/unexisting/dir/overlaps");
        final String msg = subject.checkOverlaps(file.getAbsolutePath(), "");
        assertThat(msg, startsWith("Failed writing to: /some/unexisting/dir/overlaps"));

        verifyZeroInteractions(authoritativeResourceDataValidator);
    }

    @Test
    public void checkOverlaps() throws IOException {
        folder.getRoot().mkdirs();
        final File file = new File(folder.getRoot(), "overlaps.txt");

        final String msg = subject.checkOverlaps(file.getAbsolutePath(), "");
        assertThat(msg, startsWith("Overlaps written to"));

        verify(authoritativeResourceDataValidator).checkOverlaps(any(Writer.class));
    }
}
