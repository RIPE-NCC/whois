package net.ripe.db.whois.api.rest;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.mail.EmailStatusType;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.dns.DnsGatewayStub;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class SyncUpdatesServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private DnsGatewayStub dnsGatewayStub;

    private static final String MNTNER_TEST_MNTNER = "" +
            "mntner:        mntner-mnt\n" +
            "descr:         description\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "notify:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$TTjmcwVq$zvT9UcvASZDQJeK8u9sNU.    # emptypassword\n" +
            "mnt-by:        mntner-mnt\n" +
            "source:        TEST";

    private static final String PERSON_ANY1_TEST = "" +
            "person:        Test Person\n" +
            "nic-hdl:       TP1-TEST\n" +
            "source:        TEST";

    private static final String NOTIFY_PERSON_TEST = "" +
            "person:    Pauleth Palthen \n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "notify:    test@ripe.net\n" +
            "notify:    test1@ripe.net\n" +
            "mnt-by:    mntner-mnt\n" +
            "nic-hdl:   TP2-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n";

    @Autowired
    private MailSenderStub mailSender;

    @Autowired
    private IpRanges ipRanges;

    @Autowired
    private EmailStatusDao emailStatusDao;

    @Test
    public void get_empty_request() {
        final Response response = RestTest.target(getPort(), "whois/syncupdates/test")
                    .request()
                    .get(Response.class);

        final String responseBody = response.readEntity(String.class);
        assertThat(responseBody, containsString("You have requested Help information from the RIPE NCC Database"));
        assertThat(responseBody, containsString("From-Host: 127.0.0.1"));
        assertThat(responseBody, containsString("Date/Time: "));
        assertThat(responseBody, not(containsString("$")));
    }

    @Test
    public void get_help_parameter_only() {
        final Response response = RestTest.target(getPort(), "whois/syncupdates/test?HELP=yes")
                .request()
                .get(Response.class);

        assertThat(response.getHeaderString(HttpHeaders.CONTENT_ENCODING), is(nullValue()));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE), is(MediaType.TEXT_PLAIN));

        final String responseBody = response.readEntity(String.class);
        assertThat(responseBody, containsString("You have requested Help information from the RIPE NCC Database"));
        assertThat(responseBody, containsString("From-Host: 127.0.0.1"));
        assertThat(responseBody, containsString("Date/Time: "));
        assertThat(responseBody, not(containsString("$")));
    }

    @Test
    public void get_help_parameter_only_compressed() {
        final Response response = RestTest.target(getPort(), "whois/syncupdates/test?HELP=yes")
                .request()
                .header(HttpHeaders.ACCEPT_ENCODING,"gzip, deflate")
                .get(Response.class);

        assertThat(response.getHeaderString(HttpHeaders.CONTENT_ENCODING), is("gzip"));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE), is(MediaType.TEXT_PLAIN));
    }

    @Test
    public void post_without_content_type() throws Exception {
        assertThat(postWithoutContentType(), containsString("Bad Request"));
    }

    @Test
    public void post_multipart_form_help_parameter_only() {
        final FormDataMultiPart multipart = new FormDataMultiPart().field("HELP", "help");
        String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void post_url_encoded_form_help_parameter_only() {
        String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("HELP=yes", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void help_and_invalid_parameter() {
        String response = RestTest.target(getPort(), "whois/syncupdates/test?HELP=yes&INVALID=true")
                .request()
                .get(String.class);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void help_and_data_parameters() {
        String response = RestTest.target(getPort(), "whois/syncupdates/test?HELP=yes&DATA=data")
                .request()
                .get(String.class);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void diff_parameter_only() {
        try {
            RestTest.target(getPort(), "whois/syncupdates/test?DIFF=yes")
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("the DIFF method is not actually supported by the Syncupdates interface"));
        }
    }

    @Test
    public void redirect_ignored() throws Exception {
        ipRanges.setTrusted("0/0", "::0/0");
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "REDIRECT=yes&DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\nremarks: updated" + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
        assertThat(getMessage("noreply@ripe.net"), not(nullValue()));
        assertThat(anyMoreMessages(), is(false));
    }

    @Test
    public void notify_without_redirect() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\nremarks: updated" + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] mntner"));
        assertThat(getMessage("noreply@ripe.net"), not(nullValue()));
        assertThat(anyMoreMessages(), is(false));
    }

    @Test
    public void create_object_only_data_parameter() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void update_object_only_data_parameter() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\nremarks: updated" + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void update_object_dryrun_parameter() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword" + "\ndry-run: TEST"))
                .request()
                .get(String.class);

        assertThat(response, containsString("No operation: [mntner] mntner"));
        assertThat(response, containsString("***Info:    Dry-run performed, no changes to the database have been made"));
    }

    @Test
    public void non_break_spaces_are_substituted_with_regular_space() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject("" +
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-MNT\n" +
                "source:        TEST");

        final String person = "" +
                "person:    Test\u00a0Person\n" +
                "address:   Amsterdam\n" +
                "phone:     +31-6-123456\n" +
                "nic-hdl:   TP2-TEST\n" +
                "mnt-by:    SSO-MNT\n" +
                "source:    TEST";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(person))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [person] TP2-TEST"));
    }

    @Test
    public void create_person_only_data_parameter_with_sso_token() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject("" +
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-MNT\n" +
                "source:        TEST");

        final String person = "" +
                "person:    Test Person\n" +
                "address:   Amsterdam\n" +
                "phone:     +31-6-123456\n" +
                "nic-hdl:   TP2-TEST\n" +
                "mnt-by:    SSO-MNT\n" +
                "source:    TEST";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(person))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [person] TP2-TEST"));
    }

    @Test
    public void create_person_only_data_parameter_with_invalid_sso_token() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject("" +
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-MNT\n" +
                "source:        TEST");

        final String person = "" +
                "person:    Test Person\n" +
                "address:   Amsterdam\n" +
                "phone:     +31-6-123456\n" +
                "nic-hdl:   TP2-TEST\n" +
                "mnt-by:    SSO-MNT\n" +
                "source:    TEST";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(person))
                .request()
                .cookie("crowd.token_key", "invalid-token")
                .get(String.class);

        assertThat(response, containsString("Create FAILED: [person] TP2-TEST   Test Person"));
        assertThat(response, containsString(
                "***Error:   Authorisation for [person] TP2-TEST failed\n" +
                "            using \"mnt-by:\"\n" +
                "            not authenticated by: SSO-MNT"));
    }

    @Test
    public void create_maintainer_only_data_parameter_with_sso_token() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final String mntner =
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        mntner-mnt\n" +
                "source:        TEST";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(mntner + "\npassword: emptypassword"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
        assertThat(databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-MNT").getValueForAttribute(AttributeType.AUTH),
                is(CIString.ciString("SSO 906635c2-0405-429a-800b-0602bd716124")));
    }

    @Test
    public void create_multiple_objects_with_single_password() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final String firstPerson =
                "person:        First Person\n" +
                "address:       Amsterdam\n" +
                "phone:         +31\n" +
                "nic-hdl:       FP1-TEST\n" +
                "mnt-by:        mntner-mnt\n" +
                "source:        TEST\n";
        final String secondPerson =
                "person:        Second Person\n" +
                "address:       Amsterdam\n" +
                "phone:         +31\n" +
                "nic-hdl:       SP1-TEST\n" +
                "mnt-by:        mntner-mnt\n" +
                "source:        TEST\n";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(
                                firstPerson +
                                "password: emptypassword\n\n\n" +
                                secondPerson))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [person] FP1-TEST   First Person"));
        assertThat(response, containsString("Create SUCCEEDED: [person] SP1-TEST   Second Person"));
    }

    @Test
    public void create_selfrefencing_maintainer_new_and_data_parameters_with_sso_token() {
        databaseHelper.addObject(PERSON_ANY1_TEST);

        final String mntner =
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-MNT\n" +
                "source:        TEST";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(mntner) + "&NEW=yes")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_selfreferencing_maintainer_password_with_spaces() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final String mntner =
                "mntner:        TESTING-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$7jwEckGy$EjyaikWbwDB2I4nzM0Fgr1 # pass %95{word}?\n" +
                "mnt-by:        TESTING-MNT\n" +
                "source:        TEST";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(mntner + "\npassword: pass %95{word}?\n"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] TESTING-MNT"));
    }

    @Test
    public void create_person_with_changed_attribute() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(
                                "person:        Test Person\n" +
                                "address:       Amsterdam\n" +
                                "phone:         +31\n" +
                                "nic-hdl:       TP2-RIPE\n" +
                                "mnt-by:        mntner-mnt\n" +
                                "changed:       user@host.org 20171025\n" +
                                "source:        TEST\n" +
                                "password: emptypassword\n"),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [person] TP2-RIPE   Test Person"));
        assertThat(response, containsString("***Warning: Deprecated attribute \"changed\". This attribute has been removed."));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-RIPE").containsAttribute(AttributeType.CHANGED), is(false));
    }

    @Test
    public void create_person_with_changed_attributes() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(
                                "person:        Test Person\n" +
                                "address:       Amsterdam\n" +
                                "phone:         +31\n" +
                                "nic-hdl:       TP2-RIPE\n" +
                                "mnt-by:        mntner-mnt\n" +
                                "changed:       user@host.org 20171025\n" +
                                "changed:       user1@host.org 20171026\n" +
                                "changed:       user2@host.org 20171027\n" +
                                "changed:       user3@host.org 20171028\n" +
                                "source:        TEST\n" +
                                "password: emptypassword\n"),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [person] TP2-RIPE   Test Person"));
        assertThat(response, containsString("***Warning: Deprecated attribute \"changed\". This attribute has been removed."));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-RIPE").containsAttribute(AttributeType.CHANGED), is(false));
    }

    @Test
    public void create_person_accept_encoding_none() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .header(HttpHeaders.ACCEPT_ENCODING,"none")
                .post(Entity.entity(
                        "DATA=" + SyncUpdateUtils.encode(
                                "person:        Test Person\n" +
                                "address:       Amsterdam\n" +
                                "phone:         +31\n" +
                                "nic-hdl:       TP2-RIPE\n" +
                                "mnt-by:        mntner-mnt\n" +
                                "source:        TEST\n" +
                                "password: emptypassword\n"), MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [person] TP2-RIPE   Test Person"));
    }

    @Test
    public void modify_generated_attributes_changes_last_modified_attribute() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.AUT_NUM));

        final String AUTNUM_TEST = "" +
                "aut-num:        AS104\n" +
                "status:         ASSIGNED\n" +
                "as-name:        End-User-2\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         mntner-mnt\n" +
                "created:         2001-02-04T17:00:00Z\n" +
                "last-modified:   2001-02-04T17:00:00Z\n" +
                "source:         TEST\n";

        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);
        databaseHelper.addObject(AUTNUM_TEST);

        final CIString orginialModifiedDate = databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104").getValueForAttribute(AttributeType.LAST_MODIFIED);

        final ZonedDateTime oldDateTime = testDateTimeProvider.getCurrentZonedDateTime();
        testDateTimeProvider.setTime(oldDateTime.toLocalDateTime());

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode("aut-num:        AS104\n" +
                "status:         OTHER\n" +
                "as-name:        End-User-2\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         mntner-mnt\n" +
                "source:         TEST-NONAUTH\n" +
                "override:       agoston,zoh\n"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [aut-num] AS104"));
        assertThat(databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104").getValueForAttribute(AttributeType.LAST_MODIFIED), is(not(orginialModifiedDate)));
        assertThat(databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104").getValueForAttribute(AttributeType.STATUS), is("OTHER"));
        assertThat(databaseHelper.lookupObject(ObjectType.AUT_NUM, "AS104").getValueForAttribute(AttributeType.SOURCE), is("TEST-NONAUTH"));
    }

    @Test
    public void create_selfreferencing_maintainer_post_url_encoded_data() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(
                                "mntner:        TESTING-MNT\n" +
                                "descr:         description\n" +
                                "admin-c:       TP1-TEST\n" +
                                "upd-to:        noreply@ripe.net\n" +
                                "auth:          MD5-PW $1$7jwEckGy$EjyaikWbwDB2I4nzM0Fgr1 # pass %95{word}?\n" +
                                "mnt-by:        TESTING-MNT\n" +
                                "source:        TEST\n" +
                                "password: pass %95{word}?\n"),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] TESTING-MNT"));
    }

    @Test
    public void update_selfrefencing_maintainer_only_data_parameter_with_sso_token() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        final String mntner =
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-MNT\n" +
                "source:        TEST";
        databaseHelper.addObject(mntner);

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(mntner + "\nremarks: updated"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] SSO-MNT"));
    }

    @Test
    public void create_maintainer_invalid_source_in_url() {
        try {
            RestTest.target(getPort(), "whois/syncupdates/invalid?" +
                    "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Invalid source specified: invalid"));
        }
    }

    @Test
    public void create_maintainer_invalid_source_in_data() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        final String mntnerInvalidSource = MNTNER_TEST_MNTNER.replaceAll("source:\\s+TEST", "source: invalid");

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(mntnerInvalidSource + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Error:   Unrecognized source: INVALID"));
    }

    @Test
    public void update_maintainer_only_data_parameter() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\nremarks: new" + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void only_new_parameter() {
        try {
            RestTest.target(getPort(), "whois/syncupdates/test?NEW=yes")
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("DATA parameter is missing"));
        }
    }

    @Test
    public void new_and_data_parameters_get_request() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword") + "&NEW=yes")
                .request()
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void new_and_data_parameters_existing_object_get_request() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\nremarks: new" + "\npassword: emptypassword") + "&NEW=yes")
                .request()
                .get(String.class);

        assertThat(response, containsString(
                "***Error:   Enforced new keyword specified, but the object already exists in the\n" +
                "            database"));
    }

    @Test
    public void new_and_data_parameters_urlencoded_post_request() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(
                        "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER +
                        "\npassword: emptypassword") + "&NEW=yes", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void new_and_data_parameters_multipart_post_request() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final FormDataMultiPart multipart = new FormDataMultiPart().field("DATA", MNTNER_TEST_MNTNER + "\npassword: emptypassword").field("NEW", "yes");
        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void post_url_encoded_data() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(
                                "person:     Test Person\n" +
                                "address:    Flughafenstraße 109/a\n" +
                                "phone:      +49 282 411141\n" +
                                "fax-no:     +49 282 411140\n" +
                                "nic-hdl:    TP1-TEST\n" +
                                "mnt-by:     mntner\n" +
                                "source:     INVALID\n" +
                                "password: emptypassword"),
                        MediaType.valueOf("application/x-www-form-urlencoded")), String.class);

        assertThat(response, containsString("***Error:   Unrecognized source: INVALID"));
        assertThat(response, containsString("address:        Flughafenstraße 109/a"));
    }

    @Test
    public void post_url_encoded_data_with_latin1_charset_error() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(
                                "person:     Test Person\n" +
                                "address:    Flughafenstraße 109/a\n" +
                                "phone:      +49 282 411141\n" +
                                "fax-no:     +49 282 411140\n" +
                                "nic-hdl:    TP1-TEST\n" +
                                "mnt-by:     mntner\n" +
                                "source:     INVALID\n" +
                                "password:   emptypassword", StandardCharsets.ISO_8859_1),
                        MediaType.valueOf("application/x-www-form-urlencoded; charset=ISO-8859-1")), String.class);

        assertThat(response, containsString("***Error:   Unrecognized source: INVALID"));
        assertThat(response, containsString("address:        Flughafenstraße 109/a"));
    }

    @Test
    public void post_url_encoded_data_with_latin1_charset() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity("DATA=" + SyncUpdateUtils.encode(
                                "person:     Test Person\n" +
                                "address:    Flughafenstraße 109/a\n" +
                                "phone:      +49 282 411141\n" +
                                "fax-no:     +49 282 411140\n" +
                                "nic-hdl:    TP1-TEST\n" +
                                "mnt-by:     mntner-mnt\n" +
                                "source:     TEST\n" +
                                "password:   emptypassword", StandardCharsets.ISO_8859_1),
                        MediaType.valueOf("application/x-www-form-urlencoded; charset=ISO-8859-1")), String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [person] TP1-TEST"));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP1-TEST").toString(),
                containsString("address:        Flughafenstraße 109/a"));
    }

    @Test
    public void post_url_encoded_data_with_non_latin1_address_error() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post( Entity.entity("DATA=" +  SyncUpdateUtils.encode(
                    "person:    Test Person again\n" +
                    "address:   Тверская улица,москва\n" +
                    "phone:     +31-6-123456\n" +
                    "nic-hdl:   TP2-TEST\n" +
                    "mnt-by:    mntner-mnt\n" +
                    "source:    INVALID\n" +
                    "password:  emptypassword"),
                  MediaType.valueOf("application/x-www-form-urlencoded; charset=UTF-8")), String.class);

        assertThat(response, containsString("***Error:   Unrecognized source: INVALID"));
        assertThat(response, containsString("address:        ???????? ?????,??????"));
    }

    @Test
    public void post_url_encoded_data_with_non_latin1_address() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post( Entity.entity("DATA=" +  SyncUpdateUtils.encode(
                    "person:    Test Person again\n" +
                    "address:   Тверская улица,москва\n" +
                    "phone:     +31-6-123456\n" +
                    "nic-hdl:   TP2-TEST\n" +
                    "mnt-by:    mntner-mnt\n" +
                    "source:    TEST\n" +
                    "password:  emptypassword"),
                  MediaType.valueOf("application/x-www-form-urlencoded; charset=UTF-8")), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(),
                containsString("address:        ???????? ?????,??????"));
    }

    @Test
    public void post_empty_body() {
        final Response response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post( null);

        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400));
    }

    @Test
    public void post_url_encoded_data_with_control_characters_address() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post( Entity.entity("DATA=" +  SyncUpdateUtils.encode(
                    "person:    Test Person again\n" +
                    "address:   Test\u000B\u000c\u007F\u008F Address\n" +
                    "phone:     +31-6-123456\n" +
                    "nic-hdl:   TP2-TEST\n" +
                    "mnt-by:    mntner-mnt\n" +
                    "source:    TEST\n" +
                    "password:  emptypassword"),
                  MediaType.valueOf("application/x-www-form-urlencoded; charset=UTF-8")), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(),
                containsString("address:        Test???? Address"));
    }

    @Test
    public void post_url_encoded_data_with_latin1_email_address_converted_to_punycode() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post( Entity.entity("DATA=" +  SyncUpdateUtils.encode(
                    "person:    Test Person\n" +
                    "address:   Zürich\n" +
                    "e-mail:    no-reply@zürich.example\n" +
                    "phone:     +31-6-123456\n" +
                    "nic-hdl:   TP2-TEST\n" +
                    "mnt-by:    mntner-mnt\n" +
                    "source:    TEST\n" +
                    "password:  emptypassword"),
                  MediaType.valueOf("application/x-www-form-urlencoded; charset=UTF-8")), String.class);

        assertThat(response, containsString("***Warning: Value changed due to conversion of IDN email address(es) into\n            Punycode\n"));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(),
                containsString("e-mail:         no-reply@xn--zrich-kva.example"));
    }

    @Test
    public void post_url_encoded_data_with_cyrillic_email_address_converted_to_punycode() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post( Entity.entity("DATA=" +  SyncUpdateUtils.encode(
                    "person:    Test Person\n" +
                    "address:   Moscow\n" +
                    "e-mail:    no-reply@москва.ru\n" +
                    "phone:     +31-6-123456\n" +
                    "nic-hdl:   TP2-TEST\n" +
                    "mnt-by:    mntner-mnt\n" +
                    "source:    TEST\n" +
                    "password:  emptypassword"),
                  MediaType.valueOf("application/x-www-form-urlencoded; charset=UTF-8")), String.class);

        assertThat(response, containsString("***Warning: Value changed due to conversion of IDN email address(es) into\n            Punycode\n"));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(),
                containsString("e-mail:         no-reply@xn--80adxhks.ru"));
    }

    @Test
    public void post_multipart_data_with_non_latin1_address() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        Тверская улица,москва\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP2-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "source:         TEST\n" +
                        "password: emptypassword")
                .field("NEW", "yes");
        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(),
                containsString("address:        ???????? ?????,??????"));
    }

    @Test
    public void post_multipart_data_with_control_characters_address() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        Test\u000b\u000c\u007F\u008f Address\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP2-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "source:         TEST\n" +
                        "password: emptypassword")
                .field("NEW", "yes");
        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(),
                containsString("address:        Test???? Address"));
    }


    @Test
    public void post_multipart_data_with_latin1_non_ascii_address() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        ÅçÅç\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP2-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "source:         TEST\n" +
                        "password: emptypassword")
                .field("NEW", "yes");
        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(),
                containsString("address:        ÅçÅç"));
    }

    @Test
    public void post_multipart_data_with_latin1_non_ascii_address_latin1_encoded() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        ÅçÅç\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP2-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "source:         TEST\n" +
                        "password: emptypassword")
                .field("NEW", "yes");
        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, new MediaType("multipart", "form-data", StandardCharsets.ISO_8859_1.displayName())), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(), containsString("address:        ÅçÅç"));
    }

    @Test
    public void post_multipart_data_with_latin1_email_address_converted_to_punycode() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        Zürich\n" +
                        "e-mail:         no-reply@zürich.example\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP2-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "source:         TEST\n" +
                        "password: emptypassword")
                .field("NEW", "yes");
        RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, new MediaType("multipart", "form-data", StandardCharsets.ISO_8859_1.displayName())), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(), containsString("e-mail:         no-reply@xn--zrich-kva.example"));
    }

    @Test
    public void create_person_with_filtered_source() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        Home\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP2-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "source:         TEST #Filtered\n" +
                        "password: emptypassword")
                .field("NEW", "yes");

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(response, containsString("Create FAILED"));
        assertThat(response, containsString("***Error:   End of line comments not allowed on \"source:\" attribute"));
    }

    @Test
    public void update_person_with_filtered_source() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        Home\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP1-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "remarks:         test remark\n" +
                        "remarks:         another test remark\n" +
                        "source:         TEST #Filtered\n" +
                        "password: emptypassword");

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(response, containsString("Modify FAILED"));
        assertThat(response, containsString("End of line comments not allowed on \"source:\" attribute"));
    }

    @Test
    public void update_person_with_lower_case_source() {
        databaseHelper.addObject(RpslObject.parse(
            "person:        Test Person\n" +
            "nic-hdl:       TP1-TEST\n" +
            "source:        test"));
        databaseHelper.addObject(MNTNER_TEST_MNTNER);
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP1-TEST").toString(), containsString("source:         test"));

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        Home\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP1-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "remarks:         test remark\n" +
                        "remarks:         another test remark\n" +
                        "source:         test\n" +
                        "password: emptypassword");

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, multipart.getMediaType()), String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [person] TP1-TEST   Test Person"));
    }

    @Test
    public void replace_attributes_when_rpsl_has_double_generated_attributes() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("DATA",
                        "person:         Test Person\n" +
                        "address:        ÅçÅç\n" +
                        "phone:          +31 6 12345678\n" +
                        "nic-hdl:        TP2-TEST\n" +
                        "mnt-by:         mntner-mnt\n" +
                        "source:         TEST\n" +
                        "created:       2016-03-31T09:10:52Z\n" +
                        "created:       2016-03-31T09:11:52Z\n" +
                        "last-modified: 2016-03-31T09:10:52Z\n" +
                        "last-modified: 2016-03-31T09:11:52Z\n" +
                        "password: emptypassword")
                .field("NEW", "yes");

        final String response = RestTest.target(getPort(), "whois/syncupdates/test")
                .request()
                .post(Entity.entity(multipart, new MediaType("multipart", "form-data", StandardCharsets.ISO_8859_1.displayName())), String.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(), containsString("address:        ÅçÅç"));
        assertThat(response, containsString("Create SUCCEEDED"));
        assertThat(response, containsString("***Warning: Supplied attribute 'created' has been replaced with"));
        assertThat(response, containsString("***Warning: Supplied attribute 'last-modified' has been replaced with"));
    }

    @Test
    public void notify_from_sso() throws MessagingException, IOException {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final String mntner =
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        test@test.nl\n" +
                "notify:        test@test.nl\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        mntner-mnt\n" +
                "source:        TEST";

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(mntner + "\npassword: emptypassword"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        final MimeMessage message = getMessage("test@test.nl");
        assertThat(message.getContent().toString(), containsString("You can reply to this message to contact the person who made this change."));
        assertThat(message.getContent().toString(), not(containsString("Please DO NOT reply to this message.")));
        assertThat(getAddressesAsString(message.getReplyTo()), containsInAnyOrder("person@net.net"));

        assertThat(response, containsString("Create SUCCEEDED: [mntner] SSO-MNT"));
        assertThat(databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-MNT"), not(nullValue()));
    }

    @Test
    public void no_notify_from_sso_when_override_used() throws MessagingException, IOException {

        databaseHelper.insertUser(User.createWithPlainTextPassword("user", "password", ObjectType.PERSON));

        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        final RpslObject person = new RpslObjectBuilder(PERSON_ANY1_TEST)
                .addAttributeSorted(new RpslAttribute(AttributeType.NOTIFY, "test@test.net"))
                .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "address"))
                .addAttributeSorted(new RpslAttribute(AttributeType.PHONE, "+123456789"))
                .addAttributeSorted(new RpslAttribute(AttributeType.MNT_BY, "mntner-mnt"))
                .get();

        databaseHelper.updateObject(person);

        final String updatedPerson = new RpslObjectBuilder(databaseHelper.lookupObject(ObjectType.PERSON, person.getKey().toString()))
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "test"))
                .get()
                .toString();

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                "DATA=" + SyncUpdateUtils.encode(updatedPerson + "override: user,password\n"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        final MimeMessage message = getMessage("test@test.net");
        assertThat(message.getContent().toString(), not(containsString("You can reply to this message to contact the person who made this change.")));
        assertThat(message.getContent().toString(), containsString("Please DO NOT reply to this message."));
        assertThat(getAddressesAsString(message.getReplyTo()), not(containsInAnyOrder("person@net.net")));

        assertThat(response, containsString("Modify SUCCEEDED: [person] TP1-TEST   Test Person"));
    }

    @Test
    public void create_multiple_domain_object_fail_dns_timeout() {

        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.AUT_NUM));

        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);

        databaseHelper.addObject("" +
                "inet6num:      1a00:fb8::/23\n" +
                "mnt-by:        mntner-mnt\n" +
                "mnt-domains:   mntner-mnt\n" +
                "source:        TEST");
        final RpslObject domain1 = RpslObject.parse("" +
                "domain:        e.0.0.0.a.1.ip6.arpa\n" +
                "descr:         Reverse delegation for 1a00:fb8::/23\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "nserver:       ns1.xs4all.nl\n" +
                "nserver:       ns2.xs4all.nl\n" +
                "mnt-by:        NON-EXISTING-MNT\n" +
                "source:        TEST");

        final String updatedPerson = new RpslObjectBuilder(databaseHelper.lookupObject(ObjectType.PERSON, "TP1-TEST"))
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "test"))
                .get()
                .toString();

        dnsGatewayStub.addResponse(CIString.ciString("e.0.0.0.a.1.ip6.arpa"), UpdateMessages.dnsCheckMessageParsingError());

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(updatedPerson + "\npassword: emptypassword\n\n\n" + domain1))
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .get(String.class);

        assertThat(response, containsString("Create FAILED: [domain] e.0.0.0.a.1.ip6.arpa"));
        assertThat(response, containsString("***Error:   Error parsing response while performing DNS check"));
    }


    @Test
    public void unsubscribed_and_undeliverable_notify_user_gets_warn_when_updating() {
        databaseHelper.addObject(PERSON_ANY1_TEST);
        databaseHelper.addObject(MNTNER_TEST_MNTNER);
        databaseHelper.addObject(NOTIFY_PERSON_TEST);

        final String person = "" +
                "person:    Pauleth Palthen \n" +
                "address:   Singel 258 test\n" +
                "remarks:   test\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "notify:    test@ripe.net\n" +
                "notify:    test1@ripe.net\n" +
                "mnt-by:    mntner-mnt\n" +
                "nic-hdl:   TP2-TEST\n" +
                "remarks:   remark\n" +
                "source:    TEST\n";

        emailStatusDao.createEmailStatus("test@ripe.net", EmailStatusType.UNSUBSCRIBE);
        emailStatusDao.createEmailStatus("test1@ripe.net", EmailStatusType.UNDELIVERABLE);

        final String response = RestTest.target(getPort(),
                        "whois/syncupdates/test?" + "DATA=" + SyncUpdateUtils.encode(person + "\npassword: emptypassword"))
                .request()
                .cookie("crowd.token_key", "valid-token")
                .get(String.class);

        assertThat(response, containsString("Modify SUCCEEDED: [person] TP2-TEST"));
        assertThat(response, containsString("***Warning: Not sending notification to test@ripe.net because it is unsubscribe."));
        assertThat(response, containsString("***Warning: Not sending notification to test1@ripe.net because it is\n" +
                "            undeliverable."));
    }

    @Test
    public void create_object_only_data_parameter_over_http() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final String response = RestTest.target(getPort(), "whois/syncupdates/test?" +
                        "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                .request()
                .get(String.class);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
        assertThat(response, containsString(
                "This Syncupdates request used insecure HTTP, which will be removed\n" +
                        "            in a future release. Please switch to HTTPS."));
    }


    // helper methods

    private MimeMessage getMessage(final String to) throws MessagingException {
        return mailSender.getMessage(to);
    }

    private boolean anyMoreMessages() {
        return mailSender.anyMoreMessages();
    }

    private Set<String> getAddressesAsString(final Address[] addresses) {
        return Arrays.stream(addresses)
            .map(Address::toString)
            .collect(Collectors.toSet());
    }

    private String postWithoutContentType() throws IOException {
        final URL url = new URL(String.format("http://localhost:%d/whois/syncupdates/test", getPort()));
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");

        connection.connect();
        final String response = connection.getResponseMessage();
        connection.disconnect();

        return response;
    }
}
