package net.ripe.db.whois.update.handler.validator.inetnum;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static net.ripe.db.whois.common.Messages.Type.ERROR;
import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InetnumStrictStatusValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock RpslObjectDao objectDao;
    @Mock Ipv4Tree ipv4Tree;
    @Mock Subject authenticationSubject;
    @Mock Maintainers maintainers;
    @InjectMocks InetnumStrictStatusValidator subject;

    @Before
    public void setup() {
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"))).thenReturn(true);
    }

    @Test
    public void invalid_child_status_fails_ipv4() {
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse("192.0/32");
        final Ipv4Entry child = new Ipv4Entry(ipv4Resource, 1);
        when(ipv4Tree.findFirstMoreSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(child));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("inetnum: 192.0/32\nstatus: ASSIGNED PI"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.incorrectChildStatus(ERROR, "ALLOCATED PA", "ASSIGNED PI", "192.0/32"));
        verify(maintainers).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void not_authorized_by_rsmntner_ipv4() {
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255\nstatus: ALLOCATED UNSPECIFIED"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));
        when(ipv4Tree.findFirstMoreSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList());

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.statusRequiresAuthorization("ASSIGNED ANYCAST"));
        verify(maintainers, times(2)).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void not_authorized_by_rsmntner_ipv4_override() {
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255\nstatus: ALLOCATED UNSPECIFIED"));
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));
        when(ipv4Tree.findFirstMoreSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList());

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void parent_has_assigned_pa_status_and_grandparent_is_allocated_pa_and_has_rs_maintainer() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.168.1.0/24\nstatus: ASSIGNED PA"));
        Ipv4Entry parentEntry = new Ipv4Entry(Ipv4Resource.parse("192.168/16"), 1);
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("inetnum: 192.168/16\nstatus: ASSIGNED PA"));
        Ipv4Entry grandParentEntry = new Ipv4Entry(Ipv4Resource.parse("192/8"), 2);
        when(objectDao.getById(2)).thenReturn(RpslObject.parse("inetnum: 192/8\nstatus: ALLOCATED PA\nmnt-by: RIPE-NCC-HM-MNT"));
        when(ipv4Tree.findAllLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(parentEntry, grandParentEntry));
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(parentEntry));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, INETNUM, "ASSIGNED PA"));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verify(maintainers, times(2)).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void parent_has_assigned_pa_status_and_grandparent_is_allocated_pa_but_does_not_have_rs_maintainer() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.168.1.0/24\nstatus: ASSIGNED PA"));
        Ipv4Entry parentEntry = new Ipv4Entry(Ipv4Resource.parse("192.168/16"), 1);
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("inetnum: 192.168/16\nstatus: ASSIGNED PA"));
        Ipv4Entry grandParentEntry = new Ipv4Entry(Ipv4Resource.parse("192/8"), 2);
        when(objectDao.getById(2)).thenReturn(RpslObject.parse("inetnum: 192/8\nstatus: ALLOCATED PA"));
        when(ipv4Tree.findAllLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(parentEntry, grandParentEntry));
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(parentEntry));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, INETNUM, "ASSIGNED PA"));
        verify(maintainers, times(3)).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void incorrect_parent_status_ipv4() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED PI"));
        when(ipv4Tree.findFirstMoreSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList());
        Ipv4Entry parentEntry = new Ipv4Entry(Ipv4Resource.parse("192.0/16"), 1);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(parentEntry));
        final RpslObject parent = RpslObject.parse("inetnum: 192.0/16\nstatus: SUB-ALLOCATED PA");
        when(objectDao.getById(1)).thenReturn(parent);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, INETNUM, "SUB-ALLOCATED PA"));
        verify(maintainers).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void incorrect_parent_status_ipv4_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED PI"));
        when(ipv4Tree.findFirstMoreSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList());
        Ipv4Entry parentEntry = new Ipv4Entry(Ipv4Resource.parse("192.0/16"), 1);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(parentEntry));
        final RpslObject parent = RpslObject.parse("inetnum: 192.0/16\nstatus: SUB-ALLOCATED PA");
        when(objectDao.getById(1)).thenReturn(parent);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, INETNUM, "SUB-ALLOCATED PA"));
    }

    @Test
    public void correct_parent_status_ipv4() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED PI\nmnt-by: RIPE-NCC-HM-MNT"));
        when(ipv4Tree.findFirstMoreSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList());
        Ipv4Entry parentEntry = new Ipv4Entry(Ipv4Resource.parse("192.0/16"), 1);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.<Ipv4Entry>newArrayList(parentEntry));
        final RpslObject parent = RpslObject.parse("inetnum: 192.0/16\nstatus: ALLOCATED UNSPECIFIED");
        when(objectDao.getById(1)).thenReturn(parent);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void create_inetnum_w_legacy_allowed_under_legacy_w_non_rs_maintainer() {
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("" +
                "inetnum: 0.0.0.0 - 255.255.255.255\n" +
                "status: LEGACY"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.inetnumStatusLegacy());
    }

    @Test
    public void create_inetnum_w_legacy_not_allowed_under_unspecified_w_non_rs_maintainer() {
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("" +
                "inetnum: 0.0.0.0 - 255.255.255.255\n" +
                "status: ALLOCATED UNSPECIFIED"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.inetnumStatusLegacy());
    }

    @Test
    public void create_inetnum_w_legacy_allowed_under_unspecified_w_rs_maintainer() {
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("" +
                "inetnum: 0.0.0.0 - 255.255.255.255\n" +
                "status: ALLOCATED UNSPECIFIED"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.inetnumStatusLegacy());
    }

    @Test
    public void create_inetnum_w_legacy_not_allowed_under_wrong_status_w_rs_maintainer() {
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("" +
                "inetnum: 0.0.0.0 - 255.255.255.255\n" +
                "status: LIR-PARTITIONED PA"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.inetnumStatusLegacy());
        verify(updateContext, times(1)).addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, INETNUM, "LIR-PARTITIONED PA"));
    }

    @Test
    public void validate_invalid_parent_interval() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: ASSIGNED PA"));
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Collections.<Ipv4Entry>emptyList());

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidParentEntryForInterval(Ipv4Resource.parse("192.0/24")));
        verifyNoMoreInteractions(updateContext);
    }

}
