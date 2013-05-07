package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithMemberOfTest extends IndexTestBase {
    IndexStrategy subject;

    RpslObject asSet;

    @Before
    public void setUp() throws Exception {
        subject = IndexStrategies.get(AttributeType.MEMBER_OF);

        asSet = RpslObject.parse("" +
                "as-set:          AS-BOGUS\n" +
                "source:          RIPE");

        databaseHelper.addObject(asSet);
    }

    @Test
    public void add_autnum() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num:         AS5404\n" +
                "member-of:       AS-BOGUS");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(2, rpslObject.getType(), rpslObject.getKey());
        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, rpslObject.getValueForAttribute(AttributeType.MEMBER_OF));

        assertThat(getNrMemberOf(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_route() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "route:     193.0.0.0/8\n" +
                "origin:    AS100\n" +
                "member-of: AS-BOGUS");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(2, rpslObject.getType(), rpslObject.getKey());
        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, rpslObject.getValueForAttribute(AttributeType.MEMBER_OF));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_autnum_invalid_reference() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num:         AS5404\n" +
                "member-of:       AS-UNKNOWN");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(2, rpslObject.getType(), rpslObject.getKey());
        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, rpslObject.getValueForAttribute(AttributeType.MEMBER_OF));
    }

    @Test
    public void remove_autnum() {
        final RpslObject autnum = databaseHelper.addObject("" +
                "aut-num:         AS5404\n" +
                "member-of:       AS-BOGUS");

        assertThat(getNrMemberOf(), is(1));

        subject.removeFromIndex(whoisTemplate, new RpslObjectInfo(autnum.getObjectId(), autnum.getType(), autnum.getValueForAttribute(AttributeType.MEMBER_OF)));

        assertThat(getNrMemberOf(), is(0));
    }

    @Test
    public void findInIndex_no_mbrsByRef() {
        databaseHelper.addObject("" +
                "aut-num:         AS5404\n" +
                "member-of:       AS-BOGUS");

        final List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "AS-BOGUS");
        assertThat(result, hasSize(0));
    }

    @Test
    public void findInIndex_mbrsByRef_any() {
        databaseHelper.addObject("" +
                "as-set:          AS-TEST\n" +
                "mbrs-by-ref:     ANY\n" +
                "source:          RIPE");

        databaseHelper.addObject("" +
                "aut-num:         AS5404\n" +
                "member-of:       AS-TEST");

        final List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "AS-TEST");
        assertThat(result, hasSize(1));
    }

    @Test
    public void findInIndex_mbrsByRef_mntner() {
        databaseHelper.addObject("mntner: DEV-MNT");

        databaseHelper.addObject("" +
                "as-set:          AS-TEST\n" +
                "mbrs-by-ref:     DEV-MNT\n" +
                "source:          RIPE");

        databaseHelper.addObject("" +
                "aut-num:         AS5404\n" +
                "member-of:       AS-TEST\n" +
                "mnt-by:          DEV-MNT");

        final List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "AS-TEST");
        assertThat(result, hasSize(1));
    }

    @Test
    public void findInIndex_mbrsByRef_mntner_not_maintained() {
        databaseHelper.addObject("mntner: DEV-MNT");

        databaseHelper.addObject("" +
                "as-set:          AS-TEST\n" +
                "mbrs-by-ref:     DEV-MNT\n" +
                "source:          RIPE");

        databaseHelper.addObject("" +
                "aut-num:         AS5404\n" +
                "member-of:       AS-TEST");

        final List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "AS-TEST");
        assertThat(result, hasSize(0));
    }

    @Test
    public void findInIndex_route_set() {
        databaseHelper.addObject("mntner:test-mnt");
        databaseHelper.addObject("route-set: rs-ripe\nmbrs-by-ref: test-mnt");
        databaseHelper.addObject("route: 195.10.40.0/29\nmember-of: rs-ripe\nmnt-by: test-mnt\norigin:AS3255");

        final List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "rs-ripe");
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getKey(), is("195.10.40.0/29AS3255"));
    }

    private int getNrMemberOf() {
        return whoisTemplate.queryForInt("select count(*) from member_of");
    }
}
