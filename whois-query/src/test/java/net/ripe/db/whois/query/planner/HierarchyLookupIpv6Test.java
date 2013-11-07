package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HierarchyLookupIpv6Test {
    @Mock Ipv6Tree ipv6Tree;
    @Mock RpslObjectDao rpslObjectDao;
    @InjectMocks HierarchyLookupIpv6 subject;

    @Test
    public void getSupportedType() {
        assertThat(subject.getSupportedType(), is(ObjectType.INET6NUM));
    }

    @Test
    public void createResource() {
        final String resource = "::0";
        assertThat(subject.createResource(resource), is(Ipv6Resource.parse(resource)));
    }
}
