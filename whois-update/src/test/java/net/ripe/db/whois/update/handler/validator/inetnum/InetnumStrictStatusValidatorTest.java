package net.ripe.db.whois.update.handler.validator.inetnum;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.RpslObject;
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

import static net.ripe.db.whois.common.Messages.Type.WARNING;
import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: [ES] Replace these unmaintainable unit tests with integration tests
@ExtendWith(MockitoExtension.class)
public class InetnumStrictStatusValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock RpslObjectDao objectDao;
    @Mock StatusDao statusDao;
    @Mock Ipv4Tree ipv4Tree;
    @Mock Subject authenticationSubject;
    @Mock Maintainers maintainers;
    @InjectMocks InetnumStrictStatusValidator subject;

    @BeforeEach
    public void setup() {
        lenient().when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        lenient().when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"))).thenReturn(true);
        lenient().when(update.getAction()).thenReturn(Action.CREATE);
    }

    @Test
    public void not_authorized_by_rsmntner_ipv4() {
        lenient().when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        lenient().when(statusDao.getStatus(1)).thenReturn(CIString.ciString("ALLOCATED UNSPECIFIED"));
        lenient().when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));
        lenient().when(ipv4Tree.findFirstMoreSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList());

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.statusRequiresAuthorization("ASSIGNED ANYCAST"));
    }

    @Test
    public void not_authorized_by_rsmntner_ipv4_override() {
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        lenient().when(statusDao.getStatus(1)).thenReturn(CIString.ciString("ALLOCATED UNSPECIFIED"));
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, new Message(WARNING, UpdateMessages.statusRequiresAuthorization("ASSIGNED ANYCAST").getText(), UpdateMessages.statusRequiresAuthorization("ASSIGNED ANYCAST").getArgs()));
    }

    @Test
    public void create_inetnum_w_legacy_allowed_under_legacy_w_non_rs_maintainer() {
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        lenient().when(statusDao.getStatus(1)).thenReturn(CIString.ciString("LEGACY"));
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
        lenient().when(statusDao.getStatus(1)).thenReturn(CIString.ciString("ALLOCATED UNSPECIFIED"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n"));

       subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.inetnumStatusLegacy());
    }

    @Test
    public void create_inetnum_w_legacy_allowed_under_unspecified_w_rs_maintainer() {
        lenient().when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        lenient().when(statusDao.getStatus(1)).thenReturn(CIString.ciString("ALLOCATED UNSPECIFIED"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: LEGACY\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n"));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.inetnumStatusLegacy());
    }

}
