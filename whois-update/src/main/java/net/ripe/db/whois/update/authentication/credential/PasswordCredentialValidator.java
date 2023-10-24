package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
class PasswordCredentialValidator implements CredentialValidator<PasswordCredential, PasswordCredential> {
    private final LoggerContext loggerContext;

    @Autowired
    PasswordCredentialValidator(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public Class<PasswordCredential> getSupportedCredentials() {
        return PasswordCredential.class;
    }

    @Override
    public Class<PasswordCredential> getSupportedOfferedCredentialType() {
        return PasswordCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update,
                                      final UpdateContext updateContext,
                                      final Collection<PasswordCredential> offeredCredentials,
                                      final PasswordCredential knownCredential) {
        for (final PasswordCredential offeredCredential : offeredCredentials) {
            try {
                if (hasValidPassword(update, knownCredential.getPassword(), offeredCredential.getPassword())){
                    return true;
                }
            } catch (IllegalArgumentException e) {
                updateContext.addGlobalMessage(new Message(Messages.Type.WARNING, e.getMessage()));
            }
        }
        return false;
    }

    protected boolean hasValidPassword(final PreparedUpdate update, final String knownPassword, final String offeredPassword){
        if (PasswordHelper.authenticateMd5Passwords(knownPassword, offeredPassword)) {
            loggerContext.logString(
                    update.getUpdate(),
                    getClass().getCanonicalName(),
                    String.format("Validated %s against known encrypted password: %s)", update.getFormattedKey(), knownPassword));

            return true;
        }
        return false;
    }
}
