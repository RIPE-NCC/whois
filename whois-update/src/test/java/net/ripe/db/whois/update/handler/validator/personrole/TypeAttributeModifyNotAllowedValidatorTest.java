package net.ripe.db.whois.update.handler.validator.personrole;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TypeAttributeModifyNotAllowedValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    private TypeAttributeModifyNotAllowedValidator subject;

    @Before
    public void setup() {
        subject = new TypeAttributeModifyNotAllowedValidator();
    }

    @Test
    public void same_person() {
        when(update.getType()).thenReturn(ObjectType.PERSON);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("person: TEST PERSON\nnic-hdl: TP1-RIPE"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("person: TEST PERSON\nnic-hdl: TP1-RIPE"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void same_role() {
        when(update.getType()).thenReturn(ObjectType.ROLE);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("role: Admin\nnic-hdl: TP1-RIPE"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("role: Admin\nnic-hdl: TP1-RIPE"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void not_same_role() {
        when(update.getType()).thenReturn(ObjectType.ROLE);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("role: Master Admin\nnic-hdl: TP1-RIPE"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("role: Admin\nnic-hdl: TP2-RIPE"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.nameChanged());
    }

    @Test
    public void not_same_person() {
        when(update.getType()).thenReturn(ObjectType.PERSON);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("person: Person A\nnic-hdl: TP1-RIPE"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("person: Person B\nnic-hdl: TP2-RIPE"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.nameChanged());
    }
}
