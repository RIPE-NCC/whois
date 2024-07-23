package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.eclipse.jetty.http.HttpStatus.TOO_MANY_REQUESTS_429;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@Tag("IntegrationTest")
public class WhoisRestServiceDoSTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    @Value("${dos.filter.max.updates:10}")
    private String dosUpdatesMaxSecs;

    @Value("${dos.filter.max.query:50}")
    private String dosQueryMaxSecs;

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    private static final Integer SECONDS_NEEDED_TO_FREE_IP = 1;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("dos.filter.enabled", "true");
    }

    @AfterAll
    public static void clear(){ System.clearProperty("dos.filter.enabled"); }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
    }

    // lookup
    @Test
    public void multiple_lookup_per_second_then_429_too_many_requests() throws InterruptedException {
        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40")
                .request()
                .get();
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final Invocation.Builder lookupRequest = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40").request();

        // Simulate a DoS attack by sending many GET requests in a short time
        final Map<Integer, Integer> responsesCodesCount = IntStream.range(0, Integer.parseInt(dosQueryMaxSecs))
                .mapToObj(lookupCount -> lookupRequest.get())
                .map(Response::getStatus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )));

        assertThat(responsesCodesCount.size(), is(2)); //Two different status codes
        assertThat(responsesCodesCount.get(HttpStatus.OK_200), is (Integer.parseInt(dosQueryMaxSecs) - 1));
        assertThat(responsesCodesCount.get(TOO_MANY_REQUESTS_429), is (1));

        TimeUnit.SECONDS.sleep(SECONDS_NEEDED_TO_FREE_IP); // Free the IP after one second

        //After a second, the user can perform more requests
        final Response unLockedResponse = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40")
                .request()
                .get();

        assertThat(HttpStatus.OK_200, is(unLockedResponse.getStatus()));
    }

    @Test
    public void multiple_async_lookup_per_second_then_429_too_many_requests() throws InterruptedException {
        final Response creation = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40")
                .request()
                .get();
        assertThat(HttpStatus.OK_200, is(creation.getStatus()));

        final Invocation.Builder lookupRequest = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40").request();

        // Simulate a DoS attack by sending many GET requests in a short time asynchronously
        final Map<Integer, Integer> responsesCodesCount = IntStream.range(0, Integer.parseInt(dosQueryMaxSecs))
                .parallel()
                .mapToObj(lookupCount -> lookupRequest.get())
                .map(Response::getStatus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                        Collectors.counting(),
                        Long::intValue
                        )));

        assertThat(responsesCodesCount.size(), is(2)); //Two different status codes
        assertThat(responsesCodesCount.get(HttpStatus.OK_200), is(Integer.parseInt(dosQueryMaxSecs) - 1));
        assertThat(responsesCodesCount.get(TOO_MANY_REQUESTS_429), is (1));

        TimeUnit.SECONDS.sleep(SECONDS_NEEDED_TO_FREE_IP); // Free the IP after one second

        //After a second, the user can perform more requests
        final Response unLockedResponse = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40")
                .request()
                .get();

        assertThat(HttpStatus.OK_200, is(unLockedResponse.getStatus()));
    }

    @Test
    public void multiple_lookup_per_second_but_white_list_IP_then_200() {
        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .request()
                .get();
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final Invocation.Builder lookupRequest = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT").request();

        // Simulate a DoS attack by sending many GET requests in a short time
        final Map<Integer, Integer> responsesCodesCount = IntStream.range(0, Integer.parseInt(dosQueryMaxSecs))
                .parallel()
                .mapToObj(lookupCount -> lookupRequest.get())
                .map(Response::getStatus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )));

        assertThat(responsesCodesCount.size(), is(1));
        assertThat(responsesCodesCount.get(HttpStatus.OK_200), is (Integer.parseInt(dosQueryMaxSecs)));
    }

    // Updates
    @Test
    public void multiple_updates_per_second_then_429_too_many_requests() throws InterruptedException {
        final RpslObject person = RpslObject.parse("" +
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        final Response response = RestTest.target(getPort(), "whois/test/person?clientIp=10.20.30.40&password=test")
                .request()
                .post(Entity.entity(map(person), MediaType.APPLICATION_XML));
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final Invocation.Builder updateRequest = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=10.20.30.40&password=test").request();

        // Simulate a DoS attack by sending many PUT requests in a short time
        final Map<Integer, Integer> responsesCodesCount = IntStream.range(0, Integer.parseInt(dosUpdatesMaxSecs))
                .mapToObj(updateCount -> updateRequest.put(Entity.entity(map(person), MediaType.APPLICATION_XML)))
                .map(Response::getStatus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )));

        assertThat(responsesCodesCount.size(), is(2));
        assertThat(responsesCodesCount.get(HttpStatus.OK_200), is (Integer.parseInt(dosUpdatesMaxSecs) - 1));
        assertThat(responsesCodesCount.get(TOO_MANY_REQUESTS_429), is (1));

        TimeUnit.SECONDS.sleep(SECONDS_NEEDED_TO_FREE_IP); // Free the IP after one second

        //After a second, the user can perform more requests
        final Response unLockedResponse = updateRequest.put(Entity.entity(map(person), MediaType.APPLICATION_XML));
        assertThat(HttpStatus.OK_200, is(unLockedResponse.getStatus()));
    }

    @Test
    public void multiple_async_updates_per_second_then_429_too_many_requests() throws InterruptedException {
        final RpslObject person = RpslObject.parse("" +
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        final Response response = RestTest.target(getPort(), "whois/test/person?clientIp=10.20.30.40&password=test")
                .request()
                .post(Entity.entity(map(person), MediaType.APPLICATION_XML));
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final Invocation.Builder updateRequest = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=10.20.30.40&password=test").request();

        // Simulate a DoS attack by sending many PUT requests in a short time asynchronously
        final Map<Integer, Integer> responsesCodesCount = IntStream.range(0, Integer.parseInt(dosUpdatesMaxSecs))
                .parallel()
                .mapToObj(updateCount -> updateRequest.put(Entity.entity(map(person), MediaType.APPLICATION_XML)))
                .map(Response::getStatus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )));

        assertThat(responsesCodesCount.size(), is(2));
        assertThat(responsesCodesCount.get(HttpStatus.OK_200), is (Integer.parseInt(dosUpdatesMaxSecs) - 1));
        assertThat(responsesCodesCount.get(TOO_MANY_REQUESTS_429), is (1));

        TimeUnit.SECONDS.sleep(SECONDS_NEEDED_TO_FREE_IP); // Free the IP after one second

        //After a second, the user can perform more requests
        final Response unLockedResponse = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=10.20.30.40&password=test")
                .request()
                .put(Entity.entity(map(person), MediaType.APPLICATION_XML));

        assertThat(HttpStatus.OK_200, is(unLockedResponse.getStatus()));
    }

    @Test
    public void multiple_updates_per_second_but_white_list_IP_then_200() {
        final RpslObject person = RpslObject.parse("" +
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        final Response response = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(person), MediaType.APPLICATION_XML));
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final Invocation.Builder updateRequest = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test").request();

        // Simulate a DoS attack by sending many PUT requests in a short time
        final Map<Integer, Integer> responsesCodesCount = IntStream.range(0, Integer.parseInt(dosUpdatesMaxSecs))
                .mapToObj(updateCount -> updateRequest.put(Entity.entity(map(person), MediaType.APPLICATION_XML)))
                .map(Response::getStatus)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )));

        assertThat(responsesCodesCount.size(), is(1));
        assertThat(responsesCodesCount.get(HttpStatus.OK_200), is(Integer.parseInt(dosUpdatesMaxSecs)));
    }


    //Helper methods
    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
