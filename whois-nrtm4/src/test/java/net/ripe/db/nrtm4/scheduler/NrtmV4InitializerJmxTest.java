package net.ripe.db.nrtm4.scheduler;

import net.ripe.db.nrtm4.dao.UpdateNrtmFileRepository;
import net.ripe.db.nrtm4.generator.NrtmKeyPairService;
import net.ripe.db.whois.common.DateTimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NrtmV4InitializerJmxTest {

    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2012, 12, 1, 0, 0);
    @Mock SnapshotFileScheduledTask snapshotFileScheduledTask;
    @Mock TaskScheduler taskScheduler;
    @Mock UpdateNrtmFileRepository nrtmFileRepository;
    @Mock
    NrtmKeyPairService nrtmKeyPairService;

    @Mock DateTimeProvider dateTimeProvider;
    @Captor
    ArgumentCaptor<ScheduledMethodRunnable> scheduleTaskCaptor;

    @InjectMocks
    NrtmV4Jmx subject;

    @Test
    public void shouldInitializeNrtmv4()  {
        when(dateTimeProvider.getCurrentZonedDateTime()).thenReturn(LOCAL_DATE_TIME.atZone(ZoneOffset.UTC));
        subject.runInitializerTask("test");

        verify(nrtmFileRepository, times(1)).cleanupNrtmv4Database();
        verify(taskScheduler, times(1)).schedule(scheduleTaskCaptor.capture(), eq(LOCAL_DATE_TIME.atZone(ZoneOffset.UTC).toInstant()));
        assertThat(scheduleTaskCaptor.getValue().getTarget().getClass(), is(SnapshotFileScheduledTask.class));
    }

    @Test
    public void shouldForceRotateKey()  {
        subject.forceRotateKey("test");

        verify(nrtmKeyPairService, times(1)).forceRotateKey();
    }

    @Test
    public void shouldGenerateNewActiveKey()  {
        subject.deleteAndGenerateNewActiveKey("test");

        verify(nrtmKeyPairService, times(1)).deleteAndGenerateNewActiveKey();
    }
}
