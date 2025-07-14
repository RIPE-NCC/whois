package net.ripe.db.whois.update.handler.validator.personrole;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SelfReferencePreventionValidatorTest {
    @Mock UpdateContext updateContext;
    @Mock PreparedUpdate preparedUpdate;
    @Mock Update update;

    @InjectMocks SelfReferencePreventionValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.MODIFY, Action.CREATE));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.ROLE));
    }

    @Test
    public void not_self_referenced() {
        when(preparedUpdate.getUpdate()).thenReturn(update);
        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: OTHER-TEST\ntech-c: TECH-TEST"));

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(new RpslAttribute(AttributeType.ADMIN_C, "OTHER-TEST")));
        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(new RpslAttribute(AttributeType.TECH_C, "TECH-TEST")));
    }

    @Test
    public void self_referenced_adminC() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: NIC-TEST\ntech-c: TECH-TEST");
        when(preparedUpdate.getUpdate()).thenReturn(update);
        when(update.getSubmittedObject()).thenReturn(role);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, times(1)).addMessage(preparedUpdate, role.findAttribute(AttributeType.ADMIN_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.ADMIN_C)));
        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.TECH_C)));
    }

    @Test
    public void self_referenced_techC() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: OTHER-TEST\ntech-c: NIC-TEST");
        when(preparedUpdate.getUpdate()).thenReturn(update);
        when(update.getSubmittedObject()).thenReturn(role);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.ADMIN_C)));
        verify(updateContext, times(1)).addMessage(preparedUpdate,role.findAttribute(AttributeType.TECH_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.TECH_C)));
    }


    @Test
    public void self_referenced_techC_adminC() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: NIC-TEST\ntech-c: NIC-TEST");
        when(preparedUpdate.getUpdate()).thenReturn(update);
        when(update.getSubmittedObject()).thenReturn(role);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, times(1)).addMessage(preparedUpdate, role.findAttribute(AttributeType.ADMIN_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.ADMIN_C)));
        verify(updateContext, times(1)).addMessage(preparedUpdate, role.findAttribute(AttributeType.TECH_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.TECH_C)));
    }
}
