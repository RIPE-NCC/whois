package net.ripe.db.whois.update.handler.validator.inetnum;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InetnumStatusValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock StatusDao statusDao;
    @Mock Ipv4Tree ipv4Tree;
    @Mock Subject authenticationSubject;
    @Mock Maintainers maintainers;
    @InjectMocks InetnumStatusValidator subject;

    @Before
    public void setup() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
    }

    @Test
    public void delete_inetnum_w_legacy_not_allowed_under_unspecified_w_non_rs_maintainer() {
        when(update.getAction()).thenReturn(Action.DELETE);
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(statusDao.getStatus(1)).thenReturn(CIString.ciString("ALLOCATED UNSPECIFIED"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: LEGACY\n" +
                "mnt-by: TEST-MNT\n" +
                "password: update"));

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.inetnumStatusLegacy());
    }

    @Test
    public void modify_status_change() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: ASSIGNED PI"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum: 192.0/24\n" +
                "status: ASSIGNED PA"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.statusChange());
    }

}
