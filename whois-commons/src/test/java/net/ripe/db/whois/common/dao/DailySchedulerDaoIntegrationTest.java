package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DailySchedulerDaoIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired DailySchedulerDao subject;

    @Test
    public void test_acquire_does_not_allocate_twice() {
        LocalDate date = LocalDate.now();
        assertTrue(subject.acquireDailyTask(date, getClass(), "localhost"));
        assertFalse(subject.acquireDailyTask(date, getClass(), "localhost"));
        assertTrue(subject.acquireDailyTask(date.plusDays(1), getClass(), "localhost"));
        assertTrue(subject.acquireDailyTask(date.plusDays(1), subject.getClass(), "blabla"));
    }

    @Test
    public void test_marks_as_done() {
        LocalDate date = LocalDate.now();
        assertTrue(subject.acquireDailyTask(date, getClass(), "localhost"));

        final long when = System.currentTimeMillis();
        subject.markTaskDone(when, date, getClass());
        assertFalse(subject.acquireDailyTask(date, getClass(), "localhost"));
        assertTrue(subject.acquireDailyTask(date.plusDays(1), getClass(), "localhost"));
        assertTrue(subject.acquireDailyTask(date, subject.getClass(), "localhost"));

        assertThat(when - subject.getDailyTaskFinishTime(date, getClass()), lessThanOrEqualTo(1000L));
    }

    @Test
    public void remove_tasks() {
        LocalDate date = LocalDate.now();
        assertTrue(subject.acquireDailyTask(date, getClass(), "localhost"));
        subject.removeFinishedScheduledTasks();
        assertTrue(subject.acquireDailyTask(date, getClass(), "localhost"));
        subject.removeFinishedScheduledTask(getClass());
        assertTrue(subject.acquireDailyTask(date, getClass(), "localhost"));
        subject.removeOldScheduledEntries(date);
        assertFalse(subject.acquireDailyTask(date, getClass(), "localhost"));
        subject.removeOldScheduledEntries(date.plusDays(1));
        assertTrue(subject.acquireDailyTask(date, getClass(), "localhost"));
    }
}
