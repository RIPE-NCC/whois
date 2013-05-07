package net.ripe.db.whois.update.authentication;

import java.util.Collections;
import java.util.Set;

public class Subject {
    private final Set<Principal> principals;

    Subject(final Set<Principal> principals) {
        this.principals = Collections.unmodifiableSet(principals);
    }

    public boolean hasPrincipal(final Principal principal) {
        return principals.contains(principal);
    }
}
