package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LirMntByValidatorTest {
    @Mock
    private UpdateContext updateContext;
    @Mock
    private PreparedUpdate update;
    @Mock
    private Maintainers maintainers;
    @Mock
    private Subject authenticationSubject;

    @InjectMocks
    private LirMntByValidator subject;

    @Before
    public void setup() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        when(maintainers.getRsMaintainers()).thenReturn(ciSet("RIPE-NCC-HM-MNT"));
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.INETNUM, ObjectType.INET6NUM));
    }

    @Test
    public void modify_mntby_on_inetnum_with_lir() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST2-MNT");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.MNT_BY));
    }

    @Test
    public void modify_mntby_on_inetnum_with_rs() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST2-MNT");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void modify_mntby_on_inetnum_with_override() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST2-MNT");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void mntby_unchanged() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void modify_mntby_remove_rs_mntner_on_inetnum_with_lir() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       TEST-MNT");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.MNT_BY));
    }

    @Test
    public void modify_mntby_remove_own_mntner_on_inetnum_with_lir() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.MNT_BY));
    }

    @Test
    public void modify_mntby_multiple_on_inetnum_with_lir() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "mnt-by:       TEST2-MNT");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST4-MNT");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.MNT_BY));
    }

    @Test
    public void modify_mntby_add_lir_mntner_on_inetnum_with_rs() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void modify_mntby_add_lir_mntner_on_inetnum_with_override() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void modify_mntby_delete_lir_mntner_on_inetnum_with_rs() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void modify_mntby_delete_lir_mntner_on_inetnum_with_override() {
        final RpslObject rpslOriginalObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n");
        final RpslObject rpslUpdatedlObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n");
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }
}