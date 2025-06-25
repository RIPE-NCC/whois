package net.ripe.db.whois.api.httpserver;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

import static jakarta.ws.rs.client.Entity.*;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class RewriteEngineTestIntegration extends AbstractIntegrationTest {

    @BeforeAll
    public static void enableRewriteEngine() {
        System.setProperty("rewrite.engine.enabled", "true");
    }

    @AfterAll
    public static void disableRewriteEngine() {
        System.clearProperty("rewrite.engine.enabled");
    }

    @Value("${api.rest.baseurl}")
    private String restApiBaseUrl;

    @Autowired
    WhoisObjectMapper whoisObjectMapper;

    final RpslObject person = RpslObject.parse(
            "person:        Pauleth Palthen\n" +
                    "address:       Singel 258\n" +
                    "phone:         +31-1234567890\n" +
                    "e-mail:        noreply@ripe.net\n" +
                    "mnt-by:        TEST-MNT\n" +
                    "nic-hdl:       PP1-TEST\n" +
                    "remarks:       remark\n" +
                    "created:         2022-08-14T11:48:28Z\n" +
                    "last-modified:   2022-10-25T12:22:39Z\n" +
                    "source:        TEST\n");

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST\ncreated:         2022-08-14T11:48:28Z\nlast-modified:   2022-10-25T12:22:39Z\nsource: TEST\n");
        final RpslObject mntner = RpslObject.parse(
                "mntner:        TEST-MNT\n" +
                        "descr:         Test maintainer\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        upd-to@ripe.net\n" +
                        "mnt-nfy:       mnt-nfy@ripe.net\n" +
                        "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                        "mnt-by:        TEST-MNT\n" +
                        "created:         2022-08-14T11:48:28Z\n" +
                        "last-modified:   2022-10-25T12:22:39Z\n" +
                        "source:        TEST"
        );
        databaseHelper.addObject(mntner);
        databaseHelper.addObject(person);
    }

    @Test
    public void rest_lookup_with_rewrite() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "test/person/TP1-TEST")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .header(HttpHeader.X_FORWARDED_PROTO.toString(), HttpScheme.HTTP)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void dont_allow_password_over_http() {
        final ForbiddenException throwable = assertThrows(ForbiddenException.class, () ->
            RestTest.target(getPort(), "test/person/TP1-TEST?password=123")
                    .request()
                    .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                    .header(HttpHeader.X_FORWARDED_PROTO.toString(), HttpScheme.HTTP)
                    .get(WhoisResources.class)
        );


        assertThat(throwable.getResponse().getStatus(), is(403));
        /*final String error = throwable.getResponse().readEntity(String.class);
        assertThat(error.contains("""
                <title>Error 403 Forbidden</title>
                </head>
                <body><h2>HTTP ERROR 403 Forbidden</h2>
                <table>
                <tr><th>URI:</th><td>/test/person/TP1-TEST</td></tr>
                <tr><th>STATUS:</th><td>403</td></tr>
                <tr><th>MESSAGE:</th><td>Forbidden</td></tr>
                <tr><th>SERVLET:</th><td>-</td></tr>
                </table>
                """), is(true));*/
    }

    @Test
    public void rest_update_over_https() {
        final RpslObject updated = new RpslObjectBuilder(person)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        RestTest.target(getPort(), "test/person/PP1-TEST")
                .queryParam("password", "123")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .header(HttpHeader.X_FORWARDED_PROTO.toString(), HttpScheme.HTTPS)
                .put(entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updated), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(databaseHelper.lookupObject(PERSON, updated.getKey().toString()).containsAttribute(AttributeType.REMARKS), is(true));
    }

    @Test
    public void domain_object_creation_over_https() {
        try {
            RestTest.target(getPort(), "domain-objects/test")
                    .request()
                    .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                    .header(HttpHeader.X_FORWARDED_PROTO.toString(), HttpScheme.HTTPS)
                    .post(entity("{}", MediaType.APPLICATION_JSON), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("WhoisResources is mandatory"));
        }
    }

    @Test
    public void batch_update() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("batch", "batch", ObjectType.values()));

        final RpslObject updated = new RpslObjectBuilder(person)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "more_test"))
                .get();

        RestTest.target(getPort(), "batch/test")
                .queryParam("override", "batch,batch")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .header(HttpHeader.X_FORWARDED_PROTO.toString(), HttpScheme.HTTPS)
                .post(entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updated), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(databaseHelper.lookupObject(PERSON, updated.getKey().toString()).containsAttribute(AttributeType.REMARKS), is(true));
    }

    @Test
    public void rdap_lookup_with_rewrite() {
        final Entity person = RestTest.target(getPort(), "entity/TP1-TEST")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl).replace("rest", "rdap"))
                .get(Entity.class);

        assertThat(person.getHandle(), is("TP1-TEST"));
    }

    @Test
    public void syncupdates_with_rewrite() {
        final Response response = RestTest.target(getPort(), "?HELP=yes")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl).replace("rest", "syncupdates"))
                .get(Response.class);

        final String responseBody = response.readEntity(String.class);
        assertThat(responseBody, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void syncupdates_url_encoded_post_data() {
        final Response response = RestTest.target(getPort(), "")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl).replace("rest", "syncupdates"))
                .post(entity("DATA=" + SyncUpdateUtils.encode(
                                "person:        Test Person\n" +
                                "address:       Amsterdam\n" +
                                "phone:         +31\n" +
                                "nic-hdl:       TP2-RIPE\n" +
                                "mnt-by:        mntner-mnt\n" +
                                "changed:       user@host.org 20171025\n" +
                                "source:        TEST\n" +
                                "password: emptypassword\n"),
                        MediaType.valueOf("application/x-www-form-urlencoded")), Response.class);

        final String responseBody = response.readEntity(String.class);

        assertThat(responseBody, containsString("Create FAILED: [person] TP2-RIPE   Test Person"));
        assertThat(responseBody, not(containsString("You have requested Help information from the RIPE NCC Database")));
    }

    @Test
    public void rest_bad_request_fallthrough() {
        final BadRequestException throwable = assertThrows(BadRequestException.class, () ->
                RestTest.target(getPort(), "does_not_exist")
                        .request()
                        .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                        .get(WhoisResources.class)
        );
        final String error = throwable.getResponse().readEntity(String.class);
        assertThat(error.contains("""
                <title>Error 400 Bad Request</title>
                </head>
                <body><h2>HTTP ERROR 400 Bad Request</h2>
                <table>
                <tr><th>URI:</th><td>/does_not_exist</td></tr>
                <tr><th>STATUS:</th><td>400</td></tr>
                <tr><th>MESSAGE:</th><td>Bad Request</td></tr>
                <tr><th>SERVLET:</th><td>-</td></tr>
                </table>
                """), is(true));
    }

    @Test
    public void rest_root_redirect_to_doc() {
        final Response response =
            RestTest.target(getPort(), "")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .get(Response.class);

        assertThat(response.getStatus(), is(HttpStatus.FOUND_302));
        assertThat(response.getHeaderString("Location"), is("https://docs.db.ripe.net/RIPE-Database-Structure/REST-API-Data-model/#whoisresources"));

    }

    @Test
    public void rest_abuse_contact() {
        databaseHelper.addObject(
                "role:          Test Role\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31 6 12345678\n" +
                        "nic-hdl:       TR1-TEST\n" +
                        "admin-c:       TR1-TEST\n" +
                        "abuse-mailbox: abuse@test.net\n" +
                        "mnt-by:        TEST-MNT\n" +
                        "source:        TEST\n");

        databaseHelper.addObject("" +
                "inetnum:       193.0.0.0 - 193.0.0.255\n" +
                "abuse-c:        TR1-TEST\n" +
                "status:        ALLOCATED PA\n" +
                "source:        TEST");
        ipTreeUpdater.rebuild();

        final String result = RestTest.target(getPort(), "abuse-contact/193.0.0.1")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .get(String.class);

        assertThat(result, containsString("abuse@test.net"));
    }

    @Test
    public void cors_preflight_request() {
        Response response = RestTest.target(getPort(), "TEST/person/PP1-TEST")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .options();


        Response response2 = RestTest.target(getPort(), "test/person/PP1-TEST")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .options();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));

        assertThat(response2.getStatus(), is(HttpStatus.OK_200));
    }

    @Test
    public void whois_ripe_net() {
        Response response = RestTest.target(getPort(), "/some-value")
                .request()
                .header(HttpHeaders.HOST, "whois.ripe.net")
                .get();

        assertThat(response.getStatus(), is(HttpStatus.MOVED_PERMANENTLY_301));
        assertThat(response.getLocation(), is(URI.create("https://apps.db.ripe.net/db-web-ui/query")));
    }

    // helper methods

    private String getHost(final String url) {
        final URI uri = URI.create(url);
        return uri.getHost();
    }

}
