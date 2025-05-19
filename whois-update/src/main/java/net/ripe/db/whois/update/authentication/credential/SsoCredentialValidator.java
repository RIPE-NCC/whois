package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.common.credentials.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SsoCredentialValidator implements CredentialValidator<SsoCredential, SsoCredential> {
    private final LoggerContext loggerContext;

    @Autowired
    public SsoCredentialValidator(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<SsoCredential> getSupportedCredentials() {
        return SsoCredential.class;
    }

    @Override
    public Class<SsoCredential> getSupportedOfferedCredentialType() {
        return SsoCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<SsoCredential> offeredCredentials, final SsoCredential knownCredential, final RpslObject maintainer) {
        for (SsoCredential offered : offeredCredentials) {
            if (offered.getOfferedUserSession().getUuid().equals(knownCredential.getKnownUuid())) {
                log(update, String.format("Validated %s with RIPE NCC Access for user: %s.", update.getFormattedKey(), offered.getOfferedUserSession().getUsername()));

                update.getUpdate().setEffectiveCredential(offered.getOfferedUserSession().getUsername(), Update.EffectiveCredentialType.SSO);
                return true;
            }
        }
        return false;
    }

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
    }
}
