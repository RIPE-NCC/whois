package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock DailySchedulerDao dailySchedulerDao;
    @Mock ResourceDataDao resourceDataDao;
    AuthoritativeResourceRefreshTask subject;
    AuthoritativeResourceData authoritativeResourceData;

    @Before
    public void setUp() {
        authoritativeResourceData = new AuthoritativeResourceData("test", "test", resourceDataDao);
        subject = new AuthoritativeResourceRefreshTask(dailySchedulerDao, authoritativeResourceData, resourceDataDao, "test");
    }

    @Test
    public void refresh() {
        when(dailySchedulerDao.getDailyTaskFinishTime(any(String.class))).thenReturn(Optional.of(Timestamp.fromSeconds(10L)));
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

    @Test(expected = IllegalSourceException.class)
    public void nonexistant_source_throws_exception() {
        authoritativeResourceData.getAuthoritativeResource(ciString("BLAH"));
    }

}
