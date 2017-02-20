package net.ripe.db.whois.common.scheduler;

import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DailySchedulerTest {
    @Mock private TestDateTimeProvider dateTimeProvider = new TestDateTimeProvider();
    @Mock private MaintenanceMode mockMaintenanceMode;
    @Mock private DailySchedulerDao mockDailySchedulerDao;
    @Mock private DailyScheduledTask mockTask;
    @InjectMocks @Autowired private DailyScheduler subject;

    @Test
    public void testDailyScheduledTasksInRegularMode() throws Exception {
        when(mockMaintenanceMode.allowUpdate()).thenReturn(Boolean.TRUE);
        when(mockDailySchedulerDao.acquireDailyTask((LocalDate) anyObject(), (Class) anyObject(), anyString())).thenReturn(Boolean.TRUE);
        subject.setScheduledTasks(mockTask);

        subject.executeScheduledTasks();

        verify(mockTask).run();
        verify(mockDailySchedulerDao).acquireDailyTask((LocalDate) anyObject(), (Class) anyObject(), anyString());
        verify(mockDailySchedulerDao).markTaskDone(anyLong(), (LocalDate) anyObject(), (Class) anyObject());
        verify(mockDailySchedulerDao).removeOldScheduledEntries((LocalDate) anyObject());
    }

    @Test
    public void testDailyScheduledTasksAcquiringProblem() throws Exception {
        when(mockMaintenanceMode.allowUpdate()).thenReturn(Boolean.TRUE);
        when(mockDailySchedulerDao.acquireDailyTask((LocalDate) anyObject(), (Class)any(), anyString())).thenReturn(Boolean.FALSE);
        subject.setScheduledTasks(mockTask);

        subject.executeScheduledTasks();

        verify(mockTask, never()).run();
        verify(mockDailySchedulerDao).acquireDailyTask((LocalDate) anyObject(), (Class) anyObject(), anyString());
        verify(mockDailySchedulerDao, never()).markTaskDone(anyLong(), (LocalDate) anyObject(), (Class) anyObject());
        verify(mockDailySchedulerDao).removeOldScheduledEntries((LocalDate) anyObject());
    }

    @Test
    public void testDailyScheduledTasksInMaintenanceMode() throws Exception {
        when(mockMaintenanceMode.allowUpdate()).thenReturn(Boolean.FALSE);
        subject.setScheduledTasks(mockTask);

        subject.executeScheduledTasks();

        verify(mockTask, never()).run();
        verify(mockDailySchedulerDao, never()).acquireDailyTask((LocalDate) anyObject(), (Class) anyObject(), anyString());
        verify(mockDailySchedulerDao, never()).markTaskDone(anyLong(), (LocalDate) anyObject(), (Class) anyObject());
        verify(mockDailySchedulerDao, never()).removeOldScheduledEntries((LocalDate) anyObject());
    }
}
