package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.override.OverrideCredentialValidator;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticatorPrincipalPowerMaintainerTest {
    @Mock
    private IpRanges ipRanges;

    @Mock
    private Origin origin;

    @Mock
    private PreparedUpdate update;

    @Mock
    private UpdateContext updateContext;

    @Mock
    private AuthenticationStrategy authenticationStrategy1;

    @Mock
    private AuthenticationStrategy authenticationStrategy2;

    @Mock
    private Maintainers maintainers;

    @Mock
    private LoggerContext loggerContext;

    @Mock
    private OverrideCredentialValidator overrideCredentialValidator;

    Authenticator subject;
    ArgumentCaptor<Subject> subjectCapture;

    @BeforeEach
    public void setup() {
        when(authenticationStrategy1.getName()).thenReturn("authenticationStrategy1");
        when(authenticationStrategy2.compareTo(authenticationStrategy1)).thenReturn(1);

        when(maintainers.getEnduserMaintainers()).thenReturn(ciSet("RIPE-NCC-END-MNT"));
        when(maintainers.getAllocMaintainers()).thenReturn(ciSet("RIPE-NCC-HM-MNT", "AARDVARK-MNT"));
        when(update.getCredentials()).thenReturn(new Credentials());

        subjectCapture = ArgumentCaptor.forClass(Subject.class);
        subject = new Authenticator(ipRanges, maintainers, loggerContext, new AuthenticationStrategy[]{authenticationStrategy1, authenticationStrategy2}, overrideCredentialValidator);
    }


    @Test
    public void authenticate_by_powerMaintainer_outside_ripe_not_allowed_when_deployed() {
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(RpslObject.parse("mntner: RIPE-NCC-HM-MNT")));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.ripeMntnerUpdatesOnlyAllowedFromWithinNetwork());
    }

    @Test
    public void authenticate_by_powerMaintainer_by_email_not_allowed_when_deployed() {
        when(origin.allowAdminOperations()).thenReturn(false);
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(RpslObject.parse("mntner: RIPE-NCC-HM-MNT")));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.ripeMntnerUpdatesOnlyAllowedFromWithinNetwork());
    }

}
