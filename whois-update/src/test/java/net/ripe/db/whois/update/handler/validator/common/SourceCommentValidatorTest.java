package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

        verifyNoMoreInteractions(updateContext);
    }
}
