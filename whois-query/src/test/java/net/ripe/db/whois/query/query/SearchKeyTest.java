package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.domain.ip.IpInterval;
import net.ripe.db.whois.common.domain.attrs.AsBlockRange;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

public class SearchKeyTest {
    private SearchKey subject;

    @Test
    public void parse_and_value() {
        subject = new SearchKey("test");

        assertThat(subject.getValue(), is("test"));
    }

    @Test
    public void contains_ipv4() {
        subject = new SearchKey("128/8");

        final IpInterval<?> key = subject.getIpKeyOrNull();

        assertThat(key.toString(), is("128.0.0.0/8"));
        assertTrue(key == subject.getIpKeyOrNull()); // cache hit

        assertThat(subject.getIpKeyOrNullReverse(), is(nullValue()));
        assertThat(subject.getAsBlockRangeOrNull(), is(nullValue()));
    }

    @Test
    public void contains_ipv6() {
        subject = new SearchKey("::0/8");

        final IpInterval<?> key = subject.getIpKeyOrNull();

        assertThat(key.toString(), is("::/8"));
        assertTrue(key == subject.getIpKeyOrNull()); // cache hit

        assertThat(subject.getIpKeyOrNullReverse(), is(nullValue()));
        assertThat(subject.getAsBlockRangeOrNull(), is(nullValue()));
    }

    @Test
    public void contains_ipv4_reverse() {
        subject = new SearchKey("128.in-addr.arpa");

        final IpInterval<?> key = subject.getIpKeyOrNullReverse();

        assertThat(key.toString(), is("128.0.0.0/8"));
        assertTrue(key == subject.getIpKeyOrNullReverse()); // cache hit

        assertThat(subject.getIpKeyOrNull(), is(nullValue()));
        assertThat(subject.getAsBlockRangeOrNull(), is(nullValue()));
    }

    @Test
    public void contains_ipv6_reverse() {
        subject = new SearchKey("2.ip6.arpa");

        final IpInterval<?> key = subject.getIpKeyOrNullReverse();

        assertThat(key.toString(), is("2000::/4"));
        assertTrue(key == subject.getIpKeyOrNullReverse()); // cache hit

        assertThat(subject.getIpKeyOrNull(), is(nullValue()));
        assertThat(subject.getAsBlockRangeOrNull(), is(nullValue()));
    }

    @Test
    public void contains_asnum() {
        subject = new SearchKey("AS123");

        final AsBlockRange key = subject.getAsBlockRangeOrNull();

        assertThat(key.getBegin(), is(123L));
        assertThat(key.getEnd(), is(123L));

        assertThat(subject.getIpKeyOrNull(), is(nullValue()));
        assertThat(subject.getIpKeyOrNullReverse(), is(nullValue()));
    }

    @Test
    public void comma_in_search_key() {
        subject = new SearchKey("10,10");
        assertNull(subject.getIpKeyOrNull());
    }

    @Test
    public void parse_ipv6_with_v4() {
        final SearchKey searchKey = new SearchKey("::ffff:0.0.0.0");
        final IpInterval<?> ipKeyOrNull = searchKey.getIpKeyOrNull();

        assertNull(ipKeyOrNull);
    }

    @Test
    public void parse_ipv6_with_v4_prefix() {
        final SearchKey searchKey = new SearchKey("::ffff:0.0.0.0/72");
        final IpInterval<?> ipKeyOrNull = searchKey.getIpKeyOrNull();

        assertNull(ipKeyOrNull);
    }
}
