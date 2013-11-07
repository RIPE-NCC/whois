package net.ripe.db.whois.common.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.domain.ip.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataValidatorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceDataValidatorTest.class);

    @Mock AuthoritativeResourceData authoritativeResourceData;
    AuthoritativeResourceDataValidator subject;

    @Before
    public void setUp() throws Exception {
        subject = new AuthoritativeResourceDataValidator(new String[] {"GRS1","GRS2","GRS3"}, authoritativeResourceData);

        prepareAuthoritativeResourceData(
                "GRS1",
                Lists.newArrayList("AS1", "AS2", "as3"),
                Lists.newArrayList("192.0.0.0 - 192.0.0.2", "193.0.0.10 - 193.0.0.15", "10.0.0.0"),
                Lists.newArrayList("::0/0"));

        prepareAuthoritativeResourceData(
                "GRS2",
                Lists.newArrayList("as1", "AS10", "AS11"),
                Lists.newArrayList("192.0.0.1 - 192.0.0.4", "10.0.0.0", "11/8"),
                Lists.newArrayList("::0/0"));

        prepareAuthoritativeResourceData(
                "GRS3",
                Lists.newArrayList("AS1", "AS10", "AS12"),
                Lists.newArrayList("193.0.0.0 - 193.0.0.11", "10.0.0.1"),
                Lists.newArrayList("::0/0"));
    }

    @Test
    public void checkOverlaps() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.checkOverlaps(writer);

        final String output = writer.getBuffer().toString();
        LOGGER.info("overlaps:\n{}", output);

        final Matcher matcher = Pattern.compile("(.*?)\\t(.*?)\\t(.*?)\\t(.*?)\\t(.*)").matcher(output);
        final List<String> overlaps = Lists.newArrayList();
        while (matcher.find()) {
            overlaps.add(matcher.group(0));
        }

        assertThat(overlaps, hasSize(10));
        assertThat(overlaps, contains(
                "GRS1\tGRS2\taut-num     \tAS1                                     \tAS1",
                "GRS1\tGRS2\tinetnum     \t10.0.0.0/32                             \t10.0.0.0/32",
                "GRS1\tGRS2\tinetnum     \t192.0.0.0 - 192.0.0.2                   \t192.0.0.1/30",
                "GRS1\tGRS2\tinet6num    \t::/0                                    \t::/0",
                "GRS1\tGRS3\taut-num     \tAS1                                     \tAS1",
                "GRS1\tGRS3\tinetnum     \t193.0.0.10 - 193.0.0.15                 \t193.0.0.0 - 193.0.0.11",
                "GRS1\tGRS3\tinet6num    \t::/0                                    \t::/0",
                "GRS2\tGRS3\taut-num     \tas1                                     \tas1",
                "GRS2\tGRS3\taut-num     \tAS10                                    \tAS10",
                "GRS2\tGRS3\tinet6num    \t::/0                                    \t::/0"
        ));
    }


    private void prepareAuthoritativeResourceData(final String name, final List<String> autNums, final List<String> ipv4Resources, final List<String> ipv6Resources) {
        final AuthoritativeResource authoritativeResource = mock(AuthoritativeResource.class);
        when(authoritativeResource.getAutNums()).thenReturn(ciSet(autNums));

        final IntervalMap<Ipv4Resource, Ipv4Resource> ipv4Map = new NestedIntervalMap<>();
        for (final String ipv4Resource : ipv4Resources) {
            final Ipv4Resource resource = Ipv4Resource.parse(ipv4Resource);
            ipv4Map.put(resource, resource);
        }
        when(authoritativeResource.getInetRanges()).thenReturn(ipv4Map);

        final IntervalMap<Ipv6Resource, Ipv6Resource> ipv6Map = new NestedIntervalMap<>();
        for (final String ipv6Resource : ipv6Resources) {
            final Ipv6Resource resource = Ipv6Resource.parse(ipv6Resource);
            ipv6Map.put(resource, resource);
        }
        when(authoritativeResource.getInet6Ranges()).thenReturn(ipv6Map);

        when(authoritativeResourceData.getAuthoritativeResource(ciString(name))).thenReturn(authoritativeResource);
    }
}
