package net.ripe.db.whois.common.domain;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class IntervalTreeKeyTest {

    private Ipv4Entry subject = new Ipv4Entry(Ipv4Resource.parse("127.0.0.0/8"), 1);

    @Test
    public void should_contain_inetnum_and_object_id() {
        assertEquals(Ipv4Resource.parse("127.0.0.0/8"), subject.getKey(), "key");
        assertEquals(1, subject.getObjectId(), "objectId");
    }

    @Test
    public void test_equals_hashCode() {
        Ipv4Entry a = new Ipv4Entry(Ipv4Resource.parse("127.0.0.0/8"), 1);
        Ipv4Entry b = new Ipv4Entry(Ipv4Resource.parse("127.0.0.0/8"), 1);
        Ipv4Entry c = new Ipv4Entry(Ipv4Resource.parse("10.0.0.0/8"), 1);
        Ipv4Entry d = new Ipv4Entry(Ipv4Resource.parse("10.0.0.0/8"), 7);

        assertEquals(a, a, "same");
        assertEquals(a, b, "equal");
        assertFalse(a.equals(null), "null");
        assertFalse(a.equals(new Object()), "different class");
        assertFalse(a.equals(c), "different key");
        assertThat(c, is(d));       // same key

        assertThat( a.hashCode(), is(b.hashCode()));    // same
        assertThat(c.hashCode(), is(d.hashCode()));     // same
        assertFalse(a.hashCode() == c.hashCode(), "different hashcode");
    }
}
