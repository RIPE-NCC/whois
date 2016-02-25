package net.ripe.db.whois.update.handler.validator.maintainer;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ForbidRpslMntbyValidatorTest {

    @Mock
    private PreparedUpdate preparedUpdate;

    @Mock
    private UpdateContext updateContext;

    @InjectMocks
    private ForbidRpslMntbyValidator subject;

    @Test
    public void actions() {
        Assert.assertThat(subject.getActions(), Matchers.containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void test_forbid_rpsl_multiple_mnt_by() {
        final RpslObject updatedObject = RpslObject.parse("person: Test Person\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: OWNER-MNT, RIPE-NCC-RPSL-MNT\n" +
                "source: TEST");
        when(preparedUpdate.getUpdatedObject()).thenReturn(updatedObject);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(preparedUpdate, UpdateMessages.rpslMntbyForbidden());
    }

    @Test
    public void test_forbid_rpsl_multiline_mnt_by() {
        final RpslObject updatedObject = RpslObject.parse("person: Test Person\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "mnt-by: RIPE-NCC-RPSL-MNT\n" +
                "source: TEST");
        when(preparedUpdate.getUpdatedObject()).thenReturn(updatedObject);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(preparedUpdate, UpdateMessages.rpslMntbyForbidden());
    }

    @Test
    public void test_multiple_mnts_in_mntby() {
        final RpslObject updatedObject = RpslObject.parse("person: Test Person\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: OWNER-MNT, TEST-MNT\n" +
                "source: TEST");
        when(preparedUpdate.getUpdatedObject()).thenReturn(updatedObject);

        subject.validate(preparedUpdate, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
