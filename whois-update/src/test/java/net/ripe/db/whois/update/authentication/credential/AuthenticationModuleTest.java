package net.ripe.db.whois.update.authentication.credential;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationModuleTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private PasswordCredentialValidator credentialValidator;
    @Mock private SsoCredentialValidator ssoCredentialValidator;
    @Mock private Credentials credentials;
    @Mock LoggerContext loggerContext;

    private AuthenticationModule subject;

    @Before
    public void setup() {
        when(credentialValidator.getSupportedCredentials()).thenReturn(PasswordCredential.class);
        when(ssoCredentialValidator.getSupportedCredentials()).thenReturn(SsoCredential.class);
        when(update.getCredentials()).thenReturn(credentials);

        subject = new AuthenticationModule(loggerContext, credentialValidator, ssoCredentialValidator);
    }

    @Test
    public void authenticate_finds_all_candidates() {
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
        when(credentialValidator.hasValidCredential(any(PreparedUpdate.class), any(UpdateContext.class), anyCollection(), any(PasswordCredential.class))).thenReturn(true);

        final RpslObject mntner = RpslObject.parse("mntner: TEST-MNT\nauth: Md5-pW something");
        final List<RpslObject> result = subject.authenticate(update, updateContext, Lists.newArrayList(mntner));

        assertThat(result, hasSize(1));
        assertThat(result.contains(mntner), is(true));
    }

    @Test
    public void authenticate_fails() {
        when(credentialValidator.hasValidCredential(any(PreparedUpdate.class), any(UpdateContext.class), anyCollection(), any(PasswordCredential.class))).thenReturn(false);

        final RpslObject mntner = RpslObject.parse("mntner: TEST-MNT\nauth: MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/");
        final List<RpslObject> result = subject.authenticate(update, updateContext, Lists.newArrayList(mntner));

        assertThat(result, hasSize(0));
    }

    @Test
    public void authenticate_sso_credential_checked_first() {
        when(credentialValidator.hasValidCredential(any(PreparedUpdate.class), any(UpdateContext.class), anyCollection(), any(PasswordCredential.class))).thenReturn(false);
        when(ssoCredentialValidator.hasValidCredential(any(PreparedUpdate.class), any(UpdateContext.class), anyCollection(), any(SsoCredential.class))).thenAnswer(
                new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {
                        verify(credentialValidator, never()).hasValidCredential(any(PreparedUpdate.class), any(UpdateContext.class), anyCollection(), any(PasswordCredential.class));
                        return false;
                    }
        });

        final RpslObject maintainer = RpslObject.parse(
                "mntner:        OWNER-MNT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:          SSO user1@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "referral-by:   OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST");

        subject.authenticate(update, updateContext, Collections.singletonList(maintainer));
    }
}
