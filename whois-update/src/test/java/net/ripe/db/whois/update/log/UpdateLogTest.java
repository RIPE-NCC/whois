package net.ripe.db.whois.update.log;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResult;
import net.ripe.db.whois.update.domain.UpdateStatus;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateLogTest {
    @Mock UpdateRequest updateRequest;
    @Mock UpdateContext updateContext;
    @Mock Update update;
    final Stopwatch stopwatch = Stopwatch.createUnstarted();

    @Mock Logger logger;
    @InjectMocks UpdateLog subject;

    @Test
    public void logUpdateResult_create_success() {
        final RpslObject maintainer = RpslObject.parse("mntner: TST-MNT");
        final UpdateResult updateResult = new UpdateResult(maintainer, maintainer, Action.CREATE, UpdateStatus.SUCCESS, new ObjectMessages(), 0, false);
        when(update.getCredentials()).thenReturn(new Credentials());
        when(updateContext.createUpdateResult(update)).thenReturn(updateResult);

        subject.logUpdateResult(updateRequest, updateContext, update, stopwatch);

        verify(logger).info(matches("\\[\\s*0\\] 0[,.]000 ns   UPD CREATE mntner       TST-MNT                        \\(1\\) SUCCESS               : <E0,W0,I0> AUTH  - null"));
    }

    @Test
    public void logUpdateResult_create_success_dryRun() {
        final RpslObject maintainer = RpslObject.parse("mntner: TST-MNT");
        final UpdateResult updateResult = new UpdateResult(maintainer, maintainer, Action.CREATE, UpdateStatus.SUCCESS, new ObjectMessages(), 0, true);
        when(update.getCredentials()).thenReturn(new Credentials());
        when(updateContext.createUpdateResult(update)).thenReturn(updateResult);

        subject.logUpdateResult(updateRequest, updateContext, update, stopwatch);

        verify(logger).info(matches("\\[\\s*0\\] 0[,.]000 ns   DRY CREATE mntner       TST-MNT                        \\(1\\) SUCCESS               : <E0,W0,I0> AUTH  - null"));
    }
}
