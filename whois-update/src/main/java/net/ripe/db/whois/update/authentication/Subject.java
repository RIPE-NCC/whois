package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Sets;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@Immutable
public class Subject {
    private final Set<Principal> principals;
    private final Set<String> passedAuthentications;
    private final Set<String> failedAuthentications;

    static Subject EMPTY = new Subject();

    Subject(final Principal... principals) {
        this(principals.length == 0 ? Collections.<Principal>emptySet() : Sets.newHashSet(principals),
                Collections.<String>emptySet(),
                Collections.<String>emptySet());
    }

    Subject(final Set<Principal> principals, final Set<String> passedAuthentications, final Set<String> failedAuthentications) {
        this.principals = unmodifiableSet(principals);
        this.passedAuthentications = unmodifiableSet(passedAuthentications);
        this.failedAuthentications = unmodifiableSet(failedAuthentications);
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
}
