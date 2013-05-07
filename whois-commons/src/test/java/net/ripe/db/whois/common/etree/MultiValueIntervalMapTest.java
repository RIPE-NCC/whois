package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.domain.Ipv4Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MultiValueIntervalMapTest {
    private MultiValueIntervalMap<Ipv4Resource, String> subject;
    private Ipv4Resource k_11;
    private Ipv4Resource k_12;
    private Ipv4Resource k_13;
    private Ipv4Resource k_14;

    private String v_11 = "1-1";
    private String v_121 = "1-2 1";
    private String v_122 = "1-2 2";
    private String v_131 = "1-3 1";
    private String v_132 = "1-3 2";
    private String v_133 = "1-3 3";

    @Before
    public void setUp() throws Exception {
        subject = new MultiValueIntervalMap<Ipv4Resource, String>();

        k_11 = new Ipv4Resource(1, 1);
        k_12 = new Ipv4Resource(1, 2);
        k_13 = new Ipv4Resource(1, 3);
        k_14 = new Ipv4Resource(1, 4);

        subject.put(k_11, v_11);

        subject.put(k_12, v_122);
        subject.put(k_12, v_121);

        subject.put(k_13, v_131);
        subject.put(k_13, v_133);
        subject.put(k_13, v_132);
    }

    @Test
    public void findExact_k11() {
        final List<String> result = subject.findExact(k_11);
        assertThat(result, contains(v_11));
    }

    @Test
    public void findExact_k13() {
        final List<String> result = subject.findExact(k_13);
        assertThat(result, contains(v_131, v_132, v_133));
    }

    @Test
    public void remove() {
        subject.remove(k_11);

        final List<String> result = subject.findExact(k_11);
        assertThat(result, hasSize(0));
    }

    @Test
    public void remove_with_value() {
        subject.remove(k_12, v_122);
        assertThat(subject.findExact(k_12), contains(v_121));

        subject.remove(k_12, v_121);
        assertThat(subject.findExact(k_12), hasSize(0));

    }

    @Test
    public void remove_with_value_key_unknown() {
        subject.remove(k_14, v_11);
        final List<String> result = subject.findAllMoreSpecific(Ipv4Resource.MAX_RANGE);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122, v_11));
    }

    @Test
    public void clear() {
        subject.clear();

        final List<String> result = subject.findAllMoreSpecific(Ipv4Resource.MAX_RANGE);
        assertThat(result, hasSize(0));
    }

    @Test
    public void findFirstLessSpecific() {
        final List<String> result = subject.findFirstLessSpecific(k_11);
        assertThat(result, contains(v_121, v_122));
    }

    @Test
    public void findExact() {
        final List<String> result = subject.findExact(k_12);
        assertThat(result, contains(v_121, v_122));
    }

    @Test
    public void findExactOrFirstLessSpecific() {
        final List<String> result = subject.findExactOrFirstLessSpecific(k_12);
        assertThat(result, contains(v_121, v_122));
    }

    @Test
    public void findAllLessSpecific() {
        final List<String> result = subject.findAllLessSpecific(k_11);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122));
    }

    @Test
    public void findExactAndAllLessSpecific() {
        final List<String> result = subject.findExactAndAllLessSpecific(k_12);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122));
    }

    @Test
    public void findFirstMoreSpecific() {
        final List<String> result = subject.findFirstMoreSpecific(k_12);
        assertThat(result, contains(v_11));
    }

    @Test
    public void findAllMoreSpecific() {
        final List<String> result = subject.findAllMoreSpecific(Ipv4Resource.MAX_RANGE);
        assertThat(result, contains(v_131, v_132, v_133, v_121, v_122, v_11));
    }

    @Test
    public void findExactAndAllMoreSpecific() {
        final List<String> result = subject.findExactAndAllMoreSpecific(k_12);
        assertThat(result, contains(v_121, v_122, v_11));
    }
}
