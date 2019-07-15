package net.ripe.db.whois.common.sso;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SsoTokenTranslatorTest {

    @Mock
    CrowdClient crowdClient;
    private SsoTokenTranslator subject;

    @Before
    public void setup() {
        subject = new SsoTokenTranslator(crowdClient);
    }

    @Test
    public void translateSsoToken() {
        final String ssotoken = "ssotoken";
        final String username = "username";
        final String displayName = "Test User";
        final String uuid = "uuid";

        when(crowdClient.getUserSession(ssotoken)).thenReturn(new UserSession(username, displayName, true, "2033-01-30T16:38:27.369+11:00"));
        when(crowdClient.getUuid(username)).thenReturn(uuid);

        final UserSession userSession = subject.translateSsoToken(ssotoken);

        assertThat(userSession.getUsername(), is(username));
        assertThat(userSession.getUuid(), is(uuid));
        assertThat(userSession.isActive(), is(true));
    }

    @Test(expected = CrowdClientException.class)
    public void translateSsoToken_invalid_session() {
        final String ssotoken = "ssotoken";

        when(crowdClient.getUserSession(ssotoken)).thenThrow(new CrowdClientException("Unknown RIPE NCC Access token: " + ssotoken));

        subject.translateSsoToken(ssotoken);
    }

    @Test(expected = CrowdClientException.class)
    public void translateSsoToken_invalid_username() {
        final String ssotoken = "ssotoken";
        final String username = "username";
        final String displayName = "Test User";

        when(crowdClient.getUserSession(ssotoken)).thenReturn(new UserSession(username, displayName, true, "2033-01-30T16:38:27.369+11:00"));
        when(crowdClient.getUuid(username)).thenThrow(new CrowdClientException("Unknown RIPE NCC Access user: " + username));

        subject.translateSsoToken(ssotoken);
    }
}
