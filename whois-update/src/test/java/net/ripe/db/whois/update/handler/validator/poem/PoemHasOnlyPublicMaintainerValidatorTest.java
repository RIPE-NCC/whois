package net.ripe.db.whois.update.handler.validator.poem;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PoemHasOnlyPublicMaintainerValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @InjectMocks PoemHasOnlyPublicMaintainerValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.POEM));
    }

    @Test
    public void validate_LIM_MNT() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "poem:            POEM-FORM-LIMERICK\n" +
                "mnt-by:          LIM-MNT\n"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_other_maintainer() {
        final RpslObject poem = RpslObject.parse("" +
                "poem:            POEM-FORM-LIMERICK\n" +
                "mnt-by:          DEV-MNT\n");

        when(update.getUpdatedObject()).thenReturn(poem);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, poem.findAttribute(AttributeType.MNT_BY), UpdateMessages.poemRequiresPublicMaintainer());
    }
}
