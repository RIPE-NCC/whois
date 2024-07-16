package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.client.AsyncInvoker;
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
import org.apache.commons.compress.utils.Lists;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@Tag("IntegrationTest")
public class WhoisRestServiceDoSTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("dos.filter.enabled", "true");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    // LookUps
    @Test
    public void multiple_lookUps_per_second_then_429_too_many_requests() throws InterruptedException {
        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40")
                .request()
                .get();
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final Invocation.Builder lookupRequest = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40").request();
        final List<Integer> statuses = Lists.newArrayList();
        for (int i = 0; i < 50; i++) {
            statuses.add(lookupRequest.get().getStatus());
        }

        assertThat(HttpStatus.OK_200, is(statuses.get(48)));
        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(statuses.getLast()));

        TimeUnit.SECONDS.sleep(1); // Free the IP after one second

        //After a second, the user can perform more requests
        assertThat(HttpStatus.OK_200, is(lookupRequest.get().getStatus()));
    }

    @Test
    public void multiple_async_lookUps_per_second_then_429_too_many_requests() throws InterruptedException {
        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40")
                .request()
                .get();
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final AsyncInvoker lookupRequest = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40").request().async();

        // Simulate a DoS attack by sending many POST requests in a short time asynchronously
        final List<CompletableFuture<Response>> futures = IntStream.range(0, 50)
                .mapToObj(i -> sendAsyncGet(lookupRequest))
                .toList();

        // Wait for all requests to complete
        final CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        // Collect status
        final List<Integer> statuses = futures.stream()
                .map(future -> {
                    try {
                        return future.get().getStatus();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .toList();
        final List<Integer> manyRequestStatuses = statuses.stream().filter(status -> HttpStatus.TOO_MANY_REQUESTS_429 == status).toList();
        assertThat(manyRequestStatuses.size(), is(1));

        TimeUnit.SECONDS.sleep(1); // Free the IP after one second

        //After a second, the user can perform more requests
        final Response unLockedResponse = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=10.20.30.40")
                .request()
                .get();

        assertThat(HttpStatus.OK_200, is(unLockedResponse.getStatus()));
    }

    @Test
    public void multiple_lookUps_per_second_but_white_list_IP_then_200() {
        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .request()
                .get();
        assertThat(HttpStatus.OK_200, is(response.getStatus()));

        final Invocation.Builder lookupRequest = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT").request();
        final List<Integer> statuses = Lists.newArrayList();
        for (int i = 0; i < 50; i++) {
            statuses.add(lookupRequest.get().getStatus());
        }

        assertThat(HttpStatus.OK_200, is(statuses.getLast()));
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
        final List<Integer> statuses = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            statuses.add(updateRequest.put(Entity.entity(map(person), MediaType.APPLICATION_XML)).getStatus());
        }

        assertThat(HttpStatus.OK_200, is(statuses.get(8)));
        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(statuses.getLast()));

        TimeUnit.SECONDS.sleep(1); // Free the IP after one second

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

        final AsyncInvoker updateRequest = RestTest.target(getPort(), "whois/test/person/PP1-TEST?clientIp=10.20.30.40&password=test").request().async();

        // Simulate a DoS attack by sending many POST requests in a short time asynchronously
        final List<CompletableFuture<Response>> futures = IntStream.range(0, 10)
                .mapToObj(i -> sendAsyncPost(updateRequest, person))
                .toList();

        // Wait for all requests to complete
        final CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        // Collect status
        final List<Integer> statuses = futures.stream()
                .map(future -> {
                    try {
                        return future.get().getStatus();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .toList();
        final List<Integer> manyRequestStatuses = statuses.stream().filter(status -> HttpStatus.TOO_MANY_REQUESTS_429 == status).toList();
        assertThat(manyRequestStatuses.size(), is(1));

        TimeUnit.SECONDS.sleep(1); // Free the IP after one second

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
        final List<Integer> statuses = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            statuses.add(updateRequest.put(Entity.entity(map(person), MediaType.APPLICATION_XML)).getStatus());
        }

        assertThat(HttpStatus.OK_200, is(statuses.getLast()));
    }

    //Helper methods

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

    private CompletableFuture<Response> sendAsyncPost(final AsyncInvoker asyncInvoker, final RpslObject rpslObject) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        asyncInvoker.put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML), new jakarta.ws.rs.client.InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                future.complete(response);
            }

            @Override
            public void failed(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    private CompletableFuture<Response> sendAsyncGet(final AsyncInvoker asyncInvoker) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        asyncInvoker.get(new jakarta.ws.rs.client.InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                future.complete(response);
            }

            @Override
            public void failed(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }
}
