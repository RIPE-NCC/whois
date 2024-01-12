package net.ripe.db.whois.query.planner;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.dao.AbuseValidationStatusDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbuseCFinderTest {

    @Mock Ipv4Tree ipv4Tree;
    @Mock Ipv6Tree ipv6Tree;
    @Mock RpslObjectDao objectDao;
    @Mock Maintainers maintainers;
    @Mock AbuseValidationStatusDao abuseValidationStatusDao;

    @InjectMocks AbuseCFinder subject;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(subject, "mainSources", ImmutableSet.of(CIString.ciString("RIPE")));
    }

    @Test
    public void inetnum_with_org_reference_without_abusec() {
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG-TEST1\nsource: RIPE");
        final RpslObject parent = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\nmnt-by: RS2-MNT\nsource: RIPE");

        when(objectDao.getByKey(ObjectType.ORGANISATION, ciString("ORG-TEST1"))).thenReturn(RpslObject.parse("organisation: ORG-TEST1\n"));
        when(ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(inetnum.getKey()))).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse(parent.getKey()), 5)));
        when(objectDao.getById(5)).thenReturn(parent);

        final Optional<AbuseContact> abuseContact = subject.getAbuseContact(inetnum);

        assertThat(abuseContact.isPresent(), is(false));

        verify(maintainers).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void inetnum_with_org_reference_with_abusec() {
        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT\nsource: RIPE");
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG-TEST1\nsource: RIPE");

        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(inetnum.getKey());
        when(objectDao.getByKey(ObjectType.ORGANISATION, ciString("ORG-TEST1"))).thenReturn(RpslObject.parse("organisation: ORG-TEST1\nabuse-c: ABU-TEST"));
        when(objectDao.getByKey(ObjectType.ROLE, ciString("ABU-TEST"))).thenReturn(RpslObject.parse("role: abuse role\nabuse-mailbox: abuse@ripe.net\nnic-hdl: ABU-TEST"));

        assertThat(subject.getAbuseContact(inetnum).get().getAbuseMailbox(), is("abuse@ripe.net"));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void inetnum_with_lir_org_reference_with_suspect_abusec() {
        when(abuseValidationStatusDao.isSuspect(any(CIString.class))).thenReturn(true);
        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT\nsource: RIPE");
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG-TEST1\nsource: RIPE");

        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(inetnum.getKey());
        when(objectDao.getByKey(ObjectType.ORGANISATION, ciString("ORG-TEST1"))).thenReturn(RpslObject.parse("organisation: ORG-TEST1\norg-type: LIR\nabuse-c: ABU-TEST"));
        when(objectDao.getByKey(ObjectType.ROLE, ciString("ABU-TEST"))).thenReturn(RpslObject.parse("role: abuse role\nabuse-mailbox: abuse@ripe.net\nnic-hdl: ABU-TEST"));

        assertThat(subject.getAbuseContact(inetnum).get().getAbuseMailbox(), is("abuse@ripe.net"));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void inetnum_with_abusec_and_org_reference_with_abusec() {
        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT\nsource: RIPE");
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG-TEST1\nabuse-c: AH1-TEST\nsource: RIPE");

        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(inetnum.getKey());
        when(objectDao.getByKey(ObjectType.ROLE, ciString("AH1-TEST"))).thenReturn(
                RpslObject.parse("role: another abuse role\nabuse-mailbox: more_abuse@ripe.net\nnic-hdl: ABU-TEST")
        );

        assertThat(subject.getAbuseContact(inetnum).get().getAbuseMailbox(), is("more_abuse@ripe.net"));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void inetnum_without_org_reference() {
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0\nsource: RIPE");

        assertThat(subject.getAbuseContact(inetnum).isPresent(), is(false));
    }

    @Test
    public void inetnum_root_parent_with_abusec() {
        final RpslObject object = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG1-TEST\nsource: RIPE");

        when(objectDao.getByKey(ObjectType.ORGANISATION, ciString("ORG1-TEST"))).thenReturn(RpslObject.parse("organisation: ORG1-TEST\nabuse-c: AB-TEST"));
        when(objectDao.getByKey(ObjectType.ROLE, ciString("AB-TEST"))).thenReturn(RpslObject.parse("role: A Role\nabuse-mailbox: abuse@ripe.net\nnic-hdl: AB-TEST"));

        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT\nsource: RIPE");
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(object.getKey());

        assertThat(subject.getAbuseContact(object).get().getAbuseMailbox(), is("abuse@ripe.net"));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void inetnum_identical_parent_with_abusec() {
        final RpslObject child = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\nsource: RIPE");
        final RpslObject parent = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\norg: ORG1-TEST\nsource: RIPE");
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(parent.getKey());
        final Ipv4Entry ipv4Entry = new Ipv4Entry(ipv4Resource, 1);

        when(ipv4Tree.findFirstLessSpecific(ipv4Resource)).thenReturn(Lists.newArrayList(ipv4Entry));
        when(objectDao.getByKey(ObjectType.ORGANISATION, ciString("ORG1-TEST"))).thenReturn(RpslObject.parse("organisation: ORG1-TEST\nabuse-c: AB-TEST\nsource: RIPE"));
        when(objectDao.getByKey(ObjectType.ROLE, ciString("AB-TEST"))).thenReturn(RpslObject.parse("role: A Role\nabuse-mailbox: abuse@ripe.net\nnic-hdl: AB-TEST\nsource: RIPE"));
        when(objectDao.getById(1)).thenReturn(parent);

        assertThat(subject.getAbuseContact(child).get().getAbuseMailbox(), is("abuse@ripe.net"));

        verify(maintainers).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void inetnum_rs_maintained() {
        final RpslObject object = RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255\nmnt-lower: RS2-MNT\nsource: RIPE");

        final RpslObject root = RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\norg: ORG-TEST1\nmnt-by:RS1-MNT\nsource: RIPE");
        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(object.getKey());
        when(ipv4Tree.findFirstLessSpecific(ipv4Resource)).thenReturn(Lists.newArrayList(new Ipv4Entry(Ipv4Resource.parse(root.getKey()), 1)));
        when(objectDao.getById(1)).thenReturn(root);

        assertThat(subject.getAbuseContact(object).isPresent(), is(false));

        verify(maintainers).isRsMaintainer(ciSet("RS2-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void autnum_with_abusec() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS8462\norg: ORG1-TEST\nsource: RIPE");

        when(objectDao.getByKey(ObjectType.ORGANISATION, ciString("ORG1-TEST"))).thenReturn(RpslObject.parse("organisation: ORG1-TEST\nabuse-c: AB-TEST"));
        when(objectDao.getByKey(ObjectType.ROLE, ciString("AB-TEST"))).thenReturn(RpslObject.parse("role: Abuse Role\nnic-hdl: AB-TEST\nabuse-mailbox: abuse@ripe.net"));

        assertThat(subject.getAbuseContact(autnum).get().getAbuseMailbox(), is("abuse@ripe.net"));
        verifyNoMoreInteractions(ipv4Tree);
        verifyNoMoreInteractions(ipv6Tree);

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void autnum_without_abusec() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS8462\norg: ORG1-TEST\nsource: RIPE");

        when(objectDao.getByKey(ObjectType.ORGANISATION, ciString("ORG1-TEST"))).thenReturn(RpslObject.parse("organisation: ORG1-TEST\nsource: RIPE"));

        assertThat(subject.getAbuseContact(autnum).isPresent(), is(false));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void getAbuseContacts_rootObject() {
        final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0\nsource: RIPE");

        assertThat(subject.getAbuseContact(inetnum).isPresent(), is(false));

        verifyNoMoreInteractions(maintainers);

    }
}
