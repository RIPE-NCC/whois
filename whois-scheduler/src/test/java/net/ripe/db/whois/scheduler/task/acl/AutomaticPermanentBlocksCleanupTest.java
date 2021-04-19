package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutomaticPermanentBlocksCleanupTest {
    @Mock DateTimeProvider dateTimeProvider;
    @Mock AccessControlListDao accessControlListDao;
    @InjectMocks AutomaticPermanentBlocksCleanup subject;

    @Test
    public void run() {
        final LocalDate now = LocalDate.now();

        when(dateTimeProvider.getLocalDateUtc()).thenReturn(now);

        subject.run();

        // Should only delete bans older than 1 year
        verify(accessControlListDao, times(1)).removePermanentBlocksBefore(argThat(item -> item.equals(now.minusYears(1))));

        // Should only delete events older than 3 months
        verify(accessControlListDao, times(1)).removeBlockEventsBefore(argThat(item -> item.equals(now.minusMonths(3))));
    }
}
