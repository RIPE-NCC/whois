package net.ripe.db.whois.common.sso;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SsoTokenTranslatorTest {

    @Mock
    CrowdClient crowdClient;
    private SsoTokenTranslator subject;

    @BeforeEach
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

    @Test
    public void translateSsoToken_invalid_session() {
        final String ssotoken = "ssotoken";

        when(crowdClient.getUserSession(ssotoken)).thenThrow(new CrowdClientException("Unknown RIPE NCC Access token: " + ssotoken));
        Assertions.assertThrows(CrowdClientException.class, () -> {
            subject.translateSsoToken(ssotoken);
        });
    }

    @Test
    public void translateSsoToken_invalid_username() {
        final String ssotoken = "ssotoken";
        final String username = "username";
        final String displayName = "Test User";

        when(crowdClient.getUserSession(ssotoken)).thenReturn(new UserSession(username, displayName, true, "2033-01-30T16:38:27.369+11:00"));
        when(crowdClient.getUuid(username)).thenThrow(new CrowdClientException("Unknown RIPE NCC Access user: " + username));

        Assertions.assertThrows(CrowdClientException.class, () -> {
            subject.translateSsoToken(ssotoken);
        });
    }
}
