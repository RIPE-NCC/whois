package net.ripe.db.whois.update.authentication.credential;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.credentials.Credential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.Collection;

interface CredentialValidator<T extends Credential, K extends Credential> {

    Class<K> getSupportedCredentials();

    Class<T> getSupportedOfferedCredentialType();

    boolean hasValidCredential(PreparedUpdate update, UpdateContext updateContext, Collection<T> offeredCredentials, K knownCredential, RpslObject maintainer);
}
