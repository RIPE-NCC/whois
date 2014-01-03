package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SSOCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
//TODO [AS] This is a placeholder. Expand when requirements arrive.
public class SSOCredentialValidator implements CredentialValidator<SSOCredential>  {
    @Override
    public Class<SSOCredential> getSupportedCredentials() {
        return SSOCredential.class;
    }

    @Override
    public boolean hasValidCredential(final PreparedUpdate update, final UpdateContext updateContext, final Collection<SSOCredential> offeredCredentials, final SSOCredential knownCredential) {
        return false;
    }
}
