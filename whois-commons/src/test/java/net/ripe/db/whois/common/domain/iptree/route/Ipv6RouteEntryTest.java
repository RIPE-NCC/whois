package net.ripe.db.whois.common.domain.iptree.route;

import net.ripe.db.whois.common.iptree.Ipv6RouteEntry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Ipv6RouteEntryTest {
    @Test
    public void testParse_null() {
        assertThrows(NullPointerException.class, () -> {
            Ipv6RouteEntry.parse(null, 0);
        });
    }

    @Test
    public void testParse_empty() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ipv6RouteEntry.parse("", 0);
        });
    }

    @Test
    public void testParse_valid() {
        final Ipv6RouteEntry entry = Ipv6RouteEntry.parse("::0/128AS1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INET6NUM));
        assertThat(entry.getKey().toString(), is("::/128"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test
    public void testParse_valid_lowercase() {
        final Ipv6RouteEntry entry = Ipv6RouteEntry.parse("::0/128as1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INET6NUM));
        assertThat(entry.getKey().toString(), is("::/128"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test
    public void testParse_valid_mixedcase() {
        final Ipv6RouteEntry entry = Ipv6RouteEntry.parse("::0/128aS1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INET6NUM));
        assertThat(entry.getKey().toString(), is("::/128"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test
    public void testParse_no_origin() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ipv6RouteEntry.parse("::0/128", 11);
        });
    }

    @Test
    public void testParse_no_origin_number() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ipv6RouteEntry.parse("::0/128AS", 11);
        });
    }

    @Test
    public void testParse_no_prefix() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ipv6RouteEntry.parse("AS1234", 11);
        });

    }

    @Test
    public void testParse_with_spaces() {
        final Ipv6RouteEntry entry = Ipv6RouteEntry.parse("::0/128 AS1234", 11);

        assertThat(entry.getKey().getAttributeType(), is(AttributeType.INET6NUM));
        assertThat(entry.getKey().toString(), is("::/128"));
        assertThat(entry.getObjectId(), is(11));
        assertThat(entry.getOrigin(), is("AS1234"));
    }

    @Test
    public void testParse_v4() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ipv6RouteEntry.parse("10.0.0.0/32AS1234", 11);
        });
    }
}
