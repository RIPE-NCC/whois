package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    public void setup() {
        lenient().when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        lenient().when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"))).thenReturn(true);
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

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.MNT_BY));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
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

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
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
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, new Message(Messages.Type.WARNING, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.MNT_BY).getText(), AttributeType.MNT_BY.getName()));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
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
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
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

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.MNT_BY));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
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

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.MNT_BY));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
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

        when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT", "TEST2-MNT"))).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.MNT_BY));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT", "TEST2-MNT"));
        verifyNoMoreInteractions(maintainers);
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
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
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
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
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

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
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
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(rpslOriginalObject);
        when(update.getUpdatedObject()).thenReturn(rpslUpdatedlObject);

       subject.validate(update, updateContext);
        verify(updateContext).addMessage(update, new Message(Messages.Type.WARNING, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.MNT_BY).getText(), AttributeType.MNT_BY.getName()));
    }
}
