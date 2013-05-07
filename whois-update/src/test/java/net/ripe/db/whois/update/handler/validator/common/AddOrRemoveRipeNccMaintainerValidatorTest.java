package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AddOrRemoveRipeNccMaintainerValidatorTest {
    @Mock Subject authSubject;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock Maintainers maintainers;
    @InjectMocks AddOrRemoveRipeNccMaintainerValidator subject;

    @Before
    public void setUp() throws Exception {
        when(maintainers.getRsMaintainers()).thenReturn(ciSet("RS-MNT"));
        when(maintainers.getDbmMaintainers()).thenReturn(ciSet("DBM-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authSubject);
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
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DEV-MNT\n"));

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("DEV-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added() {
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: RS-MNT\n"));

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("RS-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added_mnt_domains() {
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-domains: RS-MNT\n"));

        when(update.getDifferences(AttributeType.MNT_DOMAINS)).thenReturn(ciSet("RS-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added_mnt_lower() {
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-lower: RS-MNT\n"));

        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("RS-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_added_mnt_routes() {
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-routes: RS-MNT {ANY}\n"));

        when(update.getDifferences(AttributeType.MNT_ROUTES)).thenReturn(ciSet("RS-MNT {ANY}"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_rs_auth_rs_maintainer_added() {
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: RS-MNT\n"));

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("RS-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void validate_dbm_auth_dbm_maintainer_added() {
        when(authSubject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(true);
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DBM-MNT\n" +
                "mnt-lower: HM-MNT\n"));

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("DBM-MNT"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("HM-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void validate_dbm_auth_dbm_maintainer_added_by_rs_maintainer() {
        when(authSubject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(false);
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DBM-MNT\n" +
                "mnt-lower: HM-MNT\n"));

        when(update.getDifferences(AttributeType.MNT_BY)).thenReturn(ciSet("DBM-MNT"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("HM-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForChangingRipeMaintainer());
    }

    @Test
    public void validate_added_override() {
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.isOverride()).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-routes: RS-MNT {ANY}\n"));

        when(update.getDifferences(AttributeType.MNT_ROUTES)).thenReturn(ciSet("RS-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }
}
