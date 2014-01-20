package net.ripe.db.whois.common.sso;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SsoTokenTranslatorTest {

    @Mock CrowdClient crowdClient;
    private SsoTokenTranslator subject;

    @Before
    public void setup() {
        subject = new SsoTokenTranslator(crowdClient);
    }

    @Test
    public void translateSsoToken_invalid_session() {
        final String ssotoken = "ssotoken";

        when(crowdClient.getUserSession(ssotoken)).thenThrow(new IllegalArgumentException("not found"));

        final UserSession userSession = subject.translateSsoToken(ssotoken);

        assertThat(userSession, is(nullValue()));
    }

    @Test
    public void translateSsoToken_invalid_username() {
        final String ssotoken = "ssotoken";
        final String username = "username";

        when(crowdClient.getUserSession(ssotoken)).thenReturn(new UserSession(username, true));
        when(crowdClient.getUuid(username)).thenThrow(new IllegalArgumentException("not found"));

        final UserSession userSession = subject.translateSsoToken(ssotoken);

        assertThat(userSession, is(nullValue()));
    }

    @Test
    public void translateSsoToken_happypath() {
        final String ssotoken = "ssotoken";
        final String username = "username";
        final String uuid = "uuid";

        when(crowdClient.getUserSession(ssotoken)).thenReturn(new UserSession(username, true));
        when(crowdClient.getUuid(username)).thenReturn(uuid);

        final UserSession userSession = subject.translateSsoToken(ssotoken);

        assertThat(userSession.getUsername(), is(username));
        assertThat(userSession.getUuid(), is(uuid));
        assertThat(userSession.isActive(), is(true));
    }
}
