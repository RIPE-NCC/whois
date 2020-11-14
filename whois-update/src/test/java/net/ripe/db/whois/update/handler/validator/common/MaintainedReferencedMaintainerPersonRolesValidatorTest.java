package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaintainedReferencedMaintainerPersonRolesValidatorTest {
    @Mock private RpslObjectDao rpslObjectDao;
    @Spy @InjectMocks private MaintainedReferencedMaintainerPersonRolesValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void no_mnt_by() {
        validateUpdate(subject, null, RpslObject.parse("mntner: foo"));

        verify(rpslObjectDao, never()).getByKey(eq(ObjectType.MNTNER), any(String.class));
    }

    @Test
    public void self_reference() {
        validateUpdate(subject, null, RpslObject.parse("mntner: foo\nmnt-by: foo"));

        verify(rpslObjectDao, never()).getByKey(eq(ObjectType.MNTNER), any(String.class));
    }

    @Test
    public void single_mnt_not_found() {
        when(rpslObjectDao.getByKey(any(ObjectType.class), any(String.class))).thenThrow(new EmptyResultDataAccessException(1));
        validateUpdate(subject, null, RpslObject.parse("mntner: foo\nmnt-by: one-maintainer-to-maintain-them-all"));

        verify(subject, never()).validateReferencedPersonsAndRoles(any(RpslObject.class));
    }

    @Test
    public void multiple_mnt_checked() {
        when(rpslObjectDao.getByKey(any(ObjectType.class), any(String.class))).thenReturn(RpslObject.parse("mntner: foo"));

        final RpslObject object = RpslObject.parse("mntner: foo\nmnt-by: one\nmnt-by: two, three\t, four");
        validateUpdate(subject, null, object);

        final int nrMaintainers = object.getValuesForAttribute(AttributeType.MNT_BY).size();
        verify(subject, times(nrMaintainers)).validateReferencedPersonsAndRoles(any(RpslObject.class));
    }
}
