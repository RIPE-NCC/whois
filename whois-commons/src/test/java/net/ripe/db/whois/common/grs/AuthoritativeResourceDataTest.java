package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

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

    @Mock DailySchedulerDao dailySchedulerDao;
    @Mock ResourceDataDao resourceDataDao;
    @Mock Logger logger;
    @Mock DateTimeProvider dateTimeProvider;
    AuthoritativeResourceData subject;

    @Before
    public void setUp() {
        subject = new AuthoritativeResourceData("TEST", resourceDataDao, dailySchedulerDao, dateTimeProvider);
    }

    @Test
    public void refresh() {
        when(dailySchedulerDao.getDailyTaskFinishTime(any(LocalDate.class), any(Class.class))).thenReturn(10l);
        when(resourceDataDao.load(any(String.class))).thenReturn(AuthoritativeResource.unknown());

        subject.init();

        verify(resourceDataDao).load(eq("test"));
        assertThat(subject.getAuthoritativeResource(ciString("TEST")), isA(AuthoritativeResource.class));
    }

    @Test(expected = IllegalSourceException.class)
    public void nonexistant_source_throws_exception() {
        subject.getAuthoritativeResource(ciString("BLAH"));
    }

}
