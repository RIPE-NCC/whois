package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectReferencedValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock RpslObjectUpdateDao rpslObjectUpdateDao;
    @InjectMocks ObjectReferencedValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.DELETE));
    }

    @Test
    public void validate_no_original_object() {
        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_not_referenced() {
        final RpslObject object = RpslObject.parse("mntner: TST-MNT");
        when(update.getReferenceObject()).thenReturn(object);
        when(rpslObjectUpdateDao.getInvalidReferences(object)).thenReturn(Collections.<RpslAttribute, Set<CIString>>emptyMap());
        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_referenced() {
        final RpslObject object = RpslObject.parse("mntner: TST-MNT\nadmin-c: ADMIN-NC");

        when(update.getType()).thenReturn(ObjectType.MNTNER);
        when(update.hasOriginalObject()).thenReturn(true);
        when(update.getReferenceObject()).thenReturn(object);
        when(rpslObjectUpdateDao.isReferenced(object)).thenReturn(true);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.objectInUse(object));
    }

    @Test
    public void validate_referenced_autnum() {
        final RpslObject object = RpslObject.parse("aut-num: AS1");

        when(update.getType()).thenReturn(ObjectType.AUT_NUM);
        when(update.hasOriginalObject()).thenReturn(true);
        when(update.getReferenceObject()).thenReturn(object);
        when(rpslObjectUpdateDao.isReferenced(object)).thenReturn(true);
        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
