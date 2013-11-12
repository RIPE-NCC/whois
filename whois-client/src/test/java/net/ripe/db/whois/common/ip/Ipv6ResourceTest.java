package net.ripe.db.whois.common.ip;

import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class Ipv6ResourceTest {
    private Ipv6Resource subject;

    private static Ipv6Resource resource(long begin, long end) {
        return new Ipv6Resource(BigInteger.valueOf(begin), BigInteger.valueOf(end));
    }

    @Test
    public void parseValidIPv6Prefix() {
        subject = Ipv6Resource.parse("::f000/116");
        assertThat(subject.begin(), is(BigInteger.valueOf(61440)));
        assertThat(subject.end(), is(BigInteger.valueOf(65535)));
    }

    @Test
    public void parseValidIPv6Address() {
        subject = Ipv6Resource.parse("::2001");
        assertThat(subject.begin(), is(BigInteger.valueOf(8193)));
        assertThat(subject.end(), is(BigInteger.valueOf(8193)));
    }

    @Test
    public void parseValidIPv6ARangeWithSlash() {
        subject = Ipv6Resource.parse("2001::/64");
        assertThat(subject.begin(), is(new BigInteger("42540488161975842760550356425300246528")));
        assertThat(subject.end(), is(new BigInteger("42540488161975842778997100499009798143")));
    }

    @Test
    public void valid_ipv6_with_prefix_48() {
        subject = Ipv6Resource.parse("2a00:1f78::fffe/48");
        assertThat(subject.begin(), is(new BigInteger("55828214085043681575463550121838379008")));
        assertThat(subject.end(), is(new BigInteger("55828214085044890501283164751013085183")));
    }

    @Test
    public void parseValidIPv6ARangeWithSlashAndNewLine() {
        subject = Ipv6Resource.parse("2001::/64\r\n");
        assertThat(subject.begin(), is(new BigInteger("42540488161975842760550356425300246528")));
        assertThat(subject.end(), is(new BigInteger("42540488161975842778997100499009798143")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseIpv6MappedIpv4Fails() {
        subject = Ipv6Resource.parse("::ffff:192.0.2.128");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResource() {
        Ipv6Resource.parse("invalid resource");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidResourceType() {
        Ipv6Resource.parse("12.0.0.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void Ipv6RangeThrowsIllegalArgumentException() {
        Ipv6Resource.parse("2001:: - 2020::");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginEndBeforeBeginFails() {
        subject = resource(2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBeginOutOfBoundsFails() {
        subject = resource(Long.MIN_VALUE, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEndOutOfBoundsFails() {
        subject = new Ipv6Resource(BigInteger.ONE, BigInteger.ONE.shiftLeft(128));
    }

    @Test
    public void createReturnsCorrectBeginAndEnd() {
        subject = resource(1, 2);

        assertThat(subject.begin(), is(BigInteger.valueOf(1L)));
        assertThat(subject.end(), is(BigInteger.valueOf(2L)));
    }

    @Test
    public void maxRangeContainsEverything() {
        assertTrue(Ipv6Resource.MAX_RANGE.contains(new Ipv6Resource(Ipv6Resource.MAX_RANGE.begin(), Ipv6Resource.MAX_RANGE.begin())));
        assertTrue(Ipv6Resource.MAX_RANGE.contains(new Ipv6Resource(Ipv6Resource.MAX_RANGE.end(), Ipv6Resource.MAX_RANGE.end())));
        assertTrue(Ipv6Resource.MAX_RANGE.contains(new Ipv6Resource(Ipv6Resource.MAX_RANGE.begin(), Ipv6Resource.MAX_RANGE.end())));
        assertTrue(Ipv6Resource.MAX_RANGE.contains(resource(1231250, 123097120)));
    }

    @Test
    public void compareUpperBounds() {
        assertEquals(0, Ipv6Resource.MAX_RANGE.compareUpperBound(Ipv6Resource.MAX_RANGE));
        assertEquals(-1, Ipv6Resource.parse("2001:ffce::/32").compareUpperBound(Ipv6Resource.MAX_RANGE));
        assertEquals(1, Ipv6Resource.MAX_RANGE.compareUpperBound(Ipv6Resource.parse("2001:ffce::/32")));
    }

    @Test
    public void singletonIntervalAtLowerBound() {
        assertEquals(Ipv6Resource.parse("2001::/128"), Ipv6Resource.parse("2001::/77").singletonIntervalAtLowerBound());
    }

    @Test
    public void compareWorks() {
        subject = resource(10, 20);

        assertThat(0, is(subject.compareTo(subject)));
        assertThat(1, is(subject.compareTo(resource(0, 9))));
        assertThat(-1, is(subject.compareTo(resource(21, 30))));

        assertThat(1, is(subject.compareTo(resource(0, 15))));
        assertThat(-1, is(subject.compareTo(resource(15, 30))));

        assertThat(1, is(subject.compareTo(resource(0, 20))));
        assertThat(1, is(subject.compareTo(resource(10, 30))));

        assertThat(-1, is(subject.compareTo(resource(11, 30))));

        assertThat(-1, is(subject.compareTo(resource(10, 19))));
        assertThat(-1, is(subject.compareTo(resource(11, 20))));
    }

    @Test
    public void verifyIntersects() {
        subject = resource(10, 20);

        assertTrue(subject.intersects(subject));
        assertTrue(subject.intersects(Ipv6Resource.MAX_RANGE));

        assertFalse(subject.intersects(resource(9, 9)));
        assertTrue(subject.intersects(resource(9, 10)));
        assertTrue(subject.intersects(resource(9, 15)));
        assertTrue(subject.intersects(resource(9, 20)));
        assertTrue(subject.intersects(resource(9, 21)));

        assertTrue(subject.intersects(resource(10, 10)));
        assertTrue(subject.intersects(resource(10, 15)));
        assertTrue(subject.intersects(resource(10, 20)));
        assertTrue(subject.intersects(resource(10, 21)));

        assertTrue(subject.intersects(resource(15, 15)));
        assertTrue(subject.intersects(resource(15, 20)));
        assertTrue(subject.intersects(resource(15, 21)));

        assertTrue(subject.intersects(resource(20, 20)));
        assertTrue(subject.intersects(resource(20, 21)));

        assertFalse(subject.intersects(resource(21, 21)));
    }

    @Test
    public void verifyEquals() {
        subject = Ipv6Resource.parse("2001::/64");

        assertTrue(subject.equals(subject));
        assertFalse(subject.equals(Ipv6Resource.MAX_RANGE));
        assertFalse(subject.equals(null));
        assertFalse(subject.equals("Random object"));
        assertThat(subject, not(Ipv6Resource.parse("ffce::/64")));

        assertThat(subject, is(Ipv6Resource.parse("2001:0:0:0:0:1:2:3/64")));
    }

    @Test
    public void verifyHashcode() {
        subject = Ipv6Resource.parse("2001::/64");
        Ipv6Resource test = Ipv6Resource.parse("2001:0:0:0:0::2:3/64");

        assertThat(subject, is(test));
        assertThat(subject.hashCode(), is(test.hashCode()));
    }

    @Test
    public void toStringOfSlashNotation() {
        assertThat(Ipv6Resource.parse("2001::/64").toString(), is("2001::/64"));
        assertThat(Ipv6Resource.parse("2001:0:0:0:0::2:3/64").toString(), is("2001::/64"));
        assertThat(Ipv6Resource.parse("2001:00:0000::0/64").toString(), is("2001::/64"));
    }

    @Test
    public void toStringBitBoundaryTest() {
        assertThat(Ipv6Resource.parse("1234:4567:89ab::/20").toString(), is("1234:4000::/20"));
        assertThat(Ipv6Resource.parse("1234:4567:89ab::/15").toString(), is("1234::/15"));
        assertThat(Ipv6Resource.parse("1234:4567:89ab::/14").toString(), is("1234::/14"));
        assertThat(Ipv6Resource.parse("1234:4567:89ab::/13").toString(), is("1230::/13"));
        assertThat(Ipv6Resource.parse("1234:4567:89ab::/12").toString(), is("1230::/12"));
        assertThat(Ipv6Resource.parse("ffff:4567:89ab:cdef::/4").toString(), is("f000::/4"));
        assertThat(Ipv6Resource.parse("ffff:4567:89ab:cdef::/3").toString(), is("e000::/3"));
        assertThat(Ipv6Resource.parse("ffff:4567:89ab:cdef::/2").toString(), is("c000::/2"));
        assertThat(Ipv6Resource.parse("ffff:4567:89ab:cdef::/1").toString(), is("8000::/1"));
        assertThat(Ipv6Resource.parse("1234:4567:89ab:cdef::/0").toString(), is("::/0"));
    }

    @Test
    public void toStringOfSingleResource() {
        assertThat(Ipv6Resource.parse("2001::").toString(), is("2001::/128"));
    }

    @Test
    public void toStringOfEntireAddressSpace() {
        assertThat(Ipv6Resource.parse("::/0").toString(), is("::/0"));
    }

    @Test
    public void toStringOfLocalhost() {
        assertThat(Ipv6Resource.parse("::1").toString(), is("::1/128"));
    }

    @Test
    public void toStringOfZero() {
        assertThat(Ipv6Resource.parse("::").toString(), is("::/128"));
    }

    private int compare(long aMsb, long aLsb, long bMsb, long bLsb) {
        return Ipv6Resource.compare(aMsb, aLsb, bMsb, bLsb);
    }

    @Test
    public void doubleLongUnsignedComparison() {
        assertThat(compare( 0,  0,  0,  0), is(0));
        assertThat(compare( 0,  0,  0, -1), is(-1));
        assertThat(compare( 0,  0,  0,  1), is(-1));

        assertThat(compare( 0,  0, -1,  0), is(-1));
        assertThat(compare( 0,  0, -1, -1), is(-1));
        assertThat(compare( 0,  0, -1,  1), is(-1));

        assertThat(compare( 0,  0,  1,  0), is(-1));
        assertThat(compare( 0,  0,  1, -1), is(-1));
        assertThat(compare( 0,  0,  1,  1), is(-1));

        assertThat(compare( 0, -1,  0,  0), is(1));
        assertThat(compare( 0, -1,  0, -1), is(0));
        assertThat(compare( 0, -1,  0,  1), is(1));

        assertThat(compare( 0, -1, -1,  0), is(-1));
        assertThat(compare( 0, -1, -1, -1), is(-1));
        assertThat(compare( 0, -1, -1,  1), is(-1));

        assertThat(compare( 0, -1,  1,  0), is(-1));
        assertThat(compare( 0, -1,  1, -1), is(-1));
        assertThat(compare( 0, -1,  1,  1), is(-1));

        assertThat(compare( 0,  1,  0,  0), is(1));
        assertThat(compare( 0,  1,  0, -1), is(-1));
        assertThat(compare( 0,  1,  0,  1), is(0));

        assertThat(compare( 0,  1, -1,  0), is(-1));
        assertThat(compare( 0,  1, -1, -1), is(-1));
        assertThat(compare( 0,  1, -1,  1), is(-1));

        assertThat(compare( 0,  1,  1,  0), is(-1));
        assertThat(compare( 0,  1,  1, -1), is(-1));
        assertThat(compare( 0,  1,  1,  1), is(-1));

        assertThat(compare(-1,  0,  0,  0), is(1));
        assertThat(compare(-1,  0,  0, -1), is(1));
        assertThat(compare(-1,  0,  0,  1), is(1));

        assertThat(compare(-1,  0, -1,  0), is(0));
        assertThat(compare(-1,  0, -1, -1), is(-1));
        assertThat(compare(-1,  0, -1,  1), is(-1));

        assertThat(compare(-1,  0,  1,  0), is(1));
        assertThat(compare(-1,  0,  1, -1), is(1));
        assertThat(compare(-1,  0,  1,  1), is(1));

        assertThat(compare(-1, -1,  0,  0), is(1));
        assertThat(compare(-1, -1,  0, -1), is(1));
        assertThat(compare(-1, -1,  0,  1), is(1));

        assertThat(compare(-1, -1, -1,  0), is(1));
        assertThat(compare(-1, -1, -1, -1), is(0));
        assertThat(compare(-1, -1, -1,  1), is(1));

        assertThat(compare(-1, -1,  1,  0), is(1));
        assertThat(compare(-1, -1,  1, -1), is(1));
        assertThat(compare(-1, -1,  1,  1), is(1));

        assertThat(compare(-1,  1,  0,  0), is(1));
        assertThat(compare(-1,  1,  0, -1), is(1));
        assertThat(compare(-1,  1,  0,  1), is(1));

        assertThat(compare(-1,  1, -1,  0), is(1));
        assertThat(compare(-1,  1, -1, -1), is(-1));
        assertThat(compare(-1,  1, -1,  1), is(0));

        assertThat(compare(-1,  1,  1,  0), is(1));
        assertThat(compare(-1,  1,  1, -1), is(1));
        assertThat(compare(-1,  1,  1,  1), is(1));

        assertThat(compare( 1,  0,  0,  0), is(1));
        assertThat(compare( 1,  0,  0, -1), is(1));
        assertThat(compare( 1,  0,  0,  1), is(1));

        assertThat(compare( 1,  0, -1,  0), is(-1));
        assertThat(compare( 1,  0, -1, -1), is(-1));
        assertThat(compare( 1,  0, -1,  1), is(-1));

        assertThat(compare( 1,  0,  1,  0), is(0));
        assertThat(compare( 1,  0,  1, -1), is(-1));
        assertThat(compare( 1,  0,  1,  1), is(-1));

        assertThat(compare( 1, -1,  0,  0), is(1));
        assertThat(compare( 1, -1,  0, -1), is(1));
        assertThat(compare( 1, -1,  0,  1), is(1));

        assertThat(compare( 1, -1, -1,  0), is(-1));
        assertThat(compare( 1, -1, -1, -1), is(-1));
        assertThat(compare( 1, -1, -1,  1), is(-1));

        assertThat(compare( 1, -1,  1,  0), is(1));
        assertThat(compare( 1, -1,  1, -1), is(0));
        assertThat(compare( 1, -1,  1,  1), is(1));

        assertThat(compare( 1,  1,  0,  0), is(1));
        assertThat(compare( 1,  1,  0, -1), is(1));
        assertThat(compare( 1,  1,  0,  1), is(1));

        assertThat(compare( 1,  1, -1,  0), is(-1));
        assertThat(compare( 1,  1, -1, -1), is(-1));
        assertThat(compare( 1,  1, -1,  1), is(-1));

        assertThat(compare( 1,  1,  1,  0), is(1));
        assertThat(compare( 1,  1,  1, -1), is(-1));
        assertThat(compare( 1,  1,  1,  1), is(0));

    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_empty() {
        Ipv6Resource.parseReverseDomain("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_null() {
        Ipv6Resource.parseReverseDomain(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_ip6arpa() {
        Ipv6Resource.parseReverseDomain("1.2.3.4");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_no_octets() {
        Ipv6Resource.parseReverseDomain(".ip6.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_more_than_four_octets() {
        Ipv6Resource.parseReverseDomain("8.7.6.5.4.3.2.1.in-addr.arpa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void reverse_invalid_nibbles_dash() {
        Ipv6Resource.parseReverseDomain("1-1.1.a.ip6.arpa");
    }
    @Test(expected = IllegalArgumentException.class)
    public void reverse_invalid_nibbles_non_hex() {
        Ipv6Resource.parseReverseDomain("g.ip6.arpa");
    }

    @Test
    public void reverse_simple() {
        assertThat(Ipv6Resource.parseReverseDomain("2.ip6.arpa").toString(), is("2000::/4"));
        assertThat(Ipv6Resource.parseReverseDomain("8.3.7.0.1.0.0.2.ip6.arpa").toString(), is("2001:738::/32"));
        assertThat(Ipv6Resource.parseReverseDomain("a.7.9.b.1.1.0.2.8.b.7.0.1.0.a.2.ip6.arpa").toString(), is("2a01:7b8:2011:b97a::/64"));
        assertThat(Ipv6Resource.parseReverseDomain("b.a.9.8.7.6.5.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa").toString(), is("2001:db8::567:89ab/128"));
        assertThat(Ipv6Resource.parseReverseDomain("B.A.9.8.7.6.5.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.B.D.0.1.0.0.2.ip6.arpa").toString(), is("2001:db8::567:89ab/128"));
        assertThat(Ipv6Resource.parseReverseDomain("a.9.8.7.6.5.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa").toString(), is("2001:db8::567:89a0/124"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_prefix_length() {
        Ipv6Resource.parse("2001::/129");
    }

    @Test
    public void begin() {
        final Ipv6Resource subject = Ipv6Resource.parse("2001:2002:2003::/48");
        assertThat(subject.beginAsInetAddress().getHostAddress(), is("2001:2002:2003:0:0:0:0:0"));
    }

    @Test
    public void end() {
        final Ipv6Resource subject = Ipv6Resource.parse("2001:2002:2003::/48");
        assertThat(subject.endAsInetAddress().getHostAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff"));
    }
}
