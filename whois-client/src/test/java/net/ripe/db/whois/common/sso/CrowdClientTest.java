package net.ripe.db.whois.common.sso;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static net.ripe.db.whois.common.sso.CrowdClient.CrowdResponse;
import static net.ripe.db.whois.common.sso.CrowdClient.CrowdSession;
import static net.ripe.db.whois.common.sso.CrowdClient.CrowdUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrowdClientTest {

    @Mock Response response;
    @Mock Client client;
    @Mock WebTarget webTarget;
    @Mock Invocation.Builder builder;

    CrowdClient subject;

    @Before
    public void setup() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.queryParam(anyString(), any())).thenReturn(webTarget);
        when(webTarget.request(any(String.class))).thenReturn(builder);
        when(webTarget.request()).thenReturn(builder);

        subject = new CrowdClient("http://testurl", "crowduser", "crowdpassword");
        ReflectionTestUtils.setField(subject, "client", client);
    }

    @Test
    public void login_success() {
        final String token = "xyz";
        when(builder.<CrowdSession>post(any(Entity.class), any(Class.class))).then(
            invocation ->
                new CrowdSession(new CrowdUser("test@ripe.net", "Test User", true), token, "2033-01-30T16:38:27.369+11:00")
            );

        assertThat(subject.login("test@ripe.net", "password"), is(token));
    }

    @Test
    public void login_not_authorized() {
        when(builder.<CrowdSession>post(any(Entity.class), any(Class.class))).then(
            invocation -> {
                when(response.getStatus()).thenReturn(401);
                when(response.getStatusInfo()).thenReturn(Response.Status.UNAUTHORIZED);
                when(response.readEntity(CrowdClient.CrowdError.class)).thenReturn(new CrowdClient.CrowdError("reason", "message"));
                throw new NotAuthorizedException(response);
            });

        try {
            subject.login("test@ripe.net", "password");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("message"));
        }
    }

    @Test
    public void get_user_session_success() {
        when(builder.<CrowdSession>post(any(Entity.class), any(Class.class))).then(
            invocation ->
                new CrowdSession(new CrowdUser("test@ripe.net", "Test User", true), null, "2033-01-30T16:38:27.369+11:00")
            );

        final UserSession session = subject.getUserSession("token");

        assertThat(session.getUsername(), is("test@ripe.net"));
        assertThat(session.getDisplayName(), is("Test User"));
        assertThat(session.isActive(), is(true));
    }

    @Test
    public void get_user_session_bad_request() {
        when(builder.<CrowdSession>post(any(Entity.class), any(Class.class))).then(invocation -> {throw new BadRequestException("Not valid sso");});

        try {
            subject.getUserSession("token");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access token: token"));
        }
    }

    @Test
    public void get_username_success() {
        when(builder.get(CrowdClient.CrowdUsers.class))
                .then(invocation -> new CrowdClient.CrowdUsers(
                        Arrays.asList(new CrowdClient.CrowdUser("test@ripe.net", "Test User", true)))
                );

        assertThat(subject.getUsername("uuid"), is("test@ripe.net"));
    }

    @Test
    public void get_username_not_found() {
        when(builder.get(CrowdClient.CrowdUsers.class)).then(invocation -> {throw new NotFoundException("message");});

        try {
            subject.getUsername("madeup-uuid");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access uuid: madeup-uuid"));
        }
    }

    @Test
    public void get_display_name_success() {
        when(builder.get(CrowdClient.CrowdUsers.class))
                .then(invocation -> new CrowdClient.CrowdUsers(
                        Arrays.asList(new CrowdClient.CrowdUser("test@ripe.net", "Test User", true)))
                );

        assertThat(subject.getDisplayName("uuid"), is("Test User"));
    }

    @Test
    public void get_display_name_not_found() {
        when(builder.get(CrowdClient.CrowdUsers.class)).then(invocation -> {throw new NotFoundException("message");});

        try {
            subject.getDisplayName("madeup-uuid");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access uuid: madeup-uuid"));
        }
    }


    @Test
    public void get_uuid_success() {
        when(builder.get(CrowdResponse.class)).then(invocation ->
                new CrowdResponse(Lists.newArrayList(
                        new CrowdClient.CrowdAttribute(Lists.newArrayList(
                                new CrowdClient.CrowdValue("1-2-3-4")), "uuid"))));

        assertThat(subject.getUuid("test@ripe.net"), is("1-2-3-4"));
    }


    @Test
    public void get_uuid_not_found() {
        when(builder.get(CrowdResponse.class)).then(invocation -> {throw new NotFoundException("message");});

        try {
            subject.getUuid("test@ripe.net");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access user: test@ripe.net"));
        }
    }

    @Test
    public void get_uuid_no_attribute() {
        final CrowdResponse crowdResponse = mock(CrowdResponse.class);
        when(crowdResponse.getUUID()).then(invocation -> {throw new NoSuchElementException();});
        when(builder.get(CrowdResponse.class)).thenReturn(crowdResponse);

        try {
            subject.getUuid("test@ripe.net");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Cannot find UUID for: test@ripe.net"));
        }

    }
}
