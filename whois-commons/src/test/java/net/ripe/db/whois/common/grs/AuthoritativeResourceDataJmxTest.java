package net.ripe.db.whois.common.grs;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataJmxTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock AuthoritativeResourceDataValidator authoritativeResourceDataValidator;
    @Mock AuthoritativeResourceRefreshTask authoritativeResourceRefreshTask;
    @InjectMocks AuthoritativeResourceDataJmx subject;

    @Test
    public void refreshCache() {
        final String msg = subject.refreshCache("comment");
        assertThat(msg, is("Refreshed caches"));

        verify(authoritativeResourceRefreshTask).refreshGrsAuthoritativeResourceCaches();
    }

    @Test
    public void checkOverlaps_existing_file() throws IOException {
        final File file = File.createTempFile("overlaps", "test");
        final String msg = subject.checkOverlaps(file.getAbsolutePath(), "");
        assertThat(msg, startsWith("Abort, file already exists"));

        verifyNoMoreInteractions(authoritativeResourceDataValidator);
    }

    @Test
    public void checkOverlaps_unwriteable_file() throws IOException {
        final File file = new File("/some/unexisting/dir/overlaps");
        final String msg = subject.checkOverlaps(file.getAbsolutePath(), "");
        assertThat(msg, startsWith("Failed writing to: /some/unexisting/dir/overlaps"));

        verifyNoMoreInteractions(authoritativeResourceDataValidator);
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
