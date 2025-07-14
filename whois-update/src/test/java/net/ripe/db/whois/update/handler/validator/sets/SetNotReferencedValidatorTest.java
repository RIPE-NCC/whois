package net.ripe.db.whois.update.handler.validator.sets;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SetNotReferencedValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock RpslObjectDao objectDao;

    @InjectMocks SetNotReferencedValidator subject;

    @Test
    public void testGetActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.DELETE));
    }

    @Test
    public void testGetTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.AS_SET, ObjectType.ROUTE_SET, ObjectType.RTR_SET));
    }

    @Test
    public void set_that_has_incoming_references() {
        final RpslObject routeSet = RpslObject.parse("route-set: rs-AH");
        when(update.getUpdatedObject()).thenReturn(routeSet);
        when(objectDao.findMemberOfByObjectTypeWithoutMbrsByRef(ObjectType.ROUTE_SET, "rs-AH")).thenReturn(Lists.newArrayList(new RpslObjectInfo(1, ObjectType.ROUTE, "192.168.0.1/32")));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.objectInUse(routeSet));
    }


    @Test
    public void set_that_has_no_incoming_references() {
        final RpslObject asSet = RpslObject.parse("as-set: AS1325:AS-lopp");
        when(update.getUpdatedObject()).thenReturn(asSet);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.objectInUse(asSet));
    }
}
