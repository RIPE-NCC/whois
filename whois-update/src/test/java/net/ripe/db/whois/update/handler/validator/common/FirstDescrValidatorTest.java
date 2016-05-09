package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirstDescrValidatorTest {
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;
    @Mock Subject subjectObject;
    @Mock Maintainers maintainers;
    @InjectMocks private FirstDescrValidator subject;

    @Before
    public void setup() {
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(maintainers.isRsMaintainer(any(Set.class))).thenReturn(Boolean.TRUE);
    }

    @Test
    public void skip_when_override() {
        when(update.isOverride()).thenReturn(true);
        when(subjectObject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verifyZeroInteractions(update);
    }

    @Test
    public void skip_when_rs_maintainer() {
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verifyZeroInteractions(update);
    }

    @Test
    public void added_first() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\ndescr: added"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.descrCannotBeAdded());
    }

    @Test
    public void added_second() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\ndescr: first"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\ndescr: first\ndescr: second"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.descrCannotBeAdded());
    }

    @Test
    public void changed_first() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\ndescr: original"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\ndescr: changed"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.descrCannotBeChanged());
    }

    @Test
    public void removed_first() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32\ndescr: original"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 193.0/32"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.descrCannotBeRemoved());
    }
}