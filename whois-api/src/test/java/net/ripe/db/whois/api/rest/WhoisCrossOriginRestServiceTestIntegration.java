package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.query.QueryServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class WhoisCrossOriginRestServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired QueryServer queryServer;

    public static final String TEST_PERSON_STRING = "" +
            "person:         Test Person\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";

    private static final String PERSON_ANY1_TEST = "" +
            "person:        Test Person\n" +
            "nic-hdl:       TP4-TEST\n" +
            "source:        TEST";

    private static final String MNTNER_TEST_MNTNER = "" +
            "mntner:        mntner-mnt\n" +
            "descr:         description\n" +
            "admin-c:       TP4-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "notify:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$TTjmcwVq$zvT9UcvASZDQJeK8u9sNU.    # emptypassword\n" +
            "mnt-by:        mntner-mnt\n" +
            "source:        TEST";

    public static final RpslObject TEST_PERSON = RpslObject.parse(TEST_PERSON_STRING);
    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");


    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    private static final String TEST_ROLE_STRING = "" +
            "role:           Test Role\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TR1-TEST\n" +
            "admin-c:        TR1-TEST\n" +
            "abuse-mailbox:  abuse@test.net\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";
    private static final RpslObject TEST_ROLE = RpslObject.parse(TEST_ROLE_STRING);

    @Autowired private MaintenanceMode maintenanceMode;
    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private TestDateTimeProvider testDateTimeProvider;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        maintenanceMode.set("FULL,FULL");
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void lookup_cross_origin_from_db_ripe() {

        final Response response = RestTest.target(getPort(), "whois/test/inet6num/2001:2002:2003::/48")
                .request()
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .get(Response.class);

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://apps.db.ripe.net"));
    }

    @Test
    public void lookup_cross_origin_from_ripe() {

        final Response response = RestTest.target(getPort(), "whois/test/inet6num/2001:2002:2003::/48")
                .request()
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://stat.ripe.net")
                .get(Response.class);

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
    }

    @Test
    public void lookup_cross_origin_from_external_site() {
        final Response response = RestTest.target(getPort(), "whois/test/inet6num/2001:2002:2003::/48")
                .request()
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://example.com")
                .get(Response.class);

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
    }

    @Test
    public void cross_origin_preflight_request_from_apps_db_ripe() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request()
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .options();

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://apps.db.ripe.net"));
        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is("true"));
    }

    @Test
    public void cross_origin_get_request_from_apps_db_ripe_net_is_allowed() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request()
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://apps.db.ripe.net"));
        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is("true"));
        assertThat(response.readEntity(WhoisResources.class).getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_from_outside_ripe_net_with_cookie() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://www.foo.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is("false"));

        // actual request is still allowed (it's the browsers responsibility to honor the restriction)
        assertThat(response.readEntity(WhoisResources.class).getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_from_outside_ripe_net_is_allowed_no_sensitive_info_using_password() {
        databaseHelper.addObject("" +
                "mntner:         MNT-TEST" + "\n" +
                "descr:          test\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           SSO test@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/mntner/MNT-TEST?password=test&unfiltered")
                .request(MediaType.APPLICATION_XML_TYPE)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://www.foo.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO", "Filtered", null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void cross_origin_get_request_from_outside_ripe_net_is_allowed_no_sensitive_info_using_override() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("mherran", "zoh", ObjectType.MNTNER));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("mherran,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://www.foo.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO", "Filtered", null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void cross_origin_get_request_from_ripe_net_is_allowed_no_sensitive_info_using_password() {
        databaseHelper.addObject("" +
                "mntner:         MNT-TEST" + "\n" +
                "descr:          test\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           SSO test@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/mntner/MNT-TEST?password=test&unfiltered")
                .request(MediaType.APPLICATION_XML_TYPE)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://www.stat.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO", "Filtered", null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void cross_origin_get_request_from_ripe_net_is_allowed_no_sensitive_info_using_override() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("mherran", "zoh", ObjectType.MNTNER));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("mherran,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://www.stat.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO", "Filtered", null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void cross_origin_get_request_from_apps_db_ripe_net_is_allowed_override_no_sensitive_information() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("mherran", "zoh", ObjectType.MNTNER));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("mherran,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://stat.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO", "Filtered", null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void cross_origin_get_request_from_apps_db_ripe_net_is_allowed_override_with_sensitive_information() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("mherran", "zoh", ObjectType.MNTNER));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("mherran,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO person@net.net", null, null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void cross_origin_get_request_from_non_db_ripe_net_is_allowed_multiple_password_no_sensitive_information() {
        databaseHelper.addObject("" +
                "mntner:         MNT-TEST" + "\n" +
                "descr:          test\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           SSO test@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/mntner/MNT-TEST?password=test&password=test123&unfiltered")
                .request(MediaType.APPLICATION_XML_TYPE)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://stats.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO", "Filtered", null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }

    @Test
    public void create_object_syncupdate_only_data_parameter_not_allowed() {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        final Response whoisResources =
                RestTest.target(getPort(), "whois/syncupdates/test?" +
                            "DATA=" + SyncUpdateUtils.encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                    .request()
                    .header(com.google.common.net.HttpHeaders.ORIGIN, "https://stats.ripe.net")
                    .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                    .get(Response.class);

        assertThat(whoisResources.getStatus(), is(200));
    }

    @Test
    public void create_only_allowed_from_allow_list_origin() {
        final RpslObject ownerMnt = new RpslObjectBuilder(OWNER_MNT)
                .addAttribute(1, new RpslAttribute(AttributeType.AUTH, String.format("MD5-PW %s", PasswordHelper.hashMd5Password("+Pass word+"))))
                .get();
        databaseHelper.updateObject(ownerMnt);

        assertThrows(NotAuthorizedException.class, () -> {
            RestTest.target(getPort(), "whois/test/person")
                    .queryParam("password", "+Pass word+")
                    .request()
                    .header(com.google.common.net.HttpHeaders.ORIGIN, "https://stats.ripe.net")
                    .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);
        });

        assertThrows(NotAuthorizedException.class, () -> {
            RestTest.target(getPort(), "whois/test/person")
                    .queryParam("password", "+Pass word+")
                    .request()
                    .header(com.google.common.net.HttpHeaders.ORIGIN, "https://example.com")
                    .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);
        });

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person")
                .queryParam("password", "+Pass word+")
                .request()
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST"), is(not(nullValue())));
    }

    @Test
    public void update_only_allowed_from_allowed_origin_list() {
        final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                .replaceAttribute(TEST_PERSON.findAttribute(AttributeType.ADDRESS),
                        new RpslAttribute(AttributeType.ADDRESS, "Тверская улица,москва")).sort().get();

        final WhoisResources response = RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request()
                    .header(com.google.common.net.HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                    .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_XML),
                            WhoisResources.class);

        RestTest.assertWarningCount(response, 2);
        RestTest.assertErrorMessage(response, 1, "Warning", "Value changed due to conversion into the ISO-8859-1 (Latin-1) character set");

        final RpslObject lookupObject = databaseHelper.lookupObject(ObjectType.PERSON, "TP1-TEST");
        assertThat(lookupObject.findAttribute(AttributeType.ADDRESS).getValue(), is("        ???????? ?????,??????"));

        assertThrows(NotAuthorizedException.class, () -> {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request()
                    .header(com.google.common.net.HttpHeaders.ORIGIN, "https://example.com")
                    .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_XML),
                            WhoisResources.class);
        });
    }

    @Test
    public void delete_only_allowed_from_allow_list_hosts() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        assertThrows(NotAuthorizedException.class, () -> {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .queryParam("password", "test")
                    .request()
                    .header(com.google.common.net.HttpHeaders.ORIGIN, "https://stats.ripe.net")
                    .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                    .delete(WhoisResources.class);
        });

        assertThrows(NotAuthorizedException.class, () -> {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .queryParam("password", "test")
                    .request()
                    .header(com.google.common.net.HttpHeaders.ORIGIN, "https://example.com")
                    .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                    .delete(WhoisResources.class);
        });


        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("password", "test")
                .request()
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(com.google.common.net.HttpHeaders.HOST, "rest.db.ripe.net")
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
