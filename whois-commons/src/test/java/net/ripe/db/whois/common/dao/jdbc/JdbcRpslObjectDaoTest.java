package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class JdbcRpslObjectDaoTest extends AbstractDaoTest {
    @Autowired RpslObjectDao subject;

    @Before
    public void setup() {
        sourceContext.setCurrent(Source.slave(source));
    }

    @After
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    @Test
    public void findSingleAsBlockUsingSingleBlockQuery() throws Exception {
        databaseHelper.addObject("as-block:AS31066-AS31066");

        RpslObject result = subject.findAsBlock(31066, 31066);

        assertThat(result.getType(), is(ObjectType.AS_BLOCK));
        assertThat(result.getKey().toString(), is("AS31066-AS31066"));
    }

    @Test
    public void findSingleAsBlockUsingExactRangeQuery() throws Exception {
        databaseHelper.addObject("as-block:AS31066-AS31066");

        RpslObject result = subject.findAsBlock(31066, 31066);

        assertThat(result.getType(), is(ObjectType.AS_BLOCK));
        assertThat(result.getKey().toString(), is("AS31066-AS31066"));
    }

    @Test
    public void findAsBlockUsingExactRangeQuery() throws Exception {
        databaseHelper.addObject("as-block:AS31066 - AS31244");

        RpslObject result = subject.findAsBlock(31066, 31244);

        assertThat(result.getType(), is(ObjectType.AS_BLOCK));
        assertThat(result.getKey().toString(), is("AS31066 - AS31244"));
    }

    @Test
    public void findAsBlockUsingContainedSingleBlockQuery() {
        databaseHelper.addObject("as-block:AS31066 - AS31244");

        RpslObject result = subject.findAsBlock(31200, 31200);

        assertThat(result.getType(), is(ObjectType.AS_BLOCK));
        assertThat(result.getKey().toString(), is("AS31066 - AS31244"));
    }

    @Test
    public void findAsBlockContainedRangeQuery() {
        databaseHelper.addObject("as-block:AS31066 - AS31244");

        RpslObject result = subject.findAsBlock(31100, 31200);

        assertThat(result.getType(), is(ObjectType.AS_BLOCK));
        assertThat(result.getKey().toString(), is("AS31066 - AS31244"));
    }

    @Test
    public void outOfRangeUsingRangeQuery() {
        databaseHelper.addObject("as-block:AS31066 - AS31244");

        assertNull(subject.findAsBlock(31066, 31299));
    }

    @Test
    public void nonexistentUsingSingleAsBlockQuery() {
        assertNull(subject.findAsBlock(1, 1));
    }

    @Test
    public void nonexistentUsingAsBlockQuery() {
        assertNull(subject.findAsBlock(0, 1));
    }

    /*
     * IRT
     */

    @Test
    public void successfulIrtQuery() {
        databaseHelper.addObject("irt:DEV-IRT");

        RpslObject result = subject.getByKey(ObjectType.IRT, "DEV-IRT");

        assertThat(result.getType(), is(ObjectType.IRT));
        assertThat(result.getKey().toString(), is("DEV-IRT"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentIrtLookup() {
        subject.getByKey(ObjectType.IRT, "nonexistent");
    }

    /*
     * Maintainer
     */

    @Test
    public void successfulMaintainerQuery() {
        databaseHelper.addObject("mntner:DEV-MNT");

        RpslObject result = subject.getByKey(ObjectType.MNTNER, "DEV-MNT");
        assertThat(result.getType(), is(ObjectType.MNTNER));
        assertThat(result.getKey().toString(), is("DEV-MNT"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentMaintainerLookup() {
        subject.getByKey(ObjectType.MNTNER, "nonexistent");
    }

    /*
     * Poetic Form
     */

    @Test
    public void successfulPoeticFormQuery() {
        databaseHelper.addObject("poetic-form:FORM-SONNET-INDONESIAN");

        RpslObject result = subject.getByKey(ObjectType.POETIC_FORM, "FORM-SONNET-INDONESIAN");
        assertThat(result.getType(), is(ObjectType.POETIC_FORM));
        assertThat(result.getKey().toString(), is("FORM-SONNET-INDONESIAN"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentPoeticFormLookup() {
        subject.getByKey(ObjectType.POETIC_FORM, "nonexistent");
    }

    /*
     * Poem
     */

    @Test
    public void successfulPoemQuery() {
        databaseHelper.addObject("poem:POEM-MELAYU");

        RpslObject result = subject.getByKey(ObjectType.POEM, "POEM-MELAYU");
        assertThat(result.getType(), is(ObjectType.POEM));
        assertThat(result.getKey().toString(), is("POEM-MELAYU"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentPoemLookup() {
        subject.getByKey(ObjectType.POEM, "nonexistent");
    }

    /*
     * Key-cert
     */

    @Test
    public void successfulKeyCertQuery() {
        databaseHelper.addObject("key-cert:PGPKEY-7FDA55DE");

        RpslObject result = subject.getByKey(ObjectType.KEY_CERT, "PGPKEY-7FDA55DE");
        assertThat(result.getType(), is(ObjectType.KEY_CERT));
        assertThat(result.getKey().toString(), is("PGPKEY-7FDA55DE"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentKeyCertLookup() {
        subject.getByKey(ObjectType.KEY_CERT, "nonexistent");
    }

    /*
     * aut-num
     */

    @Test
    public void successfulAutNumQuery() {
        databaseHelper.addObject("aut-num:AS57875");

        RpslObject result = subject.getByKey(ObjectType.AUT_NUM, "AS57875");
        assertThat(result.getType(), is(ObjectType.AUT_NUM));
        assertThat(result.getKey().toString(), is("AS57875"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentAutNumQuery() {
        subject.getByKey(ObjectType.AUT_NUM, "nonexistent");
    }

    /*
     * rtr-set
     */

    @Test
    public void successRtrSetQuery() {
        databaseHelper.addObject("rtr-set:RTRS-WT");

        RpslObject result = subject.getByKey(ObjectType.RTR_SET, "RTRS-WT");
        assertThat(result.getType(), is(ObjectType.RTR_SET));
        assertThat(result.getKey().toString(), is("RTRS-WT"));
    }

    /*
     * as-set
     */

    @Test
    public void successAsSetQuery() {
        databaseHelper.addObject("as-set:AS-RIPENCC");

        RpslObject result = subject.getByKey(ObjectType.AS_SET, "AS-RIPENCC");
        assertThat(result.getType(), is(ObjectType.AS_SET));
        assertThat(result.getKey().toString(), is("AS-RIPENCC"));
    }

    /*
     * filter-set
     */

    @Test
    public void successFilterSetQuery() {
        databaseHelper.addObject("filter-set:FLTR-RIPE");

        RpslObject result = subject.getByKey(ObjectType.FILTER_SET, "FLTR-RIPE");
        assertThat(result.getType(), is(ObjectType.FILTER_SET));
        assertThat(result.getKey().toString(), is("FLTR-RIPE"));
    }

    /*
     * inet-rtr
     */

    @Test
    public void successInetRtrQuery() {
        databaseHelper.addObject("inet-rtr:Amsterdam.ripe.net\nlocal-as: AS101");

        RpslObject result = subject.getByKey(ObjectType.INET_RTR, "Amsterdam.ripe.net");
        assertThat(result.getType(), is(ObjectType.INET_RTR));
        assertThat(result.getKey().toString(), is("Amsterdam.ripe.net"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentInetRtrQuery() {
        subject.getByKey(ObjectType.INET_RTR, "nonexistent");
    }

    /*
     * peering-set
     */

    @Test
    public void successPeeringSetQuery() {
        databaseHelper.addObject("peering-set:AS31708:PRNG-CUST");

        RpslObject result = subject.getByKey(ObjectType.PEERING_SET, "AS31708:PRNG-CUST");
        assertThat(result.getType(), is(ObjectType.PEERING_SET));
        assertThat(result.getKey().toString(), is("AS31708:PRNG-CUST"));
    }

    /*
     * route-set
     */

    @Test
    public void successRouteSetQuery() {
        databaseHelper.addObject("route-set:RS-TWN-AMS-RIPE");

        RpslObject result = subject.getByKey(ObjectType.ROUTE_SET, "RS-TWN-AMS-RIPE");
        assertThat(result.getType(), is(ObjectType.ROUTE_SET));
        assertThat(result.getKey().toString(), is("RS-TWN-AMS-RIPE"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void nonexistentRoleQuery() {
        subject.getByKey(ObjectType.ROLE, "nonexistent");
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_get_unknown_object() {
        int objectId = -1;
        subject.getById(objectId);
    }

    @Test
    public void test_adminc_related_to_role() {
        RpslObject person = databaseHelper.addObject(RpslObject.parse("person:Brian Riddle\nnic-hdl:BRD-RIPE"));
        RpslObject role = databaseHelper.addObject(RpslObject.parse("role:RIPE NCC Operations\nadmin-c:BRD-RIPE\nnic-hdl:OPS4-RIPE"));

        List<RpslObjectInfo> result = subject.relatedTo(role, Collections.<ObjectType>emptySet());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getKey(), is(person.getKey().toString()));
    }

    @Test
    public void test_adminc_related_to_role_excluded() {
        databaseHelper.addObject(RpslObject.parse("person:Brian Riddle\nnic-hdl:BRD-RIPE"));
        RpslObject role = databaseHelper.addObject(RpslObject.parse("role:RIPE NCC Operations\nadmin-c:BRD-RIPE\nnic-hdl:OPS4-RIPE"));

        List<RpslObjectInfo> result = subject.relatedTo(role, Collections.singleton(ObjectType.PERSON));
        assertThat(result, hasSize(0));
    }

    @Test
    public void getByKey_not_normalized() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "netname:         NETNAME\n" +
                "source:          RIPE\n");

        databaseHelper.addObject(rpslObject);

        final RpslObject byKey = subject.getByKey(ObjectType.INET6NUM, "2001:658:21a::/48");
        assertThat(byKey, is(rpslObject));
    }

    @Test
    public void getByKeys_not_normalized() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "netname:         NETNAME\n" +
                "source:          RIPE\n");

        databaseHelper.addObject(rpslObject);

        final List<RpslObject> byKeys = subject.getByKeys(ObjectType.INET6NUM, ciSet("2001:658:21a::/48"));
        assertThat(byKeys, contains(rpslObject));
    }

    @Test
    public void getByKeys_different_type() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:          Test\n" +
                "nic-hdl:         TEST-PN\n" +
                "source:          RIPE\n");

        databaseHelper.addObject(rpslObject);

        final List<RpslObject> byKeys = subject.getByKeys(ObjectType.ROLE, ciSet("TEST-PN"));
        assertThat(byKeys, hasSize(0));
    }
}
