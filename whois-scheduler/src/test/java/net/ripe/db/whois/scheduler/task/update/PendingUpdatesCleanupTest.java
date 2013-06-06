package net.ripe.db.whois.scheduler.task.update;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PendingUpdatesCleanupTest {

    @Mock private PendingUpdateDao pendingUpdateDao;
    @Mock private DateTimeProvider dateTimeProvider;
    private PendingUpdatesCleanup subject;

    @Before
    public void setup() {
        subject = new PendingUpdatesCleanup(pendingUpdateDao, dateTimeProvider);
    }

    @Test
    public void cleanup() {
        final LocalDateTime now = LocalDateTime.now();
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(now);

        subject.run();

        verify(pendingUpdateDao).removePendingUpdatesBefore(now.minusDays(7));
    }
}
