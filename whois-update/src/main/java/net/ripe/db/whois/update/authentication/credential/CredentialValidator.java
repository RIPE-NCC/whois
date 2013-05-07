package net.ripe.db.whois.update.authentication.credential;

import java.util.Collection;

import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

interface CredentialValidator<T extends Credential> {
    Class<T> getSupportedCredentials();

    boolean hasValidCredential(PreparedUpdate update, UpdateContext updateContext, Collection<T> offeredCredentials, T knownCredential);
}
