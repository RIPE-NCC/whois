package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class InternalNodeTest {

    private InternalNode<Ipv4Resource, String> a = new InternalNode<>(new Ipv4Resource(1, 2), "1-2");
    private InternalNode<Ipv4Resource, String> b = new InternalNode<>(new Ipv4Resource(1, 2), "1-2");
    private InternalNode<Ipv4Resource, String> c = new InternalNode<>(new Ipv4Resource(1, 4), "1-4");
    private InternalNode<Ipv4Resource, String> d = new InternalNode<>(new Ipv4Resource(1, 4), "1-4");
    private InternalNode<Ipv4Resource, String> e = new InternalNode<>(new Ipv4Resource(2, 5), "2-5");

    @Before
    public void setup() {
        d.addChild(a);
    }

    @Test
    public void test_equals_and_hashcode() {
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(c, c);
        assertFalse(a.equals(c));
        assertFalse(c.equals(a));
        assertFalse(c.equals(d));

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.hashCode() == c.hashCode());
        assertFalse(c.hashCode() == d.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_intersect_insert_fails() {
        c.addChild(e);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_intersect_remove_fails() {
        c.removeChild(e.getInterval());
    }

}
