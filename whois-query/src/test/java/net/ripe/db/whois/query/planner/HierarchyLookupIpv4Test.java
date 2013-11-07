package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HierarchyLookupIpv4Test {
    @Mock Ipv4Tree ipv4Tree;
    @Mock RpslObjectDao rpslObjectDao;
    @InjectMocks HierarchyLookupIpv4 subject;

    @Test
    public void getSupportedType() {
        assertThat(subject.getSupportedType(), is(ObjectType.INETNUM));
    }

    @Test
    public void createResource() {
        final String resource = "0.0.0.0";
        assertThat(subject.createResource(resource), is(Ipv4Resource.parse(resource)));
    }
}
