package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.ObjectType.AS_BLOCK;
import static net.ripe.db.whois.common.rpsl.ObjectType.AS_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.FILTER_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET_RTR;
import static net.ripe.db.whois.common.rpsl.ObjectType.IRT;
import static net.ripe.db.whois.common.rpsl.ObjectType.KEY_CERT;
import static net.ripe.db.whois.common.rpsl.ObjectType.MNTNER;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.PEERING_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.common.rpsl.ObjectType.POEM;
import static net.ripe.db.whois.common.rpsl.ObjectType.POETIC_FORM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.RTR_SET;
import static net.ripe.db.whois.common.rpsl.ObjectType.values;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RpslObjectInfoTest {
    private RpslObjectInfo subject;

    @Before
    public void setUp() throws Exception {
        subject = new RpslObjectInfo(1, MNTNER, "1");
    }

    @Test
    public void testEquals_null() {
        assertThat(new RpslObjectInfo(1, MNTNER, "1").equals(null), is(false));
    }

    @Test
    public void testEquals_other_type() {
        assertThat(new RpslObjectInfo(1, MNTNER, "1").equals(""), is(false));
    }

    @Test
    public void testEquals_instance() {
        assertThat(subject.equals(subject), is(true));
    }

    @Test
    public void testEquals() {
        assertThat(subject.equals(new RpslObjectInfo(1, MNTNER, "1")), is(true));
    }

    @Test
    public void testEquals_only_objectId() {
        assertThat(subject.equals(new RpslObjectInfo(1, DOMAIN, (String) null)), is(true));
    }

    @Test
    public void testHashCode() {
        assertThat(subject.hashCode(), is(1));
    }

    @Test
    public void testGetKey() {
        assertThat(subject.getKey(), is("1"));
    }

    @Test
    public void testCompareTo_same_object_type_less() {
        assertThat(subject.compareTo(new RpslObjectInfo(2, MNTNER, "2")), Matchers.lessThan(0));
    }

    @Test
    public void testCompareTo_same_object_type_greater() {
        assertThat(subject.compareTo(new RpslObjectInfo(2, MNTNER, "0")), Matchers.greaterThan(0));
    }

    @Test
    public void testCompareTo_same_object() {
        assertThat(subject.compareTo(subject), is(0));
    }

    @Test
    public void testCompareTo_different_objectType() {
        final List<RpslObjectInfo> infoList = new ArrayList<>();
        for (final ObjectType objectType : values()) {
            infoList.add(new RpslObjectInfo(1, objectType, ""));
        }

        Collections.sort(infoList);

        final List<ObjectType> objectTypesFromResult = Lists.newArrayList();
        for (final RpslObjectInfo info : infoList) {
            objectTypesFromResult.add(info.getObjectType());
        }


        assertThat(objectTypesFromResult, contains(INETNUM, INET6NUM, AS_BLOCK, AUT_NUM, AS_SET, ROUTE, ROUTE6, ROUTE_SET, INET_RTR, FILTER_SET, PEERING_SET, RTR_SET, DOMAIN, POETIC_FORM, POEM, MNTNER, IRT, KEY_CERT, ORGANISATION, ROLE, PERSON));
    }
}
