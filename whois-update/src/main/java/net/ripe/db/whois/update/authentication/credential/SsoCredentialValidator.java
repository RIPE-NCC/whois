package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SsoCredentialValidator implements CredentialValidator<SsoCredential> {
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
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<SsoCredential> offeredCredentials, final SsoCredential knownCredential) {
        for (SsoCredential offered : offeredCredentials) {
            if (offered.getOfferedUserSession().getUuid().equals(knownCredential.getKnownUuid())) {
                log(update, String.format("Validated %s with RIPE NCC Access for user: %s.", update.getFormattedKey(), offered.getOfferedUserSession().getUsername()));
                return true;
            }
        }
        return false;
    }

    private void log(final PreparedUpdate update, final String message) {
        loggerContext.logString(update.getUpdate(), getClass().getCanonicalName(), message);
    }
}
