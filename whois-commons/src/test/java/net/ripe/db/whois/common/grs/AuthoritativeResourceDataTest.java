package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthoritativeResourceDataTest {
    @TempDir
    public File folder;

    @Mock DailySchedulerDao dailySchedulerDao;
    @Mock ResourceDataDao resourceDataDao;
    AuthoritativeResourceRefreshTask subject;
    AuthoritativeResourceData authoritativeResourceData;

    @BeforeEach
    public void setUp() {
        authoritativeResourceData = new AuthoritativeResourceData("test", "test", resourceDataDao);
        subject = new AuthoritativeResourceRefreshTask(dailySchedulerDao, authoritativeResourceData, resourceDataDao, true, "", "test");
    }

    @Test
    public void refresh() {
        when(resourceDataDao.load(any(String.class))).thenReturn(AuthoritativeResource.unknown());

        authoritativeResourceData.init();

        verify(resourceDataDao, times(2)).load(eq("test"));
        assertThat(authoritativeResourceData.getAuthoritativeResource(ciString("test")), isA(AuthoritativeResource.class));
    }

    @Test
    public void refresh_on_change() {
        when(resourceDataDao.getState("test")).thenReturn(new ResourceDataDao.State("test", 1, 1)).thenReturn(new ResourceDataDao.State("test", 2, 2));

        subject.refreshMainAuthoritativeResourceCache();
        subject.refreshMainAuthoritativeResourceCache();

        verify(resourceDataDao, times(2)).load(eq("test"));
    }

    @Test
    public void no_refresh_if_unchanged() {
        when(resourceDataDao.getState("test")).thenReturn(new ResourceDataDao.State("test", 1, 1)).thenReturn(new ResourceDataDao.State("test", 1, 1));

        subject.refreshMainAuthoritativeResourceCache();
        subject.refreshMainAuthoritativeResourceCache();

        verify(resourceDataDao, times(1)).load("test");
    }

    @Test
    public void nonexistant_source_throws_exception() {
        assertThrows(IllegalSourceException.class, () -> {
            authoritativeResourceData.getAuthoritativeResource(ciString("BLAH"));
        });
    }

}
