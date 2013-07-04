package net.ripe.db.whois.query.executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.iptree.*;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.dao.Inet6numDao;
import net.ripe.db.whois.query.dao.InetnumDao;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.support.Fixture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RpslObjectSearcherTest {
    AtomicInteger rpslObjectId;
    Map<RpslObject, RpslObjectInfo> map;

    @Mock RpslObjectDao rpslObjectDao;
    @Mock InetnumDao inetnumDao;
    @Mock Inet6numDao inet6numDao;
    @Mock Ipv4Tree ipv4Tree;
    @Mock Ipv6Tree ipv6Tree;
    @Mock Ipv4RouteTree route4Tree;
    @Mock Ipv6RouteTree route6Tree;
    @Mock Ipv4DomainTree ipv4DomainTree;
    @Mock Ipv6DomainTree ipv6DomainTree;
    @InjectMocks RpslObjectSearcher subject;

    @Before
    public void setUp() throws Exception {
        rpslObjectId = new AtomicInteger(1);
        map = Maps.newHashMap();

        Fixture.mockRpslObjectDaoLoadingBehavior(rpslObjectDao);
    }

    @Test
    public void forward_lookup_invalid_attribute_syntax() {
        assertQueryResult("-T inetnum help");
    }

    @Test
    public void forward_lookup_by_key() {
        final RpslObject asSet = RpslObject.parse("as-set: AS-RIPENCC");
        mockRpslObjects(asSet);
        when(rpslObjectDao.findByKey(ObjectType.AS_SET, "AS-RIPENCC")).thenReturn(infoFor(asSet));

        assertQueryResult("-r -T as-set AS-RIPENCC", asSet);
    }

    @Test
    public void forward_lookup_by_key_no_results() {
        when(rpslObjectDao.findByKey(ObjectType.AS_SET, "AS-RIPENCC")).thenThrow(EmptyResultDataAccessException.class);

        assertQueryResult("-r -T as-set AS-RIPENCC");
    }

    @Test
    public void forward_lookup_by_attribute() {
        final RpslObject irt = RpslObject.parse("irt: DEV-IRT\ne-mail: person@domain.com");
        mockRpslObjects(irt);
        when(rpslObjectDao.findByAttribute(AttributeType.E_MAIL, "person@domain.com")).thenReturn(infosFor(irt));

        assertQueryResult("-T irt person@domain.com", irt);
    }

    @Test
    public void ipv4_domain() {
        final RpslObject domain = RpslObject.parse("domain: 1.168.192.in-addr.arpa");
        final Ipv4Resource ipResource = Ipv4Resource.parse("192.168.1/24");

        mockRpslObjects(domain);
        when(ipv4DomainTree.findExactOrFirstLessSpecific(ipResource)).thenReturn(Lists.newArrayList(new Ipv4Entry(ipResource, infoFor(domain).getObjectId())));

        assertQueryResult("-rT domain 1.168.192.in-addr.arpa", domain);
    }

    @Test
    public void ipv4_domain_both_directions() {
        final RpslObject domain = RpslObject.parse("domain: 1.168.192.in-addr.arpa");
        final Ipv4Resource ipResource = Ipv4Resource.parse("192.168.1.1");

        mockRpslObjects(domain);
        when(ipv4DomainTree.findExactOrFirstLessSpecific(ipResource)).thenReturn(Lists.newArrayList(new Ipv4Entry(ipResource, infoFor(domain).getObjectId())));

        assertQueryResult("-d 192.168.1.1", domain);
    }

    @Test
    public void ipv4_domain_both_directions_inverse() {
        final RpslObject domain = RpslObject.parse("domain: 1.168.192.in-addr.arpa");
        final Ipv4Resource ipResource = Ipv4Resource.parse("192.168.1/24");

        mockRpslObjects(domain);
        when(ipv4DomainTree.findExactOrFirstLessSpecific(ipResource)).thenReturn(Lists.newArrayList(new Ipv4Entry(ipResource, infoFor(domain).getObjectId())));

        assertQueryResult("-d 1.168.192.in-addr.arpa", domain);
    }

    @Test
    public void ipv6_domain() {
        final RpslObject domain = RpslObject.parse("domain: 0.c.e.a.0.0.a.2.ip6.arpa");
        final Ipv6Resource ipResource = Ipv6Resource.parse("2a00:aec0::/32");

        mockRpslObjects(domain);
        when(ipv6DomainTree.findExactOrFirstLessSpecific(ipResource)).thenReturn(Lists.newArrayList(new Ipv6Entry(ipResource, infoFor(domain).getObjectId())));

        assertQueryResult("-rT domain 0.c.e.a.0.0.a.2.ip6.arpa", domain);
    }


    @Test
    public void inverse_lookup_never_returns_null() {
        for (final AttributeType attributeType : AttributeType.values()) {
            assertNotNull(subject.search(Query.parse("-i " + attributeType.getName() + " query")));
        }
    }

    @Test
    public void inverse_lookup_unsupported_attribute() {
        final Iterator<? extends ResponseObject> responseIterator = subject.search(Query.parse("-i e-mail,phone something")).iterator();

        assertThat(responseIterator.next().toString(), is(QueryMessages.attributeNotSearchable("e-mail").toString()));
        assertThat(responseIterator.next().toString(), is(QueryMessages.attributeNotSearchable("phone").toString()));
        assertThat(responseIterator.hasNext(), is(false));
    }

    @Test
    public void inverse_lookup_all_types() {
        final RpslObject mntner = RpslObject.parse("mntner:aardvark");
        final RpslObject organisation = RpslObject.parse("organisation:aardvark");

        mockRpslObjects(mntner, organisation);

        when(rpslObjectDao.findByAttribute(AttributeType.MNT_BY, "aardvark")).thenReturn(infosFor(mntner));
        when(rpslObjectDao.findByAttribute(AttributeType.ORG, "aardvark")).thenReturn(infosFor(organisation));

        assertQueryResult("-r -i mnt-by,mnt-ref,org aardvark", mntner, organisation);
    }

    @Test
    public void inverse_lookup_single_type() {
        final RpslObject mntner = RpslObject.parse("mntner:aardvark");
        final RpslObject organisation = RpslObject.parse("organisation:aardvark");

        mockRpslObjects(mntner, organisation);

        when(rpslObjectDao.findByAttribute(AttributeType.MNT_BY, "aardvark")).thenReturn(infosFor(mntner));
        when(rpslObjectDao.findByAttribute(AttributeType.ORG, "aardvark")).thenReturn(infosFor(organisation));

        assertQueryResult("-r -T organisation -i mnt-by,mnt-ref,org aardvark", organisation);
    }

    private void mockRpslObjects(final RpslObject... rpslObjects) {
        for (final RpslObject rpslObject : rpslObjects) {
            final int id = rpslObjectId.getAndIncrement();
            final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(id, rpslObject.getType(), rpslObject.getKey());
            when(rpslObjectDao.getById(id)).thenReturn(new RpslObject(id, rpslObject.getAttributes()));
            map.put(rpslObject, rpslObjectInfo);
        }
    }

    private RpslObjectInfo infoFor(final RpslObject rpslObject) {
        return map.get(rpslObject);
    }

    private List<RpslObjectInfo> infosFor(final RpslObject... rpslObjects) {
        final List<RpslObjectInfo> result = Lists.newArrayListWithExpectedSize(rpslObjects.length);
        for (final RpslObject rpslObject : rpslObjects) {
            result.add(map.get(rpslObject));
        }

        return result;
    }

    private void assertQueryResult(final String query, final RpslObject... expectedResults) {
        final Set<RpslObject> rpslObjects = Sets.newLinkedHashSet();
        for (final ResponseObject responseObject : subject.search(Query.parse(query))) {
            if (responseObject instanceof RpslObject) {
                rpslObjects.add((RpslObject) responseObject);
            }
        }

        assertThat(rpslObjects, hasSize(expectedResults.length));
        if (expectedResults.length > 0) {
            assertThat(rpslObjects, contains(expectedResults));
        }
    }
}
