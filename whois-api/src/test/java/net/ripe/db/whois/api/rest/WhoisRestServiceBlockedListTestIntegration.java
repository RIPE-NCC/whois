package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.httpserver.jmx.BlockListJmx;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryServer;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class WhoisRestServiceBlockedListTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    @Autowired
    private BlockListJmx blockListJmx;
    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    @Autowired
    QueryServer queryServer;


    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    private static final RpslObject PERSON_OBJECT = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject UPDATED_PERSON_OBJECT = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258 updated\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("ipranges.blocked.list", "193.0.0.0 - 193.0.23.255, 2001:67c:2e8::/48");
    }

    @AfterAll
    public static void clear(){ System.clearProperty("ipranges.blocked.list"); }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
    }


    /*
        Lookup
     */
    @Test
    public void add_blocked_ipv4_get_request_then_429_too_many_requests(){
        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=8.8.8.8")
                .request()
                .get();

        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress("8.8.8.8");

        final Response errorResponse = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=8.8.8.8")
                .request()
                .get();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("8.8.8.8").getFormattedText()));
    }

    @Test
    public void add_blocked_get_request_telnet(){

        databaseHelper.updateObject("" +
                "person:        Test Person1\n" +
                "nic-hdl:       TP1-TEST\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z");

        assertThat( TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 TP1-TEST"),
                containsString("TP1-TEST"));

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress(LOCALHOST);

        assertThat(TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--diff-versions 1 TP1-TEST"),
                containsString(QueryMessages.accessDeniedForAbuse(LOCALHOST).getFormattedText()));

        blockListJmx.removeBlockedListAddress(LOCALHOST);
        assertThat( TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "--show-version 1 TP1-TEST"),
                containsString("TP1-TEST"));

    }

    @Test
    public void add_blocked_ipv6_get_request_then_429_too_many_requests(){
        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=2001:fff:001::")
                .request()
                .get();

        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress("2001:fff:001::/48");

        final Response errorResponse = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=2001:fff:001::")
                .request()
                .get();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:fff:001::").getFormattedText()));
    }

    @Test
    public void remove_blocked_ipv4_get_request_then_200() {
        final String reformedClientRequest = "whois/test/mntner/OWNER-MNT?clientIp=193.0.0.1";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .get();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("193.0.0.1").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("193.0.0.0 - 193.0.23.255");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .get();

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }

    @Test
    public void remove_blocked_ipv6_get_request_then_200() {
        final String reformedClientRequest = "whois/test/mntner/OWNER-MNT?clientIp=2001:67c:2e8::";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .get();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:67c:2e8::").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("2001:67c:2e8::/48");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .get();

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }


     /*
        Post
     */
     @Test
     public void add_blocked_ipv4_post_request_then_429_too_many_requests() {
         final Response response = RestTest.target(getPort(), "whois/test/person?clientIp=8.8.8.8&password=test")
                 .request()
                 .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

         assertThat(HttpStatus.OK_200, is(response.getStatus()));

         // Add IP to blocked list
         blockListJmx.addBlockedListAddress("8.8.8.8");

         final Response errorResponse = RestTest.target(getPort(), "whois/test/person?clientIp=8.8.8.8&password=test")
                 .request()
                 .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

         assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
         assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("8.8.8.8").getFormattedText()));
     }

    @Test
    public void add_blocked_ipv6_post_request_then_429_too_many_requests() {
        final Response response = RestTest.target(getPort(), "whois/test/person?clientIp=2001:fff:001::&password=test")
                .request()
                .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress("2001:fff:001::/48");

        final Response errorResponse = RestTest.target(getPort(), "whois/test/person?clientIp=2001:fff:001::&password=test")
                .request()
                .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:fff:001::").getFormattedText()));
    }

    @Test
    public void remove_blocked_ipv4_post_request_then_200() {
        final String reformedClientRequest = "whois/test/person?clientIp=193.0.0.1&password=test";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("193.0.0.1").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("193.0.0.0 - 193.0.23.255");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }

    @Test
    public void remove_blocked_ipv6_post_request_then_200() {
        final String reformedClientRequest = "whois/test/person?clientIp=2001:67c:2e8::&password=test";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:67c:2e8::").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("2001:67c:2e8::/48");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .post(Entity.entity(map(PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }


    /*
       Update
    */
    @Test
    public void add_blocked_ipv4_put_request_then_429_too_many_requests() {
        databaseHelper.addObject(PERSON_OBJECT);

        final Response response = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=8.8.8.8&password=test")
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress("8.8.8.8");

        final Response errorResponse = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=8.8.8.8&password=test")
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("8.8.8.8").getFormattedText()));
    }

    @Test
    public void add_blocked_ipv6_put_request_then_429_too_many_requests() {
        databaseHelper.addObject(PERSON_OBJECT);
        final Response response = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=2001:fff:001::&password=test")
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress("2001:fff:001::/48");

        final Response errorResponse = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=2001:fff:001::&password=test")
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:fff:001::").getFormattedText()));
    }

    @Test
    public void remove_blocked_ipv4_put_request_then_200() {
        databaseHelper.addObject(PERSON_OBJECT);

        final String reformedClientRequest = "whois/test/person/PP1-TEST?clientIp=193.0.0.1&password=test";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("193.0.0.1").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("193.0.0.0 - 193.0.23.255");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }

    @Test
    public void remove_blocked_ipv6_put_request_then_200() {
        databaseHelper.addObject(PERSON_OBJECT);

        final String reformedClientRequest = "whois/test/person/PP1-TEST?clientIp=2001:67c:2e8::&password=test";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:67c:2e8::").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("2001:67c:2e8::/48");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .put(Entity.entity(map(UPDATED_PERSON_OBJECT), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }


    /*
        Delete
    */
   @Test
    public void add_blocked_ipv4_delete_request_then_429_too_many_requests() {
        databaseHelper.addObject(PERSON_OBJECT);
       final Response response = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=8.8.8.8&password=test")
               .request()
               .delete();

       assertThat(HttpStatus.OK_200, is(response.getStatus()));

       databaseHelper.addObject(PERSON_OBJECT);

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress("8.8.8.8");

        final Response errorMessage = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=8.8.8.8&password=test")
                .request()
                .delete();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorMessage.getStatus()));
        assertThat(errorMessage.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("8.8.8.8").getFormattedText()));
    }

    @Test
    public void add_blocked_ipv6_delete_request_then_429_too_many_requests() {
        databaseHelper.addObject(PERSON_OBJECT);

        final Response response = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=2001:fff:001::&password=test")
                .request()
                .delete();

        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        databaseHelper.addObject(PERSON_OBJECT);

        // Add IP to blocked list
        blockListJmx.addBlockedListAddress("2001:fff:001::/48");

        final Response errorResponse = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=2001:fff:001::&password=test")
                .request()
                .delete();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:fff:001::").getFormattedText()));
    }

    @Test
    public void remove_blocked_ipv4_delete_request_then_200() {
        databaseHelper.addObject(PERSON_OBJECT);

        final String reformedClientRequest = "whois/test/person/PP1-TEST?clientIp=193.0.0.1&password=test";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .delete();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("193.0.0.1").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("193.0.0.0 - 193.0.23.255");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .delete();

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }

    @Test
    public void remove_blocked_ipv6_delete_request_then_200() {
        databaseHelper.addObject(PERSON_OBJECT);

        final String reformedClientRequest = "whois/test/person/PP1-TEST?clientIp=2001:67c:2e8::&password=test";

        final Response errorResponse = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .delete();

        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(errorResponse.getStatus()));
        assertThat(errorResponse.readEntity(String.class), containsString(QueryMessages.accessDeniedForAbuse("2001:67c:2e8::").getFormattedText()));

        // Remove IP from blocked list
        blockListJmx.removeBlockedListAddress("2001:67c:2e8::/48");

        final Response response = RestTest.target(getPort(), reformedClientRequest)
                .request()
                .delete();

        assertThat(HttpStatus.OK_200, is(response.getStatus()));
    }

    //Helper methods
    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
