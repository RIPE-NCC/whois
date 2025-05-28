package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AddOrRemoveRipeNccMaintainerValidatorTest {
    @Mock Subject authSubject;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock Maintainers maintainers;
    @InjectMocks AddOrRemoveRipeNccMaintainerValidator subject;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(maintainers.getRsMaintainers()).thenReturn(ciSet("RS-MNT"));
        lenient().when(maintainers.getDbmMaintainers()).thenReturn(ciSet("DBM-MNT"));
        lenient().when(updateContext.getSubject(update)).thenReturn(authSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.values()));
    }

    @Test
    public void validate_no_rs_auth_no_rs_maintainer_added() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("DEV-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("RS-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added_mnt_domains() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        lenient().when(update.getDifferences(AttributeType.MNT_DOMAINS)).thenReturn(ciSet("RS-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added_mnt_lower() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        lenient().when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("RS-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added_mnt_routes() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        lenient().when(update.getDifferences(AttributeType.MNT_ROUTES)).thenReturn(ciSet("RS-MNT ANY"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_rs_auth_rs_maintainer_added() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        lenient().when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("RS-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void validate_dbm_auth_dbm_maintainer_added() {
        lenient().when(authSubject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(true);
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("DBM-MNT"));
        lenient().when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("HM-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void validate_dbm_auth_dbm_maintainer_added_by_rs_maintainer() {
        lenient().when(authSubject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(false);
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("DBM-MNT"));
        lenient().when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("HM-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added_mnt_ref() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        lenient().when(update.getDifferences(AttributeType.MNT_REF)).thenReturn(ciSet("RS-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_dbm_auth_dbm_maintainer_added_mnt_ref() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        lenient().when(update.getDifferences(AttributeType.MNT_REF)).thenReturn(ciSet("DBM-MNT"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_added_override() {
        when(authSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authSubject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(false);
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }
}
