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
import org.apache.commons.collections.KeyValue;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class BasicAuthPasswordCredentialValidator extends PasswordCredentialValidator {

    BasicAuthPasswordCredentialValidator(LoggerContext loggerContext) {
        super(loggerContext);
    }

    @Override
    public boolean hasValidCredential(PreparedUpdate update, UpdateContext updateContext, Collection<PasswordCredential> offeredCredentials, PasswordCredential knownCredential) {
        for (final PasswordCredential offeredCredential : offeredCredentials) {
            try {
                String offeredPassword = offeredCredential.getPassword();
                final KeyValue basicAuthMap = PasswordHelper.extractBasicAuthNameAndPassword(offeredPassword);
                final Set<CIString> mntnerKey = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);

                if (basicAuthMap == null || !mntnerKey.contains(CIString.ciString(basicAuthMap.getKey().toString()))){
                    continue;
                }
                if (hasValidPassword(update, knownCredential.getPassword(), basicAuthMap.getValue().toString())){
                    return true;
                }
            } catch (IllegalArgumentException e) {
                updateContext.addGlobalMessage(new Message(Messages.Type.WARNING, e.getMessage()));
            }
        }
        return false;
    }
}
