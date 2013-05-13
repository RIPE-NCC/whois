package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AutomaticPermanentBlocksCleanupTest {
    @Mock DateTimeProvider dateTimeProvider;
    @Mock AccessControlListDao accessControlListDao;
    @InjectMocks AutomaticPermanentBlocksCleanup subject;

    @Test
    public void run() {
        final LocalDate now = new LocalDate();

        when(dateTimeProvider.getCurrentDate()).thenReturn(now);

        subject.run();

        verify(accessControlListDao, times(1)).removePermanentBlocksBefore(argThat(new BaseMatcher<LocalDate>() {
            @Override
            public boolean matches(Object item) {
                return item.equals(now.minusYears(1));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Should only delete bans older than 1 year");
            }
        }));

        verify(accessControlListDao, times(1)).removeBlockEventsBefore(argThat(new BaseMatcher<LocalDate>() {
            @Override
            public boolean matches(Object item) {
                return item.equals(now.minusMonths(3));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Should only delete events older than 3 months");
            }
        }));

    }
}
