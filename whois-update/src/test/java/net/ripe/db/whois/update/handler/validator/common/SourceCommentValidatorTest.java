package net.ripe.db.whois.update.handler.validator.common;

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
public class SourceCommentValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @InjectMocks SourceCommentValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.values()));
    }

    @Test
    public void source_attribute_has_comment() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("mntner: TEST-MNT\nsource: TEST # comment"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.commentInSourceNotAllowed());
    }

    @Test
    public void source_attribute_has_no_comment() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("mntner: TEST-MNT\nsource: TEST"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
