package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SsoCredentialValidator implements CredentialValidator<SsoCredential>  {
    private final LoggerContext loggerContext;

    @Autowired
    public SsoCredentialValidator(LoggerContext loggerContext, SsoTranslator ssoTranslator) {
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<SsoCredential> getSupportedCredentials() {
        return SsoCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<SsoCredential> offeredCredentials, final SsoCredential knownCredential) {
        for (SsoCredential offered : offeredCredentials) {
            if (offered.getOfferedUserSession().isActive() && offered.getOfferedUserSession().getUuid().equals(knownCredential.getKnownUuid())){
                loggerContext.logString(
                        update.getUpdate(),
                        getClass().getCanonicalName(),
                        String.format("Validated %s with SSO Ripe Access for user: %s", update.getFormattedKey(), offered.getOfferedUserSession().getUsername()));
                return true;
            }
        }
        return false;
    }
}
