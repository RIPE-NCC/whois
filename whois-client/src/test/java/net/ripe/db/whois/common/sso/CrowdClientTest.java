package net.ripe.db.whois.common.sso;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import static net.ripe.db.whois.common.sso.CrowdClient.CrowdResponse;
import static net.ripe.db.whois.common.sso.CrowdClient.CrowdSession;
import static net.ripe.db.whois.common.sso.CrowdClient.CrowdUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrowdClientTest {

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
        subject.setClient(client);
    }

    @Test
    public void getUserSession_success() {
        when(builder.get(CrowdSession.class)).thenReturn(new CrowdSession(new CrowdUser("test@ripe.net", true), null));

        final UserSession session = subject.getUserSession("token");

        assertThat(session.getUsername(), is("test@ripe.net"));
        assertThat(session.isActive(), is(true));
    }

    @Test
    public void getUserSession_failure() {
        when(builder.get(CrowdSession.class)).thenThrow(new BadRequestException("Not valid sso"));

        try {
            subject.getUserSession("token");
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access token: token"));
        }
    }

    @Test
    public void getUsername_failure() {
        when(builder.get(CrowdUser.class)).thenThrow(new NotFoundException("message"));

        try {
            subject.getUsername("madeup-uuid");
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access uuid: madeup-uuid"));
        }
    }

    @Test
    public void getUuid_failure() {
        when(builder.get(CrowdResponse.class)).thenThrow(new NotFoundException("message"));

        try {
            subject.getUuid("test@ripe.net");
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE NCC Access user: test@ripe.net"));
        }
    }
}
