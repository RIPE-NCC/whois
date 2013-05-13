package net.ripe.db.whois.update.authentication.credential;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationModuleTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private PasswordCredentialValidator credentialValidator;
    @Mock private Credentials credentials;

    private AuthenticationModule subject;

    @Before
    public void setup() {
        when(credentialValidator.getSupportedCredentials()).thenReturn(PasswordCredential.class);
        subject = new AuthenticationModule(credentialValidator);
    }

    @Test
    public void authenticate_finds_all_candidates() {
        when(update.getCredentials()).thenReturn(credentials);
        when(credentialValidator.hasValidCredential(any(PreparedUpdate.class), any(UpdateContext.class), anyCollection(), any(PasswordCredential.class))).thenReturn(true);

        final RpslObject mntner1 = RpslObject.parse("mntner: TEST-MNT\nauth: MD5-PWsomething");
        final RpslObject mntner2 = RpslObject.parse("mntner: TEST2-MNT\nauth: MD5-PWsomethingElse");
        final RpslObject mntner3 = RpslObject.parse("mntner: TEST3-MNT");

        final List<RpslObject> result = subject.authenticate(update, updateContext, Lists.newArrayList(mntner1, mntner2, mntner3));

        assertThat(result.size(), is(2));
        assertThat(result.contains(mntner1), is(true));
        assertThat(result.contains(mntner2), is(true));
        assertThat(result.contains(mntner3), is(false));
    }

    @Test
    public void authenticate_mixed_case_auth_line() {
        when(update.getCredentials()).thenReturn(credentials);
        when(credentialValidator.hasValidCredential(any(PreparedUpdate.class), any(UpdateContext.class), anyCollection(), any(PasswordCredential.class))).thenReturn(true);

        final RpslObject mntner = RpslObject.parse("mntner: TEST-MNT\nauth: Md5-pW something");
        final List<RpslObject> result = subject.authenticate(update, updateContext, Lists.newArrayList(mntner));

        assertThat(result.size(), is(1));
        assertThat(result.contains(mntner), is(true));
    }
}
