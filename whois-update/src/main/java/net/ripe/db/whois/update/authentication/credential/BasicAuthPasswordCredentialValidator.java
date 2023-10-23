package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class BasicAuthPasswordCredentialValidator extends PasswordCredentialValidator {

    private static final String BASIC_AUTH_NAME_PASSWORD_SEPARATOR = ":";

    BasicAuthPasswordCredentialValidator(LoggerContext loggerContext) {
        super(loggerContext);
    }

    @Override
    public boolean hasValidCredential(PreparedUpdate update, UpdateContext updateContext, Collection<PasswordCredential> offeredCredentials, PasswordCredential knownCredential) {
        boolean hasValidCredential = false;
        for (final PasswordCredential offeredCredential : offeredCredentials) {
            try {
                String offeredPassword = offeredCredential.getPassword();
                if (offeredPassword.contains(BASIC_AUTH_NAME_PASSWORD_SEPARATOR)){
                    final String[] basicAuthCredentials = offeredPassword.split(BASIC_AUTH_NAME_PASSWORD_SEPARATOR, 2);
                    final Set<CIString> mntnerKey = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);
                    if (!mntnerKey.contains(CIString.ciString(basicAuthCredentials[0]))){
                        continue;
                    }
                    hasValidCredential = hasValidPassword(update, knownCredential.getPassword(), basicAuthCredentials[1]);
                }
            } catch (IllegalArgumentException e) {
                updateContext.addGlobalMessage(new Message(Messages.Type.WARNING, e.getMessage()));
            }
        }
        return hasValidCredential;
    }
}
