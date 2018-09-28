package net.ripe.db.whois.common.scheduler;

import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DailySchedulerTest {
    @Mock private MaintenanceMode maintenanceMode;
    @Mock private DailySchedulerDao dailySchedulerDao;
    @Mock private DailyScheduledTask dailyScheduledTask;
    private TestDateTimeProvider dateTimeProvider = new TestDateTimeProvider();
    private DailyScheduler subject;

    @Before
    public void setUp() {
        this.subject = new DailyScheduler(dateTimeProvider, dailySchedulerDao, maintenanceMode);
    }

    @Test
    public void testDailyScheduledTasksInRegularMode() {
        when(maintenanceMode.allowUpdate()).thenReturn(Boolean.TRUE);
        when(dailySchedulerDao.acquireDailyTask(any(LocalDate.class), any(Class.class), anyString())).thenReturn(Boolean.TRUE);
        subject.setScheduledTasks(dailyScheduledTask);

        subject.executeScheduledTasks();

        verify(dailyScheduledTask).run();
        verify(dailySchedulerDao).acquireDailyTask(any(LocalDate.class), any(Class.class), anyString());
        verify(dailySchedulerDao).markTaskDone(anyLong(), any(LocalDate.class), any(Class.class));
        verify(dailySchedulerDao).removeOldScheduledEntries(any(LocalDate.class));
    }

    @Test
    public void testDailyScheduledTasksAcquiringProblem() {
        when(maintenanceMode.allowUpdate()).thenReturn(Boolean.TRUE);
        when(dailySchedulerDao.acquireDailyTask(any(LocalDate.class), any(Class.class), anyString())).thenReturn(Boolean.FALSE);
        subject.setScheduledTasks(dailyScheduledTask);

        subject.executeScheduledTasks();

        verify(dailyScheduledTask, never()).run();
        verify(dailySchedulerDao).acquireDailyTask(any(LocalDate.class), any(Class.class), anyString());
        verify(dailySchedulerDao, never()).markTaskDone(anyLong(), any(LocalDate.class), any(Class.class));
        verify(dailySchedulerDao).removeOldScheduledEntries(any(LocalDate.class));
    }

    @Test
    public void testDailyScheduledTasksInMaintenanceMode() {
        when(maintenanceMode.allowUpdate()).thenReturn(Boolean.FALSE);
        subject.setScheduledTasks(dailyScheduledTask);

        subject.executeScheduledTasks();

        verify(dailyScheduledTask, never()).run();
        verify(dailySchedulerDao, never()).acquireDailyTask(any(LocalDate.class), any(Class.class), anyString());
        verify(dailySchedulerDao, never()).markTaskDone(anyLong(), any(LocalDate.class), any(Class.class));
        verify(dailySchedulerDao, never()).removeOldScheduledEntries(any(LocalDate.class));
    }
}
