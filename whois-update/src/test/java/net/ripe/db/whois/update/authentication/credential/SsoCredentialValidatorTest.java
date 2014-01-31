package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class SsoCredentialValidatorTest {

    @Mock UpdateContext updateContext;
    @Mock PreparedUpdate update;
    @Mock LoggerContext loggerContext;

    private SsoCredentialValidator subject;

    @Before
    public void setup() {
        subject = new SsoCredentialValidator(loggerContext);
    }

    @Test
    public void supportedCredentials() {
        assertThat(subject.getSupportedCredentials(), Is.<Class<SsoCredential>>is(SsoCredential.class));
    }

    @Test
    public void hasValidCredential() {
        final UserSession offered = new UserSession("test@ripe.net", true);
        offered.setUuid("testuuid");

        final SsoCredential offeredCredential = (SsoCredential)SsoCredential.createOfferedCredential(offered);
        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                update,
                updateContext,
                Collections.singletonList(offeredCredential),
                knownCredential);

        assertThat(hasValidCredential, is(true));
    }

    @Test
    public void hasNoOfferedCredentials() {
        final UserSession offered = new UserSession("test@ripe.net", true);
        offered.setUuid("testuuid");

        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                update,
                updateContext,
                Collections.<SsoCredential>emptyList(),
                knownCredential);

        assertThat(hasValidCredential, is(false));
    }

    @Test
    public void hasInactiveUser() {
        final UserSession offered = new UserSession("test@ripe.net", false);
        offered.setUuid("testuuid");

        final SsoCredential offeredCredential = (SsoCredential)SsoCredential.createOfferedCredential(offered);
        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                update,
                updateContext,
                Collections.singletonList(offeredCredential),
                knownCredential);

        assertThat(hasValidCredential, is(false));
    }

    @Test
    public void noCorrectCredential() {
        final UserSession offered = new UserSession("test@ripe.net", false);
        offered.setUuid("offereduuid");

        final SsoCredential offeredCredential = (SsoCredential)SsoCredential.createOfferedCredential(offered);
        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                update,
                updateContext,
                Collections.singletonList(offeredCredential),
                knownCredential);

        assertThat(hasValidCredential, is(false));
    }
}
