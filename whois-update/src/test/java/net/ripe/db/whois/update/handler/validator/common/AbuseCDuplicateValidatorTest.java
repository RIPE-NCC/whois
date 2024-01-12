package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbuseCDuplicateValidatorTest {


    @Mock
    private UpdateContext updateContext;
    @Mock
    private PreparedUpdate update;
    @Mock
    private Ipv4Tree ipv4Tree;
    @Mock
    private Ipv6Tree ipv6Tree;
    @Mock
    private RpslObjectDao rpslObjectDao;
    @InjectMocks
    private AbuseCDuplicateValidator subject;

    @Test
    public void no_abuse_c() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.168.0.0 - 192.168.255.255\nsource: TEST"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }


    @Test
    public void has_abuse_c_but_no_org() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.168.0.0 - 192.168.255.255\nabuse-c: AA1-TEST\nsource: TEST"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void org_abuse_c_matches_inetnum_abuse_c() {
        when(update.getUpdatedObject()).thenReturn(
            RpslObject.parse(
                "inetnum: 192.168.0.0 - 192.168.255.255\n" +
                "org: ORG-AA1-TEST\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));
        when(rpslObjectDao.getByKeyOrNull(ObjectType.ORGANISATION, CIString.ciString("ORG-AA1-TEST"))).thenReturn(
            RpslObject.parse(
                "organisation: ORG-AA1-TEST\n" +
                "org-name: Any Any\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.duplicateAbuseC(CIString.ciString("AA1-TEST"), CIString.ciString("ORG-AA1-TEST")));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void org_abuse_c_does_not_match_inetnum_abuse_c() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.168.0.0 - 192.168.255.255\norg: ORG-AA1-TEST\nabuse-c: AA1-TEST\nsource: TEST"));
        when(rpslObjectDao.getByKeyOrNull(ObjectType.ORGANISATION, CIString.ciString("ORG-AA1-TEST"))).thenReturn(
            RpslObject.parse(
                "organisation: ORG-AA1-TEST\n" +
                "org-name: Any Any\n" +
                "abuse-c: BB1-TEST\n" +
                "source: TEST"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void org_abuse_c_matches_inet6num_abuse_c() {
        when(update.getUpdatedObject()).thenReturn(
            RpslObject.parse(
                "inet6num: 2001:67c:2e8::/48\n" +
                "org: ORG-AA1-TEST\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));
        when(rpslObjectDao.getByKeyOrNull(ObjectType.ORGANISATION, CIString.ciString("ORG-AA1-TEST"))).thenReturn(
            RpslObject.parse(
                "organisation: ORG-AA1-TEST\n" +
                "org-name: Any Any\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.duplicateAbuseC(CIString.ciString("AA1-TEST"), CIString.ciString("ORG-AA1-TEST")));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void org_abuse_c_matches_autnum_abuse_c() {
        when(update.getUpdatedObject()).thenReturn(
            RpslObject.parse(
                "aut-num: AS3333\n" +
                "org: ORG-AA1-TEST\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));
        when(rpslObjectDao.getByKeyOrNull(ObjectType.ORGANISATION, CIString.ciString("ORG-AA1-TEST"))).thenReturn(
            RpslObject.parse(
                "organisation: ORG-AA1-TEST\n" +
                "org-name: Any Any\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.duplicateAbuseC(CIString.ciString("AA1-TEST"), CIString.ciString("ORG-AA1-TEST")));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void org_abuse_c_matches_parent_inetnum_abuse_c() {
        when(update.getUpdatedObject()).thenReturn(
            RpslObject.parse(
                "inetnum: 192.168.0.0 - 192.168.255.255\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));
        when(ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse("192.168.0.0/16"))).thenReturn(
            Collections.singletonList(
                new Ipv4Entry(Ipv4Resource.parse("192.168.0.0/16"), 101)));
        when(rpslObjectDao.getById(101)).thenReturn(
            RpslObject.parse(
                "inetnum: 192.0.0.0 - 192.255.255.255\n" +
                "org: ORG-AA1-TEST\n" +
                "source: TEST"));
        when(rpslObjectDao.getByKeyOrNull(ObjectType.ORGANISATION, CIString.ciString("ORG-AA1-TEST"))).thenReturn(
            RpslObject.parse(
                "organisation: ORG-AA1-TEST\n" +
                "org-name: Any Any\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.duplicateAbuseC(CIString.ciString("AA1-TEST"), CIString.ciString("ORG-AA1-TEST")));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void org_abuse_c_matches_parent_inet6num_abuse_c() {
        when(update.getUpdatedObject()).thenReturn(
            RpslObject.parse(
                "inet6num: 2001:67c:2e8::/48\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));
        when(ipv6Tree.findFirstLessSpecific(Ipv6Resource.parse("2001:67c:2e8::/48"))).thenReturn(
            Collections.singletonList(
                new Ipv6Entry(Ipv6Resource.parse("2001:67c::/32"), 102)));
        when(rpslObjectDao.getById(102)).thenReturn(
            RpslObject.parse(
                "inet6num: 2001:67c::/32\n" +
                "org: ORG-AA1-TEST\n" +
                "source: TEST"));
        when(rpslObjectDao.getByKeyOrNull(ObjectType.ORGANISATION, CIString.ciString("ORG-AA1-TEST"))).thenReturn(
            RpslObject.parse(
                "organisation: ORG-AA1-TEST\n" +
                "org-name: Any Any\n" +
                "abuse-c: AA1-TEST\n" +
                "source: TEST"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.duplicateAbuseC(CIString.ciString("AA1-TEST"), CIString.ciString("ORG-AA1-TEST")));
        verifyNoMoreInteractions(updateContext);
    }
}
