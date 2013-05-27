package net.ripe.db.whois.common.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataValidatorTest {
    List<String> sources;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    AuthoritativeResourceDataValidator subject;

    @Before
    public void setUp() throws Exception {
        sources = Lists.newArrayList("GRS1", "GRS2", "GRS3");
        subject = new AuthoritativeResourceDataValidator(sources, authoritativeResourceData);

        prepareAuthoritativeResourceData(
                "GRS1",
                Lists.newArrayList("AS1", "AS2", "as3"),
                Lists.newArrayList("0/0"),
                Lists.newArrayList("::0/0"));

        prepareAuthoritativeResourceData(
                "GRS2",
                Lists.newArrayList("as1", "AS10", "AS11"),
                Lists.newArrayList("0/0"),
                Lists.newArrayList("::0/0"));

        prepareAuthoritativeResourceData(
                "GRS3",
                Lists.newArrayList("AS1", "AS10", "AS12"),
                Lists.newArrayList("0/0"),
                Lists.newArrayList("::0/0"));
    }

    @Test
    public void checkOverlaps() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.checkOverlaps(writer);

        // TODO [AK] Finish assertions

        System.out.println(writer.getBuffer().toString());
    }

    private void prepareAuthoritativeResourceData(final String name, final List<String> autNums, final List<String> ipv4Resources, final List<String> ipv6Resources) {
        final AuthoritativeResource authoritativeResource = mock(AuthoritativeResource.class);
        when(authoritativeResource.getAutNums()).thenReturn(ciSet(autNums));

        final IntervalMap<Ipv4Resource, Ipv4Resource> ipv4Map = new NestedIntervalMap<Ipv4Resource, Ipv4Resource>();
        for (final String ipv4Resource : ipv4Resources) {
            final Ipv4Resource resource = Ipv4Resource.parse(ipv4Resource);
            ipv4Map.put(resource, resource);
        }
        when(authoritativeResource.getInetRanges()).thenReturn(ipv4Map);

        final IntervalMap<Ipv6Resource, Ipv6Resource> ipv6Map = new NestedIntervalMap<Ipv6Resource, Ipv6Resource>();
        for (final String ipv6Resource : ipv6Resources) {
            final Ipv6Resource resource = Ipv6Resource.parse(ipv6Resource);
            ipv6Map.put(resource, resource);
        }
        when(authoritativeResource.getInet6Ranges()).thenReturn(ipv6Map);

        when(authoritativeResourceData.getAuthoritativeResource(ciString(name))).thenReturn(authoritativeResource);
    }

}
