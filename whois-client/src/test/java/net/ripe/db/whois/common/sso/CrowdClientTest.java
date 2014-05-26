package net.ripe.db.whois.common.sso;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.util.NoSuchElementException;

import static net.ripe.db.whois.common.sso.CrowdClient.CrowdResponse;
import static net.ripe.db.whois.common.sso.CrowdClient.CrowdSession;
import static net.ripe.db.whois.common.sso.CrowdClient.CrowdUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
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
        when(webTarget.queryParam(anyString(), anyVararg())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);

        subject = new CrowdClient("http://testurl", "crowduser", "crowdpassword");
        ReflectionTestUtils.setField(client, "subject", client);
    }

    @Test
    public void login_success() {
        final String token = "xyz";
        when(builder.post(any(Entity.class), any(Class.class))).thenReturn(new CrowdSession(new CrowdUser("test@ripe.net", true), token, "2033-01-30T16:38:27.369+11:00"));

        assertThat(subject.login("test@ripe.net", "password"), is(token));
    }

    @Test
    public void login_not_authorized() {
        when(builder.post(any(Entity.class), any(Class.class))).thenAnswer(new Answer<CrowdSession>() {
            @Override
            public CrowdSession answer(InvocationOnMock invocation) throws Throwable {
                when(response.getStatus()).thenReturn(401);
                when(response.getStatusInfo()).thenReturn(Response.Status.UNAUTHORIZED);
                when(response.readEntity(CrowdClient.CrowdError.class)).thenReturn(new CrowdClient.CrowdError("reason", "message"));
                throw new NotAuthorizedException(response);
            }
        });

        try {
            subject.login("test@ripe.net", "password");
            fail();
        } catch (CrowdClientException ignored) {
            // expected
        }
    }

    @Test
    public void get_user_session_success() throws Exception {
        when(builder.get(CrowdSession.class)).thenReturn(new CrowdSession(new CrowdUser("test@ripe.net", true), null, "2033-01-30T16:38:27.369+11:00"));

        final UserSession session = subject.getUserSession("token");

        assertThat(session.getUsername(), is("test@ripe.net"));
        assertThat(session.isActive(), is(true));
    }

    @Test
    public void get_user_session_bad_request() {
        when(builder.get(CrowdSession.class)).thenThrow(new BadRequestException("Not valid sso"));

        try {
            subject.getUserSession("token");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access token: token"));
        }
    }

    @Test
    public void get_username_success() {
        when(builder.get(CrowdUser.class)).thenReturn(new CrowdUser("test@ripe.net", true));

        assertThat(subject.getUsername("uuid"), is("test@ripe.net"));
    }

    @Test
    public void get_username_not_found() {
        when(builder.get(CrowdUser.class)).thenThrow(new NotFoundException("message"));

        try {
            subject.getUsername("madeup-uuid");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access uuid: madeup-uuid"));
        }
    }

    @Test
    public void get_uuid_success() {
        when(builder.get(CrowdResponse.class)).thenReturn(
                new CrowdResponse(Lists.newArrayList(
                        new CrowdClient.CrowdAttribute(Lists.newArrayList(
                                new CrowdClient.CrowdValue("1-2-3-4")), "uuid"))));

        assertThat(subject.getUuid("test@ripe.net"), is("1-2-3-4"));
    }

    @Test
    public void get_uuid_not_found() {
        when(builder.get(CrowdResponse.class)).thenThrow(new NotFoundException("message"));

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
        when(crowdResponse.getUUID()).thenThrow(NoSuchElementException.class);
        when(builder.get(CrowdResponse.class)).thenReturn(crowdResponse);

        try {
            subject.getUuid("test@ripe.net");
            fail();
        } catch (CrowdClientException expected) {
            assertThat(expected.getMessage(), is("Cannot find UUID for: test@ripe.net"));
        }

    }
}
