package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class IndexWithNServerTest extends IndexTestBase {
    private IndexStrategy subject;

    @Test
    public void not_found_in_index() throws Exception {
        subject = IndexStrategies.get(AttributeType.NSERVER);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "host");

        assertThat(results.size(), is(0));
    }

    @Test
    public void found_in_index() throws Exception {
        final RpslObject domain = RpslObject.parse("domain:142.37.in-addr.arpa\nnserver:ns.ripe.net");
        rpslObjectUpdateDao.createObject(domain);

        subject = IndexStrategies.get(AttributeType.NSERVER);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "ns.ripe.net");
        assertThat(results.size(), is(1));
    }

    @Test
    public void found_in_index_with_dot_in_object() throws Exception {
        final RpslObject domain = RpslObject.parse("domain:142.37.in-addr.arpa\nnserver:ns.ripe.net.");
        rpslObjectUpdateDao.createObject(domain);

        subject = IndexStrategies.get(AttributeType.NSERVER);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "ns.ripe.net");
        assertThat(results.size(), is(1));
    }

    @Test
    public void found_in_index_with_dot_in_query() throws Exception {
        final RpslObject domain = RpslObject.parse("domain:142.37.in-addr.arpa\nnserver:ns.ripe.net");
        rpslObjectUpdateDao.createObject(domain);

        subject = IndexStrategies.get(AttributeType.NSERVER);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "ns.ripe.net.");
        assertThat(results.size(), is(1));
    }
}
