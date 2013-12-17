package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.query.QueryFlag;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class RestClientTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:        OWNER-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:        OWNER-MNT\n" +
            "referral-by:   OWNER-MNT\n" +
            "changed:       dbtest@ripe.net 20120101\n" +
            "source:        TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:        Test Person\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TP1-TEST\n" +
            "mnt-by:        OWNER-MNT\n" +
            "changed:       dbtest@ripe.net 20120101\n" +
            "source:        TEST\n");

    private static final RpslObject ABUSE_CONTACT_ROLE = RpslObject.parse("" +
            "role:          Abuse Contact\n" +
            "nic-hdl:       AC1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "source:        TEST");

    private static final RpslObject ABUSE_CONTACT_ORGANISATION = RpslObject.parse("" +
            "organisation:  ORG-RN1-TEST\n" +
            "org-name:      Ripe NCC\n" +
            "org-type:      OTHER\n" +
            "address:       Amsterdam\n" +
            "abuse-c:       AC1-TEST\n" +
            "e-mail:        some@email.net\n" +
            "mnt-ref:       OWNER-MNT\n" +
            "mnt-by:        OWNER-MNT\n" +
            "changed:       dbtest@ripe.net 20121016\n" +
            "source:        TEST");

    private static final RpslObject ABUSE_CONTACT_INETNUM = RpslObject.parse("" +
            "inetnum:       193.0.0.0 - 193.0.0.255\n" +
            "netname:       RIPE-NCC\n" +
            "descr:         some description\n" +
            "org:           ORG-RN1-TEST\n" +
            "country:       NL\n" +
            "admin-c:       TP1-TEST\n" +
            "tech-c:        TP1-TEST\n" +
            "status:        SUB-ALLOCATED PA\n" +
            "mnt-by:        OWNER-MNT\n" +
            "changed:       org@ripe.net 20120505\n" +
            "source:        TEST");

    @Autowired
    private IpTreeUpdater ipTreeUpdater;

    private RestClient restClient;

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);

        restClient = new RestClient();
        restClient.setRestApiUrl(String.format("http://localhost:%d/whois", getPort()));
        restClient.setSource("TEST");
    }

    @Test
    public void search() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final Iterable<RpslObject> result = restClient.search(
                "AS102",
                Collections.<String>emptySet(),
                Collections.<AttributeType>emptySet(),
                Collections.<String>emptySet(),
                Collections.<String>emptySet(),
                ImmutableSet.of(ObjectType.AUT_NUM),
                ImmutableSet.of(QueryFlag.NO_REFERENCED));

        final Iterator<RpslObject> iterator = result.iterator();
        assertTrue(iterator.hasNext());
        final RpslObject rpslObject = iterator.next();
        assertFalse(iterator.hasNext());
        assertThat(rpslObject.getKey().toUpperCase(), is("AS102"));
    }

    @Test
    public void update_person_with_empty_remarks_has_remarks() throws Exception {
        final RpslObject object = new RpslObjectBuilder(TEST_PERSON).addAttribute(new RpslAttribute(AttributeType.REMARKS, "")).sort().get();

        final RpslObject updatedResult = restClient.update(object, "test");

        assertThat(updatedResult.findAttributes(AttributeType.REMARKS), hasSize(1));
    }

    @Test
    public void lookup_without_password() throws Exception {
        final RpslObject object = restClient.lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString());

        assertThat(object.findAttribute(AttributeType.AUTH).getValue(), is("MD5-PW"));
    }

    @Test
    public void lookup_whoisObject_with_wrong_password() throws Exception {
        final WhoisObject object = restClient.lookupWhoisObject(ObjectType.MNTNER, OWNER_MNT.getKey().toString());
        Attribute expected1 = new Attribute("source", "TEST", "Filtered", null, null);
        Attribute expected2 = new Attribute("auth", "MD5-PW", "Filtered", null, null);
        assertThat(object.getAttributes(), hasItems(expected1, expected2));
    }

    @Test
    public void lookup_with_password() throws Exception {
        final RpslObject object = restClient.lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString(), "test");

        assertThat(object.findAttribute(AttributeType.AUTH).getValue(), is("MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/"));
    }

    @Test
    public void lookup_abuse_contact() {
        databaseHelper.addObject(ABUSE_CONTACT_ROLE);
        databaseHelper.addObject(ABUSE_CONTACT_ORGANISATION);
        databaseHelper.addObject(ABUSE_CONTACT_INETNUM);
        ipTreeUpdater.rebuild();

        final AbuseContact abuseContact = restClient.lookupAbuseContact("193.0.0.1");

        assertThat(abuseContact.getEmail(), is("abuse@test.net"));
    }

    @Test
    public void lookup_abuse_contact_not_found() {
        try {
            restClient.lookupAbuseContact("10.0.0.1");
            fail();
        } catch (RestClientException e) {
            assertThat(e.getErrorMessages().get(0).getText(), is("No abuse contact found for 10.0.0.1"));
        }
    }

    @Test
    public void delete_with_reason() {
        final RpslObject person = RpslObject.parse("" +
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.addObject(person);

        restClient.delete(person, "not used anymore", "test");

        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST");
            fail();
        } catch (EmptyResultDataAccessException expected) {}
    }

    @Test
    public void delete_without_reason() {
        final RpslObject person = RpslObject.parse("" +
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.addObject(person);

        restClient.delete(person, "", "test");

        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST");
            fail();
        } catch (EmptyResultDataAccessException expected) {}
    }
}