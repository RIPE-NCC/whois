package net.ripe.db.whois.common.domain.attrs;

import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AddressPrefixRangeTest {

    @Test(expected = AttributeParseException.class)
    public void empty() {
        AddressPrefixRange.parse("");
    }

    @Test(expected = AttributeParseException.class)
    public void invalid_address() {
        AddressPrefixRange.parse("300.104.182.0/12");
    }

    @Test(expected = AttributeParseException.class)
    public void range_too_long() {
        AddressPrefixRange.parse("194.104.182.0/33");
    }

    @Test(expected = AttributeParseException.class)
    public void range_too_long_ipv6() {
        AddressPrefixRange.parse("2a00:c00::/129");
    }

    @Test
    public void operation_inclusive() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("194.104.182.0/24^+");

        assertThat(subject.getIpInterval().toString(), is("194.104.182.0/24"));
        assertThat(subject.getRangeOperation().getN(), is(24));
        assertThat(subject.getRangeOperation().getM(), is(32));
    }

    @Test
    public void operation_inclusive_ipv6() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("2a00:c00::/48^+");

        assertThat(subject.getIpInterval().toString(), is("2a00:c00::/48"));
        assertThat(subject.getRangeOperation().getN(), is(48));
        assertThat(subject.getRangeOperation().getM(), is(128));
    }

    @Test
    public void operation_exclusive() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("194.104.182.0/24^-");

        assertThat(subject.getIpInterval().toString(), is("194.104.182.0/24"));
        assertThat(subject.getRangeOperation().getN(), is(25));
        assertThat(subject.getRangeOperation().getM(), is(32));
    }

    @Test
    public void operation_exclusive_ipv6() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("2a00:c00::/48^-");

        assertThat(subject.getIpInterval().toString(), is("2a00:c00::/48"));
        assertThat(subject.getRangeOperation().getN(), is(49));
        assertThat(subject.getRangeOperation().getM(), is(128));
    }

    @Test
    public void no_operation() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("2a00:c00::/48");

        assertThat(subject.getIpInterval().toString(), is("2a00:c00::/48"));
        assertThat(subject.getRangeOperation().getN(), is(48));
        assertThat(subject.getRangeOperation().getM(), is(48));
    }

    @Test
    public void operation_range_ipv6() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("2a00:c00::/48^64-128");

        assertThat(subject.getIpInterval().toString(), is("2a00:c00::/48"));
        assertThat(subject.getRangeOperation().getN(), is(64));
        assertThat(subject.getRangeOperation().getM(), is(128));
    }

    @Test
    public void operation_range_ipv4() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("193.151.47.0/24^24");

        assertThat(subject.getIpInterval().toString(), is("193.151.47.0/24"));
        assertThat(subject.getRangeOperation().getN(), is(24));
        assertThat(subject.getRangeOperation().getM(), is(24));
    }

    @Test(expected = AttributeParseException.class)
    public void operation_range_ipv4_too_long() {
        AddressPrefixRange.parse("193.151.47.0/24^24-33");
    }

    @Test(expected = AttributeParseException.class)
    public void operation_range_ipv4_invalid_order() {
        AddressPrefixRange.parse("193.151.47.0/24^24-12");
    }

    @Test
    public void operation_length() {
        final AddressPrefixRange subject = AddressPrefixRange.parse("77.74.152.0/23^23");

        assertThat(subject.getIpInterval().toString(), is("77.74.152.0/23"));
        assertThat(subject.getRangeOperation().getN(), is(23));
        assertThat(subject.getRangeOperation().getM(), is(23));
    }

    @Test(expected = AttributeParseException.class)
    public void operation_length_too_long() {
        AddressPrefixRange.parse("77.74.152.0/23^33");
    }

    @Test(expected = AttributeParseException.class)
    public void n_lower_than_prefix_range() {
        AddressPrefixRange.parse("77.74.152.0/23^22");
    }

    @Test
    public void checkWithinBounds_same() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24").checkWithinBounds(Ipv4Resource.parse("193.0.0.0/24")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkWithinBounds_less_specific() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24").checkWithinBounds(Ipv4Resource.parse("193.0.0.0/8")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkWithinBounds_more_specific() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24").checkWithinBounds(Ipv4Resource.parse("193.0.0.0/32")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkWithinBounds_operation_exclude_more_specifics() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^-").checkWithinBounds(Ipv4Resource.parse("193.0.0.0/32")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_same_operation_unspecified() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24").checkRange(Ipv4Resource.parse("193.0.0.0/24")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_same_operation_exclude() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^-").checkRange(Ipv4Resource.parse("193.0.0.0/24")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_same_operation_include() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^+").checkRange(Ipv4Resource.parse("193.0.0.0/24")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_same_operation_range_n() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24").checkRange(Ipv4Resource.parse("193.0.0.0/24")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_same_operation_range_invalid_n() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_same_operation_range_n_m() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24-24").checkRange(Ipv4Resource.parse("193.0.0.0/24")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_less_specific_operation_unspecified() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24").checkRange(Ipv4Resource.parse("193.0.0.0/16")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_less_specific_operation_exclude() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^-").checkRange(Ipv4Resource.parse("193.0.0.0/16")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_less_specific_operation_include() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^+").checkRange(Ipv4Resource.parse("193.0.0.0/16")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_less_specific_operation_range_n() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24").checkRange(Ipv4Resource.parse("193.0.0.0/16")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_less_specific_operation_range_n_m() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24-24").checkRange(Ipv4Resource.parse("193.0.0.0/16")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_one_more_specific_operation_unspecified() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_one_more_specific_operation_exclude() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^-").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_one_more_specific_operation_include() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^+").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_one_more_specific_operation_range_n() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_one_more_specific_operation_range_n_and_1() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^25").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_one_more_specific_operation_range_n_and_2() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^26").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_one_more_specific_operation_range_n_m() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24-24").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_one_more_specific_operation_range_n_m_and_one() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24-25").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_one_more_specific_operation_range_n_m_and_two() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^24-26").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.SUCCESS));
    }

    @Test
    public void checkRange_one_more_specific_operation_range_n_and_two_m_and_two() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^26-26").checkRange(Ipv4Resource.parse("193.0.0.0/25")), is(AddressPrefixRange.BoundaryCheckResult.NOT_IN_BOUNDS));
    }

    @Test
    public void checkRange_expected_ipv4() {
        assertThat(AddressPrefixRange.parse("193.0.0.0/24^26-26").checkRange(Ipv6Resource.parse("aaaa::/16")), is(AddressPrefixRange.BoundaryCheckResult.IPV4_EXPECTED));
    }

    @Test
    public void checkRange_expected_ipv6() {
        assertThat(AddressPrefixRange.parse("aaaa::/16^26-26").checkRange(Ipv4Resource.parse("193.0.0.10")), is(AddressPrefixRange.BoundaryCheckResult.IPV6_EXPECTED));
    }
}
