package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SsoCredentialValidatorTest {

    @Mock UpdateContext updateContext;
    @Mock PreparedUpdate preparedUpdate;
    @Mock LoggerContext loggerContext;

    private SsoCredentialValidator subject;
    private final Update update = getUpdate();

    @Before
    public void setup() {
        subject = new SsoCredentialValidator(loggerContext);
        when(preparedUpdate.getUpdate()).thenReturn(update);
    }

    @Test
    public void supportedCredentials() {
        assertThat(subject.getSupportedCredentials(), Is.<Class<SsoCredential>>is(SsoCredential.class));
    }

    @Test
    public void hasValidCredential() {
        final UserSession offered = new UserSession("test@ripe.net", "Test User", true, "2033-01-30T16:38:27.369+11:00");
        offered.setUuid("testuuid");

        final SsoCredential offeredCredential = (SsoCredential)SsoCredential.createOfferedCredential(offered);
        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                preparedUpdate,
                updateContext,
                Collections.singletonList(offeredCredential),
                knownCredential);

        assertThat(hasValidCredential, is(true));
    }

    @Test
    public void setsEffectiveCredential() {
        final UserSession offered = new UserSession("test@ripe.net", "Test User", true, "2033-01-30T16:38:27.369+11:00");
        offered.setUuid("testuuid");

        final SsoCredential offeredCredential = (SsoCredential)SsoCredential.createOfferedCredential(offered);
        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        subject.hasValidCredential(
                preparedUpdate,
                updateContext,
                Collections.singletonList(offeredCredential),
                knownCredential);

        assertThat(update.getEffectiveCredential(), is("test@ripe.net" ));
        assertThat(update.getEffectiveCredentialType(), is(Update.EffectiveCredentialType.SSO));

    }


    @Test
    public void hasNoOfferedCredentials() {
        final UserSession offered = new UserSession("test@ripe.net", "Test User", true, "2033-01-30T16:38:27.369+11:00");
        offered.setUuid("testuuid");

        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                preparedUpdate,
                updateContext,
                Collections.<SsoCredential>emptyList(),
                knownCredential);

        assertThat(hasValidCredential, is(false));
    }

    @Test
    public void noCheckForUserInactivity() {
        final UserSession offered = new UserSession("test@ripe.net", "Test User", false, "2033-01-30T16:38:27.369+11:00");
        offered.setUuid("testuuid");

        final SsoCredential offeredCredential = (SsoCredential)SsoCredential.createOfferedCredential(offered);
        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                preparedUpdate,
                updateContext,
                Collections.singletonList(offeredCredential),
                knownCredential);

        assertThat(hasValidCredential, is(true));
    }

    @Test
    public void noCorrectCredential() {
        final UserSession offered = new UserSession("test@ripe.net", "Test User", false, "2033-01-30T16:38:27.369+11:00");
        offered.setUuid("offereduuid");

        final SsoCredential offeredCredential = (SsoCredential)SsoCredential.createOfferedCredential(offered);
        final SsoCredential knownCredential = SsoCredential.createKnownCredential("testuuid");

        final boolean hasValidCredential = subject.hasValidCredential(
                preparedUpdate,
                updateContext,
                Collections.singletonList(offeredCredential),
                knownCredential);

        assertThat(hasValidCredential, is(false));
    }

    private Update getUpdate() {
        final Paragraph paragraph = new Paragraph(" ");
        final RpslObject submittedObject = new RpslObject(Arrays.asList(new RpslAttribute(AttributeType.ORGANISATION, CIString.ciString("org-1"))));

        return new Update(paragraph, Operation.DELETE, Arrays.asList(" "), submittedObject);
    }
}
