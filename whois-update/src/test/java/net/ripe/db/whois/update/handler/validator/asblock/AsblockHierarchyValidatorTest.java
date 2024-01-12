package net.ripe.db.whois.update.handler.validator.asblock;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Subject;
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
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)

public class AsblockHierarchyValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject subjectObject;
    @Mock JdbcRpslObjectDao rpslObjectDao;

    @InjectMocks private AsblockHierarchyValidator subject;


    @Test
    public void testGetActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE));
    }

    @Test
    public void testGetTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.AS_BLOCK));
    }


    @Test
    public void validate_asBlock_Parent_exists() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-block: AS10 - AS20"));
        when(rpslObjectDao.findAsBlockIntersections(10L, 20L)).thenReturn(Lists.newArrayList(RpslObject.parse("as-block: AS1 - AS30")));

       subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.asblockParentAlreadyExists());
    }

    @Test
    public void validate_asBlock_intersects() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-block: AS10 - AS20"));
        when(rpslObjectDao.findAsBlockIntersections(10L, 20L)).thenReturn(Lists.newArrayList(RpslObject.parse("as-block: AS15 - AS30")));

       subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.intersectingAsblockAlreadyExists());
    }

    @Test
    public void validate_asBlock_child_exists() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-block: AS10 - AS15"));
       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.asblockChildAlreadyExists());
    }

    @Test
    public void validate_asBlock_intersects_on_boundaries() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-block: AS10 - AS15"));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.intersectingAsblockAlreadyExists());
    }

    @Test
    public void validate_already_exists() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-block: AS10 - AS15"));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.asblockAlreadyExists());
    }
}
