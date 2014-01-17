package net.ripe.db.whois.common.sso;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrowdClientTest {

    @Mock Client client;
    @Mock WebTarget webTarget;
    @Mock Invocation.Builder builder;

    private CrowdClient subject;

    @Before
    public void setup() {
        subject = new CrowdClient("http://testurl", "crowduser", "crowdpassword");
        subject.setClient(client);
    }

    @Test
    public void getUserSession_success() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.get(String.class)).thenReturn("<session expand=\"user\"><user name=\"test@ripe.net\"><active>true</active></user></session>");

        final UserSession session = subject.getUserSession("token");

        assertThat(session.getUsername(), is("test@ripe.net"));
        assertThat(session.isActive(), is(true));
    }

    @Test
    public void getUserSession_failure() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.get(String.class)).thenReturn("<error><reason>REASON</reason><message>Wordy error message</message></error>");

        try {
            subject.getUserSession("token");
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), is("Wordy error message"));
        }
    }

    @Test
    public void getUsername_success() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.get(String.class)).thenReturn("<user expand=\"attributes\" name=\"test@ripe.net\"></user>");

        final String username = subject.getUsername("madeup-uuid");

        assertThat(username, is("test@ripe.net"));
    }

    @Test
    public void getUsername_failure() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.get(String.class)).thenThrow(new NotFoundException("message"));

        try {
            subject.getUsername("madeup-uuid");
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE Access uuid: madeup-uuid"));
        }
    }

    @Test
    public void getUuid_success() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.get(String.class)).thenReturn("<attributes><attribute name=\"uuid\"><values><value>madeup-uuid</value></values></attribute></attributes>");

        final String uuid = subject.getUuid("test@ripe.net");

        assertThat(uuid, is("madeup-uuid"));
    }

    @Test
    public void getUuid_failure() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.get(String.class)).thenThrow(new NotFoundException("message"));

        try {
            subject.getUuid("test@ripe.net");
            fail();
        } catch (Exception expected) {
            assertThat(expected.getMessage(), is("Unknown RIPE Access user: test@ripe.net"));
        }
    }
}
