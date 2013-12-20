package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReferencedTypeResolverTest {

    @Mock private RpslObjectDao rpslObjectDao;
    @InjectMocks private ReferencedTypeResolver subject;

    @Test
    public void auth_attribute_md5() {
        assertThat(subject.getReferencedType(AttributeType.AUTH, CIString.ciString("MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/")), is(nullValue()));
    }

    @Test
    public void auth_attribute_md5_filtered() {
        assertThat(subject.getReferencedType(AttributeType.AUTH, CIString.ciString("MD5-PW # Filtered")), is(nullValue()));
    }

    @Test
    public void auth_attribute_x509() {
        assertThat(subject.getReferencedType(AttributeType.AUTH, CIString.ciString("X509-1")), is("key-cert"));
    }

    @Test
    public void auth_attribute_pgpkey() {
        assertThat(subject.getReferencedType(AttributeType.AUTH, CIString.ciString("PGPKEY-28F6CD6C")), is("key-cert"));
    }

    @Test
    public void members_by_ref_any() {
        assertThat(subject.getReferencedType(AttributeType.MBRS_BY_REF, CIString.ciString("ANY")), is(nullValue()));
    }

    @Test
    public void members_by_ref_mntner() {
        assertThat(subject.getReferencedType(AttributeType.MBRS_BY_REF, CIString.ciString("OWNER-MNT")), is("mntner"));
    }

    @Test
    public void member_of_as_set() {
        assertThat(subject.getReferencedType(AttributeType.MEMBER_OF, CIString.ciString("AS-TEST")), is("as-set"));
    }

    @Test
    public void member_of_route_set() {
        assertThat(subject.getReferencedType(AttributeType.MEMBER_OF, CIString.ciString("rs-ripe")), is("route-set"));
    }

    @Test
    public void member_of_rtr_set() {
        assertThat(subject.getReferencedType(AttributeType.MEMBER_OF, CIString.ciString("RTRS-TEST")), is("rtr-set"));
    }

    @Test
    public void members_as_set() {
        assertThat(subject.getReferencedType(AttributeType.MEMBERS, CIString.ciString("AS-TEST")), is("as-set"));
    }

    @Test
    public void members_autnum() {
        assertThat(subject.getReferencedType(AttributeType.MEMBERS, CIString.ciString("AS100")), is("aut-num"));
    }

    @Test
    public void members_route_set() {
        assertThat(subject.getReferencedType(AttributeType.MEMBERS, CIString.ciString("rs-ripe")), is("route-set"));
    }

    @Test
    public void members_rtr_set() {
        assertThat(subject.getReferencedType(AttributeType.MEMBERS, CIString.ciString("RTRS-TEST")), is("rtr-set"));
    }

    @Test
    public void mnt_routes_any() {
        assertThat(subject.getReferencedType(AttributeType.MNT_ROUTES, CIString.ciString("ANY")), is(nullValue()));
    }

    @Test
    public void mnt_routes_with_curly_braces() {
        assertThat(subject.getReferencedType(AttributeType.MNT_ROUTES, CIString.ciString("{2a00:c00::/24,2a00:c00::/16}")), is(nullValue()));
    }

    @Test
    public void mnt_routes_mntner() {
        assertThat(subject.getReferencedType(AttributeType.MNT_ROUTES, CIString.ciString("OWNER-MNT")), is("mntner"));
    }

    @Test
    public void person() {
        when(rpslObjectDao.findByKey(ObjectType.PERSON, "PP1-RIPE")).thenReturn(new RpslObjectInfo(1, ObjectType.PERSON, "PP1-RIPE"));
        when(rpslObjectDao.findByKey(ObjectType.ROLE, "PP1-RIPE")).thenThrow(EmptyResultDataAccessException.class);

        assertThat(subject.getReferencedType(AttributeType.TECH_C, CIString.ciString("PP1-RIPE")), is("person"));
    }

    @Test
    public void role() {
        when(rpslObjectDao.findByKey(ObjectType.ROLE, "RR1-RIPE")).thenReturn(new RpslObjectInfo(1, ObjectType.ROLE, "RR1-RIPE"));
        when(rpslObjectDao.findByKey(ObjectType.PERSON, "RR1-RIPE")).thenThrow(EmptyResultDataAccessException.class);

        assertThat(subject.getReferencedType(AttributeType.TECH_C, CIString.ciString("RR1-RIPE")), is("role"));
    }
}
