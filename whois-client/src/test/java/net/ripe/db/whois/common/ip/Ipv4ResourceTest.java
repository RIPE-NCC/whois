package net.ripe.db.whois.common.ip;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Ipv4ResourceTest {

    private Ipv4Resource subject;

    // Some sugar
    private Matcher<Long> eq(long value) {
        return org.hamcrest.Matchers.is(value);
    }

    private Matcher<Integer> eq(int value) {
        return org.hamcrest.Matchers.is(value);
    }

    @Test
    public void parseValidIPv4Range() {
        subject = Ipv4Resource.parse("212.219.1.0 - 212.219.1.255");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122687L));
    }

    @Test
    public void parseValidIPv4Address() {
        subject = Ipv4Resource.parse("212.219.1.0");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122432L));
    }

    @Test
    public void parseValidIPv4ARangeWithSlash() {
        subject = Ipv4Resource.parse("212.219.1.0/24");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122687L));
    }

    @Test
    public void parseValidIPv4ARangeWithSlashAndNewLine() {
        subject = Ipv4Resource.parse("212.219.1.0/24\r\n");
        assertThat(subject.begin(), eq(3571122432L));
        assertThat(subject.end(), eq(3571122687L));
    }

    @Test
    public void ipv4_with_prefix_21() {
        subject = Ipv4Resource.parse("151.64.0.1/21\r\n");
        assertThat(subject.begin(), eq(2537553920L));
        assertThat(subject.end(), eq(2537555967L));
    }

    @Test
    public void ipv4_with_prefix_23() {
        subject = Ipv4Resource.parse("109.73.65.0/23\r\n");
        assertThat(subject.begin(), eq(1833517056L));
        assertThat(subject.end(), eq(1833517567L));
    }

    @Test
    public void ipv4_with_prefix_28() {
        subject = Ipv4Resource.parse("62.219.43.72/28\r\n");
        assertThat(subject.begin(), eq(1054550848L));
        assertThat(subject.end(), eq(1054550863L));
    }

    @Test
    public void ipv4_with_prefix_31() {
        subject = Ipv4Resource.parse("162.219.43.72/31\r\n");
        assertThat(subject.begin(), eq(2732272456L));
        assertThat(subject.end(), eq(2732272457L));
    }

    @Test
    public void ipv4_with_prefix_32() {
        subject = Ipv4Resource.parse("162.219.43.72/32\r\n");
        assertThat(subject.begin(), eq(2732272456L));
        assertThat(subject.end(), eq(2732272456L));
    }

    @Test
    public void zero_slash_zero_with_prefix_32() {
        subject = Ipv4Resource.parse("0/32\r\n");
        assertThat(subject.begin(), eq(0L));
        assertThat(subject.end(), eq(0L));
    }

    @Test
    public void zero_slash_zero_with_prefix_zero() {
        subject = Ipv4Resource.parse("0/0\r\n");
        assertThat(subject.begin(), eq(0L));
        assertThat(subject.end(), eq(4294967295L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResource() {
        Ipv4Resource.parse("invalid resource");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResourceType() {
        Ipv4Resource.parse("::0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginEndBeforeBeginFails() {
        subject = new Ipv4Resource(2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginOutOfBoundsFails() {
        subject = new Ipv4Resource(Long.MIN_VALUE, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEndOutOfBoundsFails() {
        subject = new Ipv4Resource(1, Long.MAX_VALUE);
    }

    @Test
    public void createReturnsCorrectBeginAndEnd() {
        subject = new Ipv4Resource(1, 2);

        assertThat(subject.begin(), eq(1L));
        assertThat(subject.end(), eq(2L));
    }

    @Test
    public void maxRangeContainsEverything() {
        assertTrue(Ipv4Resource.MAX_RANGE.contains(new Ipv4Resource(Ipv4Resource.MAX_RANGE.begin(), Ipv4Resource.MAX_RANGE.begin())));
        assertTrue(Ipv4Resource.MAX_RANGE.contains(new Ipv4Resource(Ipv4Resource.MAX_RANGE.end(), Ipv4Resource.MAX_RANGE.end())));
        assertTrue(Ipv4Resource.MAX_RANGE.contains(new Ipv4Resource(Ipv4Resource.MAX_RANGE.begin(), Ipv4Resource.MAX_RANGE.end())));
        assertTrue(Ipv4Resource.MAX_RANGE.contains(new Ipv4Resource(1231250, 123097120)));
    }

    @Test
    public void compareUpperBounds() {
        assertEquals(0, Ipv4Resource.MAX_RANGE.compareUpperBound(Ipv4Resource.MAX_RANGE));
        assertEquals(-1, Ipv4Resource.parse("127.0.0.0/8").compareUpperBound(Ipv4Resource.MAX_RANGE));
        assertEquals(1, Ipv4Resource.MAX_RANGE.compareUpperBound(Ipv4Resource.parse("127.0.0.0/8")));
    }

    @Test
    public void singletonIntervalAtLowerBound() {
        assertEquals(Ipv4Resource.parse("127.0.0.0/32"), Ipv4Resource.parse("127.0.0.0/8").singletonIntervalAtLowerBound());
    }

    @Test
    public void compareWorks() {
        subject = new Ipv4Resource(10, 20);

        assertThat(0, eq(subject.compareTo(subject)));
        assertThat(1, eq(subject.compareTo(new Ipv4Resource(0, 9))));
        assertThat(-1, eq(subject.compareTo(new Ipv4Resource(21, 30))));

        assertThat(1, eq(subject.compareTo(new Ipv4Resource(0, 15))));
        assertThat(-1, eq(subject.compareTo(new Ipv4Resource(15, 30))));

        assertThat(1, eq(subject.compareTo(new Ipv4Resource(0, 20))));
        assertThat(1, eq(subject.compareTo(new Ipv4Resource(10, 30))));

        assertThat(-1, eq(subject.compareTo(new Ipv4Resource(11, 30))));

        assertThat(-1, eq(subject.compareTo(new Ipv4Resource(10, 19))));
        assertThat(-1, eq(subject.compareTo(new Ipv4Resource(11, 20))));
    }

    @Test
    public void verifyIntersects() {
        subject = new Ipv4Resource(10, 20);

        assertTrue(subject.intersects(subject));
        assertTrue(subject.intersects(Ipv4Resource.MAX_RANGE));

        assertFalse(subject.intersects(new Ipv4Resource(9, 9)));
        assertTrue(subject.intersects(new Ipv4Resource(9, 10)));
        assertTrue(subject.intersects(new Ipv4Resource(10, 11)));
        assertTrue(subject.intersects(new Ipv4Resource(5, 15)));

        assertFalse(subject.intersects(new Ipv4Resource(21, 21)));
        assertTrue(subject.intersects(new Ipv4Resource(19, 20)));
        assertTrue(subject.intersects(new Ipv4Resource(20, 21)));
        assertTrue(subject.intersects(new Ipv4Resource(15, 25)));
    }

    @Test
    public void verifyEquals() {
        subject = Ipv4Resource.parse("212.219.1.0/24");

        assertTrue(subject.equals(subject));
        assertFalse(subject.equals(Ipv4Resource.MAX_RANGE));
        assertFalse(subject.equals(null));
        assertFalse(subject.equals("Random object"));
        assertThat(subject, not(Ipv4Resource.parse("212.218.1.0/24")));

        assertThat(subject, is(Ipv4Resource.parse("212.219.1.0 - 212.219.1.255")));
    }

    @Test
    public void verifyHashcode() {
        subject = Ipv4Resource.parse("212.219.1.0/24");
        Ipv4Resource test = Ipv4Resource.parse("212.219.1.0 - 212.219.1.255");

        assertThat(subject, is(test));
        assertThat(subject.hashCode(), is(test.hashCode()));
    }

    @Test
    public void toStringOfSlashNotation() {
        Ipv4Resource subject = Ipv4Resource.parse("212.219.1.0/24");

        assertThat(subject.toString(), is("212.219.1.0/24"));
    }

    @Test
    public void toStringOfDashNotation() {
        Ipv4Resource subject = Ipv4Resource.parse("212.219.1.0 - 212.219.1.255");

        assertThat(subject.toString(), is("212.219.1.0/24"));
    }

    @Test
    public void toStringOfSingleResource() {
        Ipv4Resource subject = Ipv4Resource.parse("212.219.1.0");

        assertThat(subject.toString(), is("212.219.1.0/32"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_empty() {
        Ipv4Resource.parseReverseDomain("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_null() {
        Ipv4Resource.parseReverseDomain(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_inaddrarpa() {
        Ipv4Resource.parseReverseDomain("1.2.3.4");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_octets() {
        Ipv4Resource.parseReverseDomain(".in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_more_than_four_octets() {
        Ipv4Resource.parseReverseDomain("8.7.6.5.4.3.2.1.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_dash_not_in_fourth_octet() {
        Ipv4Resource.parseReverseDomain("1-1.1.1.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_non_numeric_input() {
        Ipv4Resource.parseReverseDomain("1-1.b.a.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_multiple_dashes() {
        Ipv4Resource.parseReverseDomain("1-1.2-2.3-3.4-4.in-addr.arpa");
    }

    @Test
    public void reverse_simple() {
        assertThat(Ipv4Resource.parseReverseDomain("111.in-addr.arpa").toString(), is("111.0.0.0/8"));
        assertThat(Ipv4Resource.parseReverseDomain("22.111.in-addr.arpa").toString(), is("111.22.0.0/16"));
        assertThat(Ipv4Resource.parseReverseDomain("3.22.111.in-addr.arpa").toString(), is("111.22.3.0/24"));
        assertThat(Ipv4Resource.parseReverseDomain("4.3.22.111.in-addr.arpa").toString(), is("111.22.3.4/32"));
    }

    @Test
    public void reverse_simple_with_trailing_dot_and_mixed_caps() {
        assertThat(Ipv4Resource.parseReverseDomain("111.in-addr.arpa.").toString(), is("111.0.0.0/8"));
        assertThat(Ipv4Resource.parseReverseDomain("22.111.In-addr.arpa.").toString(), is("111.22.0.0/16"));
        assertThat(Ipv4Resource.parseReverseDomain("3.22.111.iN-aDdR.aRpA.").toString(), is("111.22.3.0/24"));
        assertThat(Ipv4Resource.parseReverseDomain("4.3.22.111.IN-ADDR.ARPA.").toString(), is("111.22.3.4/32"));
    }

    @Test
    public void reverse_with_range() {
        assertThat(Ipv4Resource.parseReverseDomain("44-55.33.22.11.in-addr.arpa.").toString(), is("11.22.33.44 - 11.22.33.55"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_inverse_range() {
        Ipv4Resource.parseReverseDomain("80-28.79.198.195.in-addr.arpa");
    }

    @Test
    public void parsePrefixWithLength() {
        assertThat(Ipv4Resource.parsePrefixWithLength(0, 0).toString(), is("0.0.0.0/0"));
        assertThat(Ipv4Resource.parsePrefixWithLength(0xffffffff, 0).toString(), is("0.0.0.0/0"));

        assertThat(Ipv4Resource.parsePrefixWithLength(0, 32).toString(), is("0.0.0.0/32"));
        assertThat(Ipv4Resource.parsePrefixWithLength(0xffffffff, 32).toString(), is("255.255.255.255/32"));

        assertThat(Ipv4Resource.parsePrefixWithLength(0xDEADBEEF, 13).toString(), is("222.168.0.0/13"));
        assertThat(Ipv4Resource.parsePrefixWithLength(0xCAFEBABE, 26).toString(), is("202.254.186.128/26"));
    }
}
