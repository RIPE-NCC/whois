package net.ripe.db.whois.update.handler.validator.autnum;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AutnumStatus;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutnumStatusValidatorTest {
    @Mock PreparedUpdate preparedUpdate;
    @Mock UpdateContext updateContext;
    @Mock Maintainers maintainers;
    @Mock Subject subjectObject;

    @InjectMocks AutnumStatusValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.AUT_NUM));
    }


    @Test
    @Ignore // pending clarification
    public void create_override_no_status() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_override_status_assigned() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_override_status_legacy() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_override_status_other() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    @Ignore // pending clarification
    public void create_rsmaintainer_no_status() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    @Ignore // pending clarification
    public void create_rsmaintainer_status_assigned() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_rsmaintainer_status_legacy() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_rsmaintainer_status_other() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    public void create_userauth_no_status() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_userauth_status_assigned() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_userauth_mntby_rs_status_assigned() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_userauth_status_legacy() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void create_userauth_status_other() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(autnum, autnum, Action.CREATE, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    public void modify_override_adding_status_legacy() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_override_adding_status_other() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_override_adding_status_assigned() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    @Ignore // waiting for clarification
    public void modify_override_removing_status() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    public void modify_override_changing_status_LEGACY_to_ASSIGNED() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_override_changing_status_LEGACY_to_OTHER() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_override_changing_status_OTHER_to_ASSIGNED() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_override_changing_status_OTHER_to_LEGACY() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_override_changing_status_ASSIGNED_to_LEGACY() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_override_changing_status_ASSIGNED_to_OTHER() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.TRUE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    @Ignore //pending clarification
    public void modify_rsmaintainer_adding_status_legacy() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_rsmaintainer_adding_status_assigned() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_rsmaintainer_adding_status_other() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    @Ignore //pending clarification
    public void modify_rsmaintainer_removing_status() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    public void modify_rsmaintainer_changing_status_LEGACY_to_ASSIGNED() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_rsmaintainer_changing_status_LEGACY_to_OTHER() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_rsmaintainer_changing_status_ASSIGNED_to_OTHER() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_rsmaintainer_changing_status_ASSIGNED_to_LEGACY() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_rsmaintainer_changing_status_OTHER_to_ASSIGNED() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    @Ignore // pending clarification
    public void modify_rsmaintainer_changing_status_OTHER_to_LEGACY() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT"));
        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.TRUE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(preparedUpdate, UpdateMessages.invalidStatusMustBeOther(AutnumStatus.LEGACY));
    }


    @Test
    @Ignore // pending clarification, story description is contradictive
    public void modify_userauth_adding_status_other() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_userauth_adding_status_legacy() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_userauth_adding_status_assigned() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    public void modify_userauth_removing_status() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }


    @Test
    public void modify_userauth_changing_status_LEGACY_to_ASSIGNED() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_userauth_changing_status_LEGACY_to_OTHER() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_userauth_changing_status_ASSIGNED_to_OTHER() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext, never()).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_userauth_changing_status_ASSIGNED_to_LEGACY() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }

    @Test
    public void modify_userauth_changing_status_OTHER_to_ASSIGNED() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: ASSIGNED\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(preparedUpdate, UpdateMessages.invalidStatusMustBeOther(AutnumStatus.ASSIGNED));
    }

    @Test
    public void modify_userauth_changing_status_OTHER_to_LEGACY() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        setupStubs(original, update, Action.MODIFY, Boolean.FALSE, Boolean.FALSE);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(eq(preparedUpdate), any(Message.class));
    }


    private void setupStubs(final RpslObject original, final RpslObject update, final Action action, final boolean isOverride, final boolean isRsMaintainer) {
        when(preparedUpdate.getAction()).thenReturn(action);
        when(preparedUpdate.getReferenceObject()).thenReturn(original);
        when(preparedUpdate.getUpdatedObject()).thenReturn(update);
        when(updateContext.getSubject(preparedUpdate)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(isOverride);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(isRsMaintainer);
    }
}
