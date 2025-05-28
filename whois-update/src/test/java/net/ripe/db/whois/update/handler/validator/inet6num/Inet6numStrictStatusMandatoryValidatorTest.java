package net.ripe.db.whois.update.handler.validator.inet6num;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static net.ripe.db.whois.common.Messages.Type.ERROR;
import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: [ES] Replace these unmaintainable unit tests with integration tests
@ExtendWith(MockitoExtension.class)
public class Inet6numStrictStatusMandatoryValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock StatusDao statusDao;
    @Mock Ipv6Tree ipv6Tree;
    @Mock Subject authenticationSubject;
    @Mock Maintainers maintainers;
    @InjectMocks Inet6numStrictStatusMandatoryValidator subject;

    @BeforeEach
    public void setup() {
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        lenient().when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"))).thenReturn(true);
        lenient().when(statusDao.getStatus(anyList())).thenReturn(Collections.emptyMap());
    }

    @Test
    public void incorrect_parent_status_ipv6() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: 2001::/48\nstatus: ASSIGNED PI\nmnt-by: RIPE-NCC-HM-MNT\n"));
        when(ipv6Tree.findFirstMoreSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList());
        Ipv6Entry parentEntry = new Ipv6Entry(Ipv6Resource.parse("2001::/24"), 1);
        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList(parentEntry));
        when(statusDao.getStatus(1)).thenReturn(CIString.ciString("ALLOCATED-BY-LIR"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, ObjectType.INET6NUM, "ALLOCATED-BY-LIR"));
    }

    @Test
    public void correct_parent_status_ipv6() {
        lenient().when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: 2001::/48\nstatus: ASSIGNED PI\nmnt-by: RIPE-NCC-HM-MNT\n"));
        when(ipv6Tree.findFirstMoreSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList());
        Ipv6Entry parentEntry = new Ipv6Entry(Ipv6Resource.parse("2001::/24"), 1);
        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList(parentEntry));
        when(statusDao.getStatus(1)).thenReturn(CIString.ciString("ALLOCATED-BY-RIR"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

}
