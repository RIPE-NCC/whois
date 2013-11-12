package net.ripe.db.whois.common.domain;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntervalTreeKeyTest {

    private Ipv4Entry subject = new Ipv4Entry(Ipv4Resource.parse("127.0.0.0/8"), 1);

    @Test
    public void should_contain_inetnum_and_object_id() {
        assertEquals("key", Ipv4Resource.parse("127.0.0.0/8"), subject.getKey());
        assertEquals("objectId", 1, subject.getObjectId());
    }

    @Test
    public void test_equals_hashCode() {
        Ipv4Entry a = new Ipv4Entry(Ipv4Resource.parse("127.0.0.0/8"), 1);
        Ipv4Entry b = new Ipv4Entry(Ipv4Resource.parse("127.0.0.0/8"), 1);
        Ipv4Entry c = new Ipv4Entry(Ipv4Resource.parse("10.0.0.0/8"), 1);
        Ipv4Entry d = new Ipv4Entry(Ipv4Resource.parse("10.0.0.0/8"), 7);

        assertEquals("same", a, a);
        assertEquals("equal", a, b);
        assertFalse("null", a.equals(null));
        assertFalse("different class", a.equals(new Object()));
        assertFalse("different key", a.equals(c));
        assertTrue("same key", c.equals(d));

        assertTrue("same", a.hashCode() == b.hashCode());
        assertTrue("same", c.hashCode() == d.hashCode());
        assertFalse("different hashcode", a.hashCode() == c.hashCode());
    }
}
