package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class IndexWithReferenceTest extends IndexTestBase {

    @Test
    public void findInIndex_found() throws Exception {
        IndexWithReference subject = new IndexWithReference(AttributeType.MNT_REF, "mnt_ref", "mnt_id");
        final RpslObject maintainer = RpslObject.parse("mntner:MNT-TEST\nmnt-by:MNT-TEST");
        final RpslObjectUpdateInfo objectInfo = rpslObjectUpdateDao.createObject(maintainer);
        whoisTemplate.update(String.format("INSERT INTO mnt_ref(object_id, mnt_id, object_type) VALUES(%s, %s, %s)", 1, objectInfo.getObjectId(), 18));

        assertThat(subject.findInIndex(whoisTemplate, objectInfo.getKey()).size(), is(1));
        assertThat(subject.findInIndex(whoisTemplate, objectInfo).size(), is(1));
        assertThat(subject.findInIndex(whoisTemplate, objectInfo, ObjectType.ORGANISATION).size(), is(1));
    }

    @Test
    public void findInIndex_not_found() throws Exception {
        IndexWithReference subject = new IndexWithReference(AttributeType.MNT_REF, "mnt_ref", "mnt_id");
        final RpslObject maintainer = RpslObject.parse("mntner:MNT-TEST\nmnt-by:MNT-TEST");
        final RpslObjectUpdateInfo objectInfo = rpslObjectUpdateDao.createObject(maintainer);

        assertThat(subject.findInIndex(whoisTemplate, objectInfo.getKey()).size(), is(0));
        assertThat(subject.findInIndex(whoisTemplate, objectInfo).size(), is(0));
        assertThat(subject.findInIndex(whoisTemplate, objectInfo, ObjectType.ORGANISATION).size(), is(0));
    }

    @Test
    public void addToIndex() throws Exception {
        IndexWithReference subject = new IndexWithReference(AttributeType.MNT_LOWER, "mnt_lower", "mnt_id");
        final RpslObject rpslObject = RpslObject.parse("mntner: RIPE-MNT\nmnt-by:RIPE-MNT");
        rpslObjectUpdateDao.createObject(rpslObject);
        final RpslObjectInfo maintainer = new RpslObjectInfo(1, ObjectType.MNTNER, "MNTNER");

        final int added = subject.addToIndex(whoisTemplate, maintainer, null, "RIPE-MNT");
        assertThat(added, is(1));
    }
}
