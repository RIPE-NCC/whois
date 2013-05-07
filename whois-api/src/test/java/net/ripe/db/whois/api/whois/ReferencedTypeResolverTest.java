package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ReferencedTypeResolverTest {

    @Test
    public void auth_attribute_md5() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.AUTH, CIString.ciString("MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/")), is(nullValue()));
    }

    @Test
    public void auth_attribute_md5_filtered() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.AUTH, CIString.ciString("MD5-PW # Filtered")), is(nullValue()));
    }

    @Test
    public void auth_attribute_x509() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.AUTH, CIString.ciString("X509-1")), is("key-cert"));
    }

    @Test
    public void auth_attribute_pgpkey() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.AUTH, CIString.ciString("PGPKEY-28F6CD6C")), is("key-cert"));
    }

    @Test
    public void members_by_ref_any() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MBRS_BY_REF, CIString.ciString("ANY")), is(nullValue()));
    }

    @Test
    public void members_by_ref_mntner() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MBRS_BY_REF, CIString.ciString("OWNER-MNT")), is("mntner"));
    }

    @Test
    public void member_of_as_set() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MEMBER_OF, CIString.ciString("AS-TEST")), is("as-set"));
    }

    @Test
    public void member_of_route_set() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MEMBER_OF, CIString.ciString("rs-ripe")), is("route-set"));
    }

    @Test
    public void member_of_rtr_set() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MEMBER_OF, CIString.ciString("RTRS-TEST")), is("rtr-set"));
    }

    @Test
    public void members_as_set() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MEMBERS, CIString.ciString("AS-TEST")), is("as-set"));
    }

    @Test
    public void members_autnum() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MEMBERS, CIString.ciString("AS100")), is("aut-num"));
    }

    @Test
    public void members_route_set() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MEMBERS, CIString.ciString("rs-ripe")), is("route-set"));
    }

    @Test
    public void members_rtr_set() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MEMBERS, CIString.ciString("RTRS-TEST")), is("rtr-set"));
    }

    @Test
    public void mnt_routes_any() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MNT_ROUTES, CIString.ciString("ANY")), is(nullValue()));
    }

    @Test
    public void mnt_routes_with_curly_braces() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MNT_ROUTES, CIString.ciString("{2a00:c00::/24,2a00:c00::/16}")), is(nullValue()));
    }

    @Test
    public void mnt_routes_mntner() {
        assertThat(ReferencedTypeResolver.getReferencedType(AttributeType.MNT_ROUTES, CIString.ciString("OWNER-MNT")), is("mntner"));
    }
}
