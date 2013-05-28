package net.ripe.db.whois.common.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.util.StringValueResolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    List<String> sources = Lists.newArrayList("TEST");

    @Mock Downloader downloader;
    @Mock StringValueResolver valueResolver;
    AuthoritativeResourceData subject;

    @Before
    public void setUp() {
        subject = new AuthoritativeResourceData(sources, folder.getRoot().getAbsolutePath(), downloader);
        subject.setEmbeddedValueResolver(valueResolver);
    }

    @Test
    public void init_url_not_defined() throws IOException {
        try {
            subject.init();
        } finally {
            subject.cleanup();
        }

        final AuthoritativeResource authoritativeResource = subject.getAuthoritativeResource(ciString("TEST"));
        assertNotNull(authoritativeResource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_not_downloaded_or_cached() throws IOException {
        when(valueResolver.resolveStringValue(anyString())).thenReturn("http://www.ripe.net/download");
        subject.init();
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_downloaded_fails() throws IOException {
        when(valueResolver.resolveStringValue(anyString())).thenReturn("http://www.ripe.net/download");
        doThrow(IOException.class).when(downloader).downloadGrsData(any(Logger.class), any(URL.class), any(File.class));

        subject.init();
    }

    @Test
    public void init_download() throws IOException {
        when(valueResolver.resolveStringValue(anyString())).thenReturn("http://www.ripe.net/download");

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final File file = (File) invocation.getArguments()[2];
                file.createNewFile();
                return null;
            }
        }).when(downloader).downloadGrsData(any(Logger.class), any(URL.class), any(File.class));

        try {
            subject.init();
        } finally {
            subject.cleanup();
        }

        final AuthoritativeResource authoritativeResource = subject.getAuthoritativeResource(ciString("TEST"));
        assertNotNull(authoritativeResource);
    }

    @Test
    public void refreshAuthoritativeResourceCache_url_not_defined() {
        subject.refreshAuthoritativeResourceCache();
    }

    @Test
    public void refreshAuthoritativeResourceCache_download_fails() throws IOException {
        when(valueResolver.resolveStringValue(anyString())).thenReturn("http://www.ripe.net/download");
        doThrow(IOException.class).when(downloader).downloadGrsData(any(Logger.class), any(URL.class), any(File.class));

        subject.refreshAuthoritativeResourceCache();
    }

    @Test(expected = IllegalSourceException.class)
    public void getAuthoritativeResource_unknown() {
        subject.getAuthoritativeResource(ciString("UNKNOWN"));
    }
}
