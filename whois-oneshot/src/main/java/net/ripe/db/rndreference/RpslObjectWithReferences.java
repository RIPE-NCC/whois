package net.ripe.db.rndreference;

import javax.annotation.CheckForNull;
import java.util.Set;

class RpslObjectWithReferences {
    private final boolean isDeleted;
    private Set<String> referencing;
    private Set<String> referencedBy;


    public RpslObjectWithReferences(final boolean isDeleted, @CheckForNull final Set<String> referencing) {
        this.isDeleted = isDeleted;
        this.referencing = referencing;
    }


    public void setReferencing(final Set<String> referencing) {
        this.referencing = referencing;
    }

    public Set<String> getReferencing() {
        return referencing;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}
