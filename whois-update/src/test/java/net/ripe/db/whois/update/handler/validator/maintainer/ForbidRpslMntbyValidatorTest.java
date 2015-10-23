package net.ripe.db.whois.update.handler.validator.maintainer;

import net.ripe.db.whois.common.rpsl.ObjectMessages;
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


import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.mockito.Mockito.verify;
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
    public void validate_creation() {
        when(preparedUpdate.hasOriginalObject()).thenReturn(false);

        final RpslObject updatedObject = RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST\nmnt-by: RIPE-NCC-RPSL-MNT\nsource: TEST");

        when(preparedUpdate.getUpdatedObject()).thenReturn(updatedObject);


        subject.validate(preparedUpdate, updateContext);


        verify(updateContext).addMessage(preparedUpdate, updatedObject.getAttributes().get(0), UpdateMessages.rpslMntbyForbidden());
    }

}
