package net.ripe.db.whois.update.handler.validator.personrole;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SelfReferencePreventionValidatorTest {
    @Mock UpdateContext updateContext;
    @Mock PreparedUpdate preparedUpdate;
    @Mock Update update;
    @Mock Subject authSubject;

    @InjectMocks SelfReferencePreventionValidator subject;

    @BeforeEach
    public void setUp() {
        lenient().when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authSubject);
        lenient().when(authSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        lenient().when(preparedUpdate.getUpdate()).thenReturn(update);
    }

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
        when(update.getSubmittedObject()).thenReturn(RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: OTHER-TEST\ntech-c: TECH-TEST"));

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(new RpslAttribute(AttributeType.ADMIN_C, "OTHER-TEST")));
        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(new RpslAttribute(AttributeType.TECH_C, "TECH-TEST")));
    }

    @Test
    public void self_referenced_adminC() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: NIC-TEST\ntech-c: TECH-TEST");
        when(update.getSubmittedObject()).thenReturn(role);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, times(1)).addMessage(preparedUpdate, role.findAttribute(AttributeType.ADMIN_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.ADMIN_C)));
        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.TECH_C)));
    }

    @Test
    public void self_referenced_techC() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: OTHER-TEST\ntech-c: NIC-TEST");
        when(update.getSubmittedObject()).thenReturn(role);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.ADMIN_C)));
        verify(updateContext, times(1)).addMessage(preparedUpdate,role.findAttribute(AttributeType.TECH_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.TECH_C)));
    }


    @Test
    public void self_referenced_techC_adminC() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: NIC-TEST\ntech-c: NIC-TEST");
        when(update.getSubmittedObject()).thenReturn(role);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, times(1)).addMessage(preparedUpdate, role.findAttribute(AttributeType.ADMIN_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.ADMIN_C)));
        verify(updateContext, times(1)).addMessage(preparedUpdate, role.findAttribute(AttributeType.TECH_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.TECH_C)));
    }

    @Test
    public void self_referenced_adminC_with_override() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: NIC-TEST\nadmin-c: NIC-TEST\ntech-c: TECH-TEST");
        when(update.getSubmittedObject()).thenReturn(role);
        when(authSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(preparedUpdate, role.findAttribute(AttributeType.ADMIN_C), UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.ADMIN_C)));
        verify(updateContext, never()).addMessage(preparedUpdate, UpdateMessages.selfReferenceError(role.findAttribute(AttributeType.TECH_C)));
    }

}
