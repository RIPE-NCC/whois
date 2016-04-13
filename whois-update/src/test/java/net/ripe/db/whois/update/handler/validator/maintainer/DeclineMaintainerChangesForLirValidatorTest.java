package net.ripe.db.whois.update.handler.validator.maintainer;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeclineMaintainerChangesForLirValidatorTest {

    @Mock
    private UpdateContext updateContext;
    @Mock
    private PreparedUpdate update;
    @Mock
    private RpslObjectDao rpslObjectDao;
    @InjectMocks
    private DeclineMaintainerChangesForLirValidator subject;

    public static final RpslObject MNTNER = RpslObject.parse(10, "" +
            "mntner: MNT-TEST1\n" +
            "org:    ORG-TEST1\n" +
            "mnt-by: TEST-MNT");

    private static final RpslObject MNTNER_ORG = RpslObject.parse("" +
            "organisation: ORG-TEST1\n" +
            "org-name:     Test Organisation\n" +
            "org-type:     LIR\n" +
            "address:      street and number, city, country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n" +
            "mnt-by:       TEST-MNT");

    @Test
    public void should_fail_with_decline_maintainer_changes_for_lir() {
        when(update.getUpdatedObject()).thenReturn(MNTNER);
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, CIString.ciString("ORG-TEST1"))).thenReturn(MNTNER_ORG);

        subject.validate(update, updateContext);

        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.declineMaintainerChangesForLir("MNT-TEST1"));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void should_fail_with_reference_not_found() {
        when(update.getUpdatedObject()).thenReturn(MNTNER);

        subject.validate(update, updateContext);

        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.referenceNotFound("ORG-TEST1"));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.MNTNER));
    }
}
