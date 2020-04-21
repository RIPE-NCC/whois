package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.io.Downloader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.util.StringValueResolver;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceImportTaskTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Captor ArgumentCaptor<AuthoritativeResource> resourceCaptor;
    @Mock Downloader downloader;
    @Mock ResourceDataDao resourceDataDao;
    @Mock StringValueResolver valueResolver;
    AuthoritativeResourceImportTask subject;

    @Before
    public void setUp() {
        subject = new AuthoritativeResourceImportTask("TEST", resourceDataDao, downloader, folder.getRoot().getAbsolutePath(), true, "");
        subject.setEmbeddedValueResolver(valueResolver);
    }

    @Test
    public void init_url_not_defined() throws IOException {
        subject.run();
        verify(resourceDataDao).store(eq("test"), resourceCaptor.capture());
        assertThat(resourceCaptor.getValue().getNrAutNums(), is(0));
        assertThat(resourceCaptor.getValue().getNrInet6nums(), is(0));
        assertThat(resourceCaptor.getValue().getNrInetnums(), is(0));
    }

    @Test
    public void download_not_resource_data() throws IOException {
        when(valueResolver.resolveStringValue(anyString())).thenReturn("http://www.ripe.net/download");
        subject.run();
        verify(resourceDataDao, never()).store(anyString(), Mockito.<AuthoritativeResource>anyObject());
    }

    @Test
    public void downloaded_fails() throws IOException {
        when(valueResolver.resolveStringValue(anyString())).thenReturn("http://www.ripe.net/download");
        doThrow(IOException.class).when(downloader).downloadToWithMd5Check(any(Logger.class), any(URL.class), any(Path.class));

        subject.run();
    }

    @Test
    public void download() throws IOException {
        when(valueResolver.resolveStringValue(anyString())).thenReturn("http://www.ripe.net/download");

        doAnswer(invocation -> {
            final Path path = (Path) invocation.getArguments()[2];
            Files.createFile(path);
            return null;
        }).when(downloader).downloadToWithMd5Check(any(Logger.class), any(URL.class), any(Path.class));

        subject.run();

        verify(resourceDataDao).store(eq("test"), resourceCaptor.capture());
        assertThat(resourceCaptor.getValue().getNrAutNums(), is(0));
        assertThat(resourceCaptor.getValue().getNrInet6nums(), is(0));
        assertThat(resourceCaptor.getValue().getNrInetnums(), is(0));
    }
}
