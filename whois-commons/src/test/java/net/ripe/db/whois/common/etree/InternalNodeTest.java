package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InternalNodeTest {

    private InternalNode<Ipv4Resource, String> a = new InternalNode<>(new Ipv4Resource(1, 2), "1-2");
    private InternalNode<Ipv4Resource, String> b = new InternalNode<>(new Ipv4Resource(1, 2), "1-2");
    private InternalNode<Ipv4Resource, String> c = new InternalNode<>(new Ipv4Resource(1, 4), "1-4");
    private InternalNode<Ipv4Resource, String> d = new InternalNode<>(new Ipv4Resource(1, 4), "1-4");
    private InternalNode<Ipv4Resource, String> e = new InternalNode<>(new Ipv4Resource(2, 5), "2-5");

    @BeforeEach
    public void setup() {
        d.addChild(a);
    }

    @Test
    public void test_equals_and_hashcode() {
        assertThat(a, not(equalTo(null)));
        assertThat(a, not(equalTo(new Object())));
        assertThat(a, equalTo(a));
        assertThat(a, equalTo(b));
        assertThat(c, equalTo(c));
        assertThat(a, not(equalTo(c)));
        assertThat(c, not(equals(a)));
        assertThat(c, not(equalTo(d)));

        assertThat(a.hashCode(), equalTo(a.hashCode()));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
        assertThat(a.hashCode(), is(not(c.hashCode())));
        assertThat(c.hashCode(), is(not(d.hashCode())));
    }

    @Test
    public void test_intersect_insert_fails() {
        assertThrows(IllegalArgumentException.class, () -> {
            c.addChild(e);
        });
    }

    @Test
    public void test_intersect_remove_fails() {
        assertThrows(IllegalArgumentException.class, () -> {
            c.removeChild(e.getInterval());
        });
    }

}
