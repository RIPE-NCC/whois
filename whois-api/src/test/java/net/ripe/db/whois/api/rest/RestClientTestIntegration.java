package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.query.QueryFlag;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.core.Cookie;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
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

    private static final RpslObject SECOND_MNT = RpslObject.parse("" +
            "mntner:        SECOND-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$1ZnhrEYU$h8QUAsDPLZYOYVjm3uGQr1 #secondmnt\n" +
            "mnt-by:        OWNER-MNT\n" +
            "referral-by:   OWNER-MNT\n" +
            "changed:       dbtest@ripe.net 20120101\n" +
            "source:        TEST");

    RestClient restClient;

    @Before
    public void setup() throws Exception {
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);

        restClient = new RestClient(String.format("http://localhost:%d/whois", getPort()), "TEST");
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

        final Collection<RpslObject> result = restClient.request()
                .addParam("query-string", "AS102")
                .addParams("type-filter", ObjectType.AUT_NUM.getName())
                .addParams("flags", QueryFlag.NO_REFERENCED.getName())
                .search();

        final Iterator<RpslObject> iterator = result.iterator();
        assertTrue(iterator.hasNext());
        final RpslObject rpslObject = iterator.next();
        assertFalse(iterator.hasNext());
        assertThat(rpslObject.getKey().toUpperCase(), is("AS102"));
    }

    @Test
    public void update_person_with_empty_remarks_has_remarks() throws Exception {
        final RpslObject object = new RpslObjectBuilder(TEST_PERSON).append(new RpslAttribute(AttributeType.REMARKS, "")).sort().get();

        final RpslObject updatedResult = restClient.request()
                .addParam("password", "test")
                .update(object);

        assertThat(updatedResult.findAttributes(AttributeType.REMARKS), hasSize(1));
    }

    @Test
    public void lookup_without_password() throws Exception {
        final RpslObject object = restClient.request().lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString());

        assertThat(object.findAttribute(AttributeType.AUTH).getValue(), is("MD5-PW # Filtered"));
        assertThat(object.findAttribute(AttributeType.AUTH).getCleanComment(), is("Filtered"));
    }

    @Test
    public void lookup_with_wrong_password() throws Exception {
        final RpslObject object = restClient.request().lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString());

        assertThat(object.getValueForAttribute(AttributeType.SOURCE).toString(), is("TEST"));
        assertThat(object.findAttribute(AttributeType.SOURCE).getCleanComment(), is("Filtered"));
        assertThat(object.getValueForAttribute(AttributeType.AUTH).toString(), is("MD5-PW"));
        assertThat(object.findAttribute(AttributeType.AUTH).getCleanComment(), is("Filtered"));
    }

    @Test
    public void lookup_with_correct_password() throws Exception {
        final RpslObject object = restClient.request()
                .addParam("password", "test")
                .lookup(ObjectType.MNTNER, OWNER_MNT.getKey().toString());

        assertThat(object.findAttribute(AttributeType.AUTH).getValue(), is("MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ # test"));
        assertThat(object.findAttribute(AttributeType.AUTH).getCleanComment(), is("test"));
        assertThat(object.findAttribute(AttributeType.SOURCE).getCleanComment(), not(is("Filtered")));
        assertThat(object.findAttribute(AttributeType.SOURCE).getCleanComment(), is(nullValue()));
    }

    @Test
    public void lookup_person_is_unfiltered() throws Exception {
        final RpslObject object = restClient.request()
                .lookup(ObjectType.PERSON, TEST_PERSON.getKey().toString());

        assertThat(object.findAttribute(AttributeType.SOURCE).getCleanComment(), not(is("Filtered")));
        assertThat(object.findAttribute(AttributeType.SOURCE).getCleanComment(), is(nullValue()));
    }

    @Test
    public void lookup_with_sso_password() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:        SSO-MNT\n" +
                "descr:         sso Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO random@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        SSO-MNT\n" +
                "referral-by:   SSO-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST"));

        final RpslObject object = restClient.request()
                .addParam("password", "test")
                .lookup(ObjectType.MNTNER, "SSO-MNT");

        assertThat(object.findAttributes(AttributeType.AUTH),
                hasItems(new RpslAttribute(AttributeType.AUTH, "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/"),
                        new RpslAttribute(AttributeType.AUTH, "SSO random@ripe.net")));
    }

    @Test
    public void lookup_with_sso_only() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:        SSO-MNT\n" +
                "descr:         sso Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-MNT\n" +
                "referral-by:   SSO-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST"));

        final RpslObject object = restClient.request()
                .addCookie(new Cookie("crowd.token_key", "valid-token"))
                .lookup(ObjectType.MNTNER, "SSO-MNT");

        assertThat(object.findAttributes(AttributeType.AUTH),
                hasItem(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")));
    }

    @Test
    public void create_and_lookup_with_sso_only() throws Exception {
        final RpslObject object = RpslObject.parse("" +
                "mntner:        SSO-XX-MNT\n" +
                "descr:         sso Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-XX-MNT\n" +
                "referral-by:   SSO-XX-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST");

        final RpslObject returnedObject = restClient.request()
                .addCookie(new Cookie("crowd.token_key", "valid-token"))
                .create(object);

        final RpslObject lookedUpObject = restClient.request()
                .addCookie(new Cookie("crowd.token_key", "valid-token"))
                .lookup(ObjectType.MNTNER, "SSO-XX-MNT");

        assertThat(lookedUpObject.findAttributes(AttributeType.AUTH),
                hasItem(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")));
    }

    @Test
    public void lookup_mntner_with_mntby_password() throws Exception {
        databaseHelper.addObject(SECOND_MNT);

        RpslObject obj = restClient.request()
                .addParam("password", "test")
                .lookup(SECOND_MNT.getType(), SECOND_MNT.getKey().toString());

        assertThat(obj.getValueForAttribute(AttributeType.AUTH).toString(), startsWith("MD5-PW"));
        assertThat(obj.getValueForAttribute(AttributeType.AUTH).toString(), not(is("MD5-PW")));
    }

    @Test
    public void lookup_mntner_with_one_of_mntby_passwords() throws Exception {
        RpslObject THIRD_MNT = RpslObject.parse("" +
                "mntner:        THIRD-MNT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$L9a6Y39t$wuu.ykzgp596KK56tpJm31 #thirdmnt\n" +
                "mnt-by:        OWNER-MNT\n" +
                "mnt-by:        SECOND-MNT\n" +
                "referral-by:   OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST");

        databaseHelper.addObject(SECOND_MNT);
        databaseHelper.addObject(THIRD_MNT);

        RpslObject obj = restClient.request()
                .addParam("password", "secondmnt")
                .lookup(THIRD_MNT.getType(), THIRD_MNT.getKey().toString());

        assertThat(obj.getValueForAttribute(AttributeType.AUTH).toString(), startsWith("MD5-PW"));
        assertThat(obj.getValueForAttribute(AttributeType.AUTH).toString(), not(is("MD5-PW")));
    }

    @Test
    public void lookup_abuse_contact() {
        final RpslObject ABUSE_CONTACT_ROLE = RpslObject.parse("" +
                "role:          Abuse Contact\n" +
                "nic-hdl:       AC1-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "source:        TEST");

        final RpslObject ABUSE_CONTACT_ORGANISATION = RpslObject.parse("" +
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

        final RpslObject ABUSE_CONTACT_INETNUM = RpslObject.parse("" +
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


        databaseHelper.addObjects(ABUSE_CONTACT_ROLE, ABUSE_CONTACT_ORGANISATION, ABUSE_CONTACT_INETNUM);
        resetIpTrees();

        final AbuseContact abuseContact = restClient.request().lookupAbuseContact("193.0.0.1");

        assertThat(abuseContact.getEmail(), is("abuse@test.net"));
    }

    @Test
    public void lookup_abuse_contact_not_found() {
        try {
            restClient.request().lookupAbuseContact("10.0.0.1");
            fail();
        } catch (RestClientException e) {
            assertThat(e.getErrorMessages().get(0).getText(), is("No abuse contact found for 10.0.0.1"));
        }
    }

    @Test
    public void lookup_person_multiple_matches() {
        databaseHelper.addObject(
                "person:        WW Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       WP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.addObject(
                "person:        Someone Else\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       WW\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");

        final RpslObject response = restClient.request().lookup(ObjectType.PERSON, "WW");

        assertThat(response.getValueForAttribute(AttributeType.NIC_HDL).toString(), is("WW"));
    }

    @Test
    public void lookup_route_primary_key_must_be_fully_defined() {
        databaseHelper.addObject(
                "inetnum:       193.0.0.0 - 193.0.0.255\n" +
                "netname:       RIPE-NCC\n" +
                "descr:         some description\n" +
                "country:       NL\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        SUB-ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "aut-num:       AS3333\n" +
                "as-name:       RIPE-NCC-ONE\n" +
                "descr:         RIPE-NCC\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       noreply@ripe.net 20120101\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "aut-num:       AS3334\n" +
                "as-name:       RIPE-NCC-TWO\n" +
                "descr:         RIPE-NCC\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       noreply@ripe.net 20120101\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "route:         193.0.0.0/21\n" +
                "descr:         RIPE-NCC\n" +
                "origin:        AS3333\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       noreply@ripe.net 20120101\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "route:         193.0.0.0/21\n" +
                "descr:         RIPE-NCC\n" +
                "origin:        AS3334\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       noreply@ripe.net 20120101\n" +
                "source:        TEST");
        resetIpTrees();

        // return nothing on partial primary key
        try {
            final RpslObject responseNoOrgin = restClient.request().lookup(ObjectType.ROUTE, "193.0.0.0/21");
            fail("no result on partial primary key");
        } catch (RestClientException e){
            assertThat(e.getErrorMessages().get(0).toString(), is("ERROR:101: no entries found\n\nNo entries found in source TEST.\n"));
        }

        // primary key is fully specified (one match)
        final RpslObject responseWithOrgin = restClient.request().lookup(ObjectType.ROUTE, "193.0.0.0/21AS3334");
        assertThat(responseWithOrgin.getValueForAttribute(AttributeType.ROUTE).toString(), is("193.0.0.0/21"));
        assertThat(responseWithOrgin.getValueForAttribute(AttributeType.ORIGIN).toString(), is("AS3334"));
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

        restClient.request()
                .addParam("reason", "not used anymore")
                .addParam("password", "test")
                .delete(person);

        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
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

        restClient.request()
                .addParam("reason", "")
                .addParam("password", "test")
                .delete(person);

        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void create_with_attribute_comment() {
        final RpslObject person = RpslObject.parse("" +
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP2-TEST\n" +
                "mnt-by:        OWNER-MNT # comment\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");

        final RpslObject created = restClient.request()
                .addParam("password", "test")
                .create(person);

        assertThat(created, is(person));
    }

    @Test
    public void streaming_search_shows_correct_message_in_empty_result() {
        try {
            restClient.request().addParam("query-string", "bla").streamingSearch();
        } catch (RestClientException e){
            assertThat(e.getErrorMessages().get(0).toString(), is("ERROR:101: no entries found\n\nNo entries found in source TEST.\n"));
        }
    }

    @Test
    public void streaming_search_shows_correct_message_in_bad_request() {
        try {
            restClient.request().addParam("query-ng", "bla").streamingSearch();
        } catch (RestClientException e){
            assertThat(e.getErrorMessages().get(0).toString(), is("Query param 'query-string' cannot be empty"));
        }
    }
}
