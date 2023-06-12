package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_not_referenced() {
        final RpslObject object = RpslObject.parse("mntner: TST-MNT");
       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
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

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }
}
