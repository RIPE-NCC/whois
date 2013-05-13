package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithLocalAsTest extends IndexTestBase {

    private IndexWithLocalAs subject;

    @Before
    public void setup() {
        subject = new IndexWithLocalAs(AttributeType.LOCAL_AS);
    }

    @Test
    public void add_to_index() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nlocal-as: AS1234");
        final int objectId = addObject(rpslObject);
        RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(objectId, ObjectType.INET_RTR, rpslObject.getKey());
        checkIndex(objectId, "");

        final int rows = subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, "AS1234");

        assertThat(rows, is(1));
        checkIndex(objectId, "AS1234");
    }

    @Test
    public void find_in_index() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nlocal-as: AS1234");
        final int objectId = addObject(rpslObject);
        addLocalAsToIndex(rpslObject, "AS1234");
        RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(objectId, ObjectType.INET_RTR, rpslObject.getKey());

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "invalid");

        checkIndex(objectId, "AS1234");
    }

    @Test
    public void not_found_in_index() throws Exception {
        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "invalid");

        assertThat(results.size(), is(0));
    }

    private int addObject(final RpslObject rpslObject) {
        databaseHelper.addObject("inet-rtr: " + rpslObject.findAttribute(AttributeType.INET_RTR).getValue());
        return getObjectId(rpslObject);
    }

    private int addLocalAsToIndex(final RpslObject rpslObject, final String localAs) {
        return whoisTemplate.update("UPDATE inet_rtr SET local_as = ? WHERE object_id = ?", localAs, getObjectId(rpslObject));
    }

    private void checkIndex(final int objectId, final String localAs) {
        assertThat(whoisTemplate.queryForObject("SELECT local_as FROM inet_rtr WHERE object_id = ?", String.class, objectId), is(localAs));
    }
}
