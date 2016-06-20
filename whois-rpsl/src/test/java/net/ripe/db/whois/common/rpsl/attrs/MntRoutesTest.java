package net.ripe.db.whois.common.rpsl.attrs;

import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MntRoutesTest {

    @Test(expected = AttributeParseException.class)
    public void empty() {
        MntRoutes.parse("");
    }

    @Test
    public void maintainer_only() {
        final MntRoutes subject = MntRoutes.parse("RIPE-NCC-RPSL-MNT");

        assertThat(subject.getMaintainer().toString(), is("RIPE-NCC-RPSL-MNT"));
        assertThat(subject.isAnyRange(), is(true));
        assertThat(subject.getAddressPrefixRanges(), hasSize(0));
    }

    @Test
    public void maintainer_with_any() {
        final MntRoutes subject = MntRoutes.parse("RIPE-NCC-RPSL-MNT ANY");

        assertThat(subject.getMaintainer().toString(), is("RIPE-NCC-RPSL-MNT"));
        assertThat(subject.isAnyRange(), is(true));
        assertThat(subject.getAddressPrefixRanges(), hasSize(0));
    }

    @Test(expected = AttributeParseException.class)
    public void maintainer_with_any_and_range() {
        MntRoutes.parse("RIPE-NCC-RPSL-MNT { ANY,194.104.182.0/24^+ }");
    }

    @Test
    public void maintainer_with_addres_prefix_range() {
        final MntRoutes subject = MntRoutes.parse("AS286-MNT {194.104.182.0/24^+}");

        assertThat(subject.getMaintainer().toString(), is("AS286-MNT"));
        assertThat(subject.isAnyRange(), is(false));
        assertThat(subject.getAddressPrefixRanges(), hasSize(1));
        assertThat(subject.getAddressPrefixRanges().get(0).toString(), is("194.104.182.0/24^+"));
    }

    @Test
    public void maintainer_with_addres_prefix_ranges() {
        final MntRoutes subject = MntRoutes.parse("TEST-MNT {194.9.240.0/24,194.9.241.0/24}");

        assertThat(subject.getMaintainer().toString(), is("TEST-MNT"));
        assertThat(subject.isAnyRange(), is(false));
        assertThat(subject.getAddressPrefixRanges(), hasSize(2));
        assertThat(subject.getAddressPrefixRanges().get(0).getIpInterval().toString(), is("194.9.240.0/24"));
        assertThat(subject.getAddressPrefixRanges().get(1).getIpInterval().toString(), is("194.9.241.0/24"));
    }

    @Test(expected = AttributeParseException.class)
    public void maintainer_with_any_inside_brackets() {
        MntRoutes.parse("TEST-MNT { ANY }");
    }

    @Test(expected = AttributeParseException.class)
    public void maintainer_with_any_inside_brackets_no_padding_space() {
        MntRoutes.parse("TEST-MNT {ANY}");
    }
}
