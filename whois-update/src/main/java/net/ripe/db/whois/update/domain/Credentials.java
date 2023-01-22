package net.ripe.db.whois.update.domain;

import com.google.common.collect.Sets;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Immutable
public class Credentials {
    private final Set<Credential> credentials;

    public Credentials() {
        this.credentials = Collections.emptySet();
    }

    public Credentials(final Set<? extends Credential> credentials) {
        this.credentials = Collections.unmodifiableSet(credentials);
    }

    public Credentials add(final Collection<Credential> addedCredentials) {
        final Set<Credential> newCredentials = Sets.newLinkedHashSet(this.credentials);
        newCredentials.addAll(addedCredentials);
        return new Credentials(newCredentials);
    }

    public Set<Credential> all() {
        return credentials;
    }

    public <T extends Credential> T single(final Class<T> clazz) {
        final Set<T> result = ofType(clazz);
        switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.iterator().next();
            default:
                throw new IllegalArgumentException("More than 1 credentials of type: " + clazz);
        }
    }

    public <T extends Credential> Set<T> ofType(final Class<T> clazz) {
        final Set<T> result = Sets.newHashSet();

        for (final Credential credential : credentials) {
            if (clazz.isAssignableFrom(credential.getClass())) {
                result.add((T) credential);
            }
        }

        return result;
    }

    public boolean has(final Class<? extends Credential> clazz) {
        return !ofType(clazz).isEmpty();
    }
}
