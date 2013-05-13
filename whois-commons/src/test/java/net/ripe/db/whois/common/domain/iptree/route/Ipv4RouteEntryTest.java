package net.ripe.db.whois.common.domain.iptree.route;

import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4RouteEntry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class Ipv4RouteEntryTest {
    @Test(expected = NullPointerException.class)
    public void testParse_null() {
        Ipv4RouteEntry.parse(null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_empty() {
        Ipv4RouteEntry.parse("", 0);
    }

    @Test
    public void testParse_valid() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0/32AS1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INETNUM));
        assertThat(entry.getKey().toString(), is("10.0.0.0/32"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test
    public void testParse_valid_range() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0 - 10.0.0.10 AS1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INETNUM));
        assertThat(entry.getKey().toString(), is("10.0.0.0 - 10.0.0.10"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test
    public void testParse_valid_lowercase() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0/32as1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INETNUM));
        assertThat(entry.getKey().toString(), is("10.0.0.0/32"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test
    public void testParse_valid_mixedcase() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0/32aS1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INETNUM));
        assertThat(entry.getKey().toString(), is("10.0.0.0/32"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_no_origin() {
        Ipv4RouteEntry.parse("10.0.0.0", 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_no_origin_number() {
        Ipv4RouteEntry.parse("10.0.0.0/32AS", 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_no_prefix() {
        Ipv4RouteEntry.parse("AS1234", 11);
    }

    @Test
    public void testParse_with_spaces() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INETNUM));
        assertThat(entry.getKey().toString(), is("10.0.0.0/32"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_v6() {
        Ipv4RouteEntry.parse("::0/0AS1234", 11);
    }

    @Test
    public void test_equals_same() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);

        assertThat(entry, is(entry));
    }

    @Test
    public void test_equals_equal() {
        final Ipv4RouteEntry entry1 = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);
        final Ipv4RouteEntry entry2 = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);

        assertThat(entry1, is(entry2));
        assertThat(entry1.hashCode(), is(entry2.hashCode()));
    }

    @Test
    public void test_equals_null() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);

        assertFalse(entry.equals(null));
    }

    @Test
    public void test_equals_other() {
        final Ipv4RouteEntry entry = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);

        assertFalse(entry.equals(new Ipv4Entry(Ipv4Resource.parse("10.0.0.0/32"), 11)));
    }

    @Test
    public void test_equals_same_origin() {
        final Ipv4RouteEntry entry1 = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);
        final Ipv4RouteEntry entry2 = Ipv4RouteEntry.parse("100.10.10.1/32 AS1234", 12);

        assertThat(entry1, not(is(entry2)));
    }

    @Test
    public void test_equals_same_prefix() {
        final Ipv4RouteEntry entry1 = Ipv4RouteEntry.parse("10.0.0.0/32 AS1234", 11);
        final Ipv4RouteEntry entry2 = Ipv4RouteEntry.parse("10.0.0.0/32 AS5678", 12);

        assertThat(entry1, not(is(entry2)));
    }
}
