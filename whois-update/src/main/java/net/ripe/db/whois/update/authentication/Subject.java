package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@Immutable
public class Subject {
    private final Set<Principal> principals;
    private final Set<String> passedAuthentications;
    private final Set<String> failedAuthentications;
    private final Map<String, Collection<RpslObject>> pendingAuthentications;

    static Subject EMPTY = new Subject();

    Subject(final Principal... principals) {
        this(principals.length == 0 ? Collections.<Principal>emptySet() : Sets.newHashSet(principals),
                Collections.<String>emptySet(),
                Collections.<String>emptySet(),
                Collections.<String, Collection<RpslObject>>emptyMap());
    }

    Subject(final Set<Principal> principals, final Set<String> passedAuthentications, final Set<String> failedAuthentications, final Map<String, Collection<RpslObject>> pendingAuthentications) {
        this.principals = unmodifiableSet(principals);
        this.passedAuthentications = unmodifiableSet(passedAuthentications);
        this.failedAuthentications = unmodifiableSet(failedAuthentications);
        this.pendingAuthentications = pendingAuthentications;
    }

    public boolean hasPrincipal(final Principal principal) {
        return principals.contains(principal);
    }

    public Set<Principal> getPrincipals() {
        return principals;
    }

    public Set<String> getPassedAuthentications() {
        return passedAuthentications;
    }

    public Set<String> getFailedAuthentications() {
        return failedAuthentications;
    }

    public Set<String> getPendingAuthentications() {
        return unmodifiableSet(pendingAuthentications.keySet());
    }

    public Set<RpslObject> getPendingAuthenticationCandidates() {
        return Sets.newLinkedHashSet(Iterables.concat(pendingAuthentications.values()));
    }
}
