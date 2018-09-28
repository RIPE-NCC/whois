package net.ripe.db.whois.common.scheduler;

import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DailySchedulerTest {
    @Mock private MaintenanceMode mockMaintenanceMode;
    @Mock private DailySchedulerDao mockDailySchedulerDao;
    @Mock private DailyScheduledTask mockTask;
    @InjectMocks @Autowired private DailyScheduler subject;

    @Test
    public void testDailyScheduledTasksInRegularMode() {
        when(mockMaintenanceMode.allowUpdate()).thenReturn(Boolean.TRUE);
        when(mockDailySchedulerDao.acquireDailyTask(any(LocalDate.class), any(Class.class), anyString())).thenReturn(Boolean.TRUE);
        subject.setScheduledTasks(mockTask);

        subject.executeScheduledTasks();

        verify(mockTask).run();
        verify(mockDailySchedulerDao).acquireDailyTask(any(LocalDate.class), any(Class.class), anyString());
        verify(mockDailySchedulerDao).markTaskDone(anyLong(), any(LocalDate.class), any(Class.class));
        verify(mockDailySchedulerDao).removeOldScheduledEntries(any(LocalDate.class));
    }

    @Test
    public void testDailyScheduledTasksAcquiringProblem() {
        when(mockMaintenanceMode.allowUpdate()).thenReturn(Boolean.TRUE);
        when(mockDailySchedulerDao.acquireDailyTask(any(LocalDate.class), any(Class.class), anyString())).thenReturn(Boolean.FALSE);
        subject.setScheduledTasks(mockTask);

        subject.executeScheduledTasks();

        verify(mockTask, never()).run();
        verify(mockDailySchedulerDao).acquireDailyTask(any(LocalDate.class), any(Class.class), anyString());
        verify(mockDailySchedulerDao, never()).markTaskDone(anyLong(), any(LocalDate.class), any(Class.class));
        verify(mockDailySchedulerDao).removeOldScheduledEntries(any(LocalDate.class));
    }

    @Test
    public void testDailyScheduledTasksInMaintenanceMode() {
        when(mockMaintenanceMode.allowUpdate()).thenReturn(Boolean.FALSE);
        subject.setScheduledTasks(mockTask);

        subject.executeScheduledTasks();

        verify(mockTask, never()).run();
        verify(mockDailySchedulerDao, never()).acquireDailyTask(any(LocalDate.class), any(Class.class), anyString());
        verify(mockDailySchedulerDao, never()).markTaskDone(anyLong(), any(LocalDate.class), any(Class.class));
        verify(mockDailySchedulerDao, never()).removeOldScheduledEntries(any(LocalDate.class));
    }
}
