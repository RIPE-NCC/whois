package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    private static final String BASIC_AUTH_NAME_PASSWORD_SEPARATOR = ":";
    @Override
    public boolean hasValidCredential(final PreparedUpdate update,
                                      final UpdateContext updateContext,
                                      final Collection<PasswordCredential> offeredCredentials,
                                      final PasswordCredential knownCredential) {

        for (final PasswordCredential offeredCredential : offeredCredentials) {
            try {
                String offeredPassword = offeredCredential.password();
                String knownPassword = knownCredential.password();
                if (offeredPassword.contains(BASIC_AUTH_NAME_PASSWORD_SEPARATOR)){
                    final String[] basicAuthCredentials = offeredPassword.split(BASIC_AUTH_NAME_PASSWORD_SEPARATOR, 2);
                    final Set<CIString> mntnerKey = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
                    if (!mntnerKey.contains(CIString.ciString(basicAuthCredentials[0]))){
                        return false;
                    }
                    offeredPassword = basicAuthCredentials[1];
                }

                if (PasswordHelper.authenticateMd5Passwords(knownPassword, offeredPassword)) {
                    loggerContext.logString(
                            update.getUpdate(),
                            getClass().getCanonicalName(),
                            String.format("Validated %s against known encrypted password: %s)", update.getFormattedKey(), knownPassword));

                    return true;
                }
            } catch (IllegalArgumentException e) {
                updateContext.addGlobalMessage(new Message(Messages.Type.WARNING, e.getMessage()));
            }
        }

        return false;
    }
}
