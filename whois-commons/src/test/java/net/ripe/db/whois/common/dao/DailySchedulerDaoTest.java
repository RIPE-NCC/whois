package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;

public class DailySchedulerDaoTest extends AbstractDaoTest {
    @Autowired DailySchedulerDao subject;

    @Test
    public void test_acquire_does_not_allocate_twice() {
        LocalDate date = new LocalDate();
        assertTrue(subject.acquireDailyTask(date, getClass(), "localhost"));
        assertFalse(subject.acquireDailyTask(date, getClass(), "localhost"));
        assertTrue(subject.acquireDailyTask(date.plusDays(1), getClass(), "localhost"));
        assertTrue(subject.acquireDailyTask(date.plusDays(1), subject.getClass(), "blabla"));
    }

    @Test
    public void test_marks_as_done() {
        LocalDate date = new LocalDate();
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
        LocalDate date = new LocalDate();
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
