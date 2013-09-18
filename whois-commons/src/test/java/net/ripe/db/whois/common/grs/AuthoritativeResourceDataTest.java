package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.scheduler.DailyScheduler;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.Arrays;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock DailyScheduler dailyScheduler;
    @Mock ResourceDataDao resourceDataDao;
    @Mock Logger logger;
    AuthoritativeResourceData subject;

    @Before
    public void setUp() {
        subject = new AuthoritativeResourceData(Arrays.asList("TEST"), resourceDataDao, dailyScheduler);
    }

    @Test
    public void refresh() {
        when(dailyScheduler.getDailyTaskFinishTime(any(Class.class))).thenReturn(new Long(10));
        final AuthoritativeResource unknown = AuthoritativeResource.unknown(logger);
        when(resourceDataDao.load(any(Logger.class), any(String.class))).thenReturn(unknown);
        subject.init();
        verify(resourceDataDao).load(any(Logger.class), eq("test"));
        assertThat(subject.getAuthoritativeResource(ciString("TEST")), isA(AuthoritativeResource.class));
    }

    @Test(expected = IllegalSourceException.class)
    public void nonexistant_source_throws_exception() {
        subject.getAuthoritativeResource(ciString("BLAH"));
    }

}
