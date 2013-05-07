package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class IndexWithValueTest extends IndexTestBase {
    private IndexWithValue subject;

    @Test
    public void not_found_in_index() throws Exception {
        subject = new IndexWithValue(AttributeType.MNTNER, "mntner", "mntner");

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "mntner");

        assertThat(results.size(), is(0));
    }

    @Test
    public void found_in_index() throws Exception {
        final RpslObject person = RpslObject.parse("person: test person\nnic-hdl: TEST-NIC");
        rpslObjectUpdateDao.createObject(person);
        subject = new IndexWithValue(AttributeType.NIC_HDL, "person_role", "nic_hdl");

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "TEST-NIC");

        assertThat(results.size(), is(1));
    }

    @Test
    public void add_to_index_does_not_exist() {
        RpslObjectInfo role = new RpslObjectInfo(1, ObjectType.ROLE, "NIC-TEST");
        subject = new IndexWithValue(AttributeType.NIC_HDL, "person_role", "nic_hdl");

        final int added = subject.addToIndex(whoisTemplate, role, null, "NIC-TEST");
        assertThat(added, is(1));
    }

}
