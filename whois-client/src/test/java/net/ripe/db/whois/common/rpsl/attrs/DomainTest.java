package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.junit.Test;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class DomainTest {

    @Test(expected = AttributeParseException.class)
    public void empty() {
        Domain.parse("");
    }

    @Test
    public void valid_ipv4() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa");
        assertThat(domain.getValue(), is(ciString("200.193.193.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("193.193.200/24")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test
    public void ipv4_dash() {
        final Domain domain = Domain.parse("0-127.10.10.10.in-addr.arpa");
        assertThat(domain.getValue(), is(ciString("0-127.10.10.10.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("10.10.10.0/25")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test(expected = AttributeParseException.class)
    public void ipv4_dash_invalid_position() {
        Domain.parse("0-127.10.10.in-addr.arpa");
    }

    @Test(expected = AttributeParseException.class)
    public void ipv4_dash_range_0_255() {
        Domain.parse("0-255.10.10.in-addr.arpa");
    }

    @Test(expected = AttributeParseException.class)
    public void ipv4_dash_range_start_is_range_end() {
        Domain.parse("1-1.10.10.in-addr.arpa");
    }

    @Test
    public void ipv4_dash_non_prefix_range() {
        final Domain domain = Domain.parse("1-2.10.10.10.in-addr.arpa");
        assertThat(domain.getValue(), is(ciString("1-2.10.10.10.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("10.10.10.1-10.10.10.2")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test
    public void valid_ipv4_trailing_dot() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa.");
        assertThat(domain.getValue(), is(ciString("200.193.193.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("193.193.200/24")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test
    public void valid_ipv6() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA");
        assertThat(domain.getValue(), is(ciString("0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa")));
        assertThat((Ipv6Resource) domain.getReverseIp(), is(Ipv6Resource.parse("2001:7f8::/48")));
        assertThat(domain.getType(), is(Domain.Type.IP6));
    }

    @Test
    public void valid_ipv6_trailing_dot() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa.");
        assertThat(domain.getValue(), is(ciString("0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa")));
        assertThat((Ipv6Resource) domain.getReverseIp(), is(Ipv6Resource.parse("2001:7f8::/48")));
        assertThat(domain.getType(), is(Domain.Type.IP6));
    }

    @Test
    public void ipv4_prefix_32_allowed() {
        Domain domain = Domain.parse("200.193.193.193.in-addr.arpa.");
        assertThat(domain.getValue(), is(ciString("200.193.193.193.in-addr.arpa")));
    }

    @Test(expected = AttributeParseException.class)
    public void suffix() {
        Domain.parse("200.193.193.193.some-suffix.");
    }

    @Test(expected = AttributeParseException.class)
    public void suffix_almost_correct() {
        Domain.parse("200.193.193.in-addraarpa");
    }

    @Test
    public void end_with_domain_enum() {
        final Domain domain = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
        assertThat(domain.endsWithDomain(ciString("a.ns.2.1.2.1.5.5.5.2.0.2.1.e164.arpa")), is(true));
    }

    @Test
    public void end_with_domain_enum_fails() {
        final Domain domain = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
        assertThat(domain.endsWithDomain(ciString("a.ns.2.1.2.1.5.5.5.2.0.e164.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv6() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA");
        assertThat(domain.endsWithDomain(ciString("a.ns.0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA")), is(true));
    }

    @Test
    public void end_with_domain_ipv6_fails() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA");
        assertThat(domain.endsWithDomain(ciString("a.ns.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA")), is(false));
    }

    @Test
    public void end_with_domain_ipv4() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("200.193.193.in-addr.arpa")), is(true));
    }

    @Test
    public void end_with_domain_ipv4_fails() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv4_dash() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.5.200.193.193.in-addr.arpa")), is(true));
    }

    @Test
    public void end_with_domain_ipv4_dash_no_match() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.5.200a193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv4_dash_outside_range_lower() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.0.200.193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv4_dash_outside_range_upper() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.100.200.193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void enum_domain() {
        final Domain domain = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
        assertThat(domain.getValue(), is(ciString("2.1.2.1.5.5.5.2.0.2.1.e164.arpa")));
        assertNull(domain.getReverseIp());
        assertThat(domain.getType(), is(Domain.Type.E164));
    }
}
