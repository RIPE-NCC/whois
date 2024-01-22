package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AutomaticPermanentBlocksCleanupTest {
    @Mock DateTimeProvider dateTimeProvider;
    @Mock
    IpAccessControlListDao ipAccessControlListDao;
    @InjectMocks AutomaticPermanentBlocksCleanup subject;

    @Test
    public void run() {
        final LocalDate now = LocalDate.now();

        when(dateTimeProvider.getCurrentDate()).thenReturn(now);

        subject.run();

        // Should only delete bans older than 1 year
        verify(ipAccessControlListDao, times(1)).removePermanentBlocksBefore(argThat(item -> item.equals(now.minusYears(1))));

        // Should only delete events older than 3 months
        verify(ipAccessControlListDao, times(1)).removeBlockEventsBefore(argThat(item -> item.equals(now.minusMonths(3))));
    }
}
