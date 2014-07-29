package net.ripe.db.whois.internal.api.rnd.domain;

import javax.annotation.CheckForNull;
import java.util.Set;

public class RpslObjectWithReferences {
    private final boolean isDeleted;
    private Set<RpslObjectKey> referencing;
    private Set<RpslObjectKey> referencedBy;


    public RpslObjectWithReferences(final boolean isDeleted, @CheckForNull final Set<RpslObjectKey> referencing) {
        this.isDeleted = isDeleted;
        this.referencing = referencing;
    }


    public void setReferencing(final Set<RpslObjectKey> referencing) {
        this.referencing = referencing;
    }

    public Set<RpslObjectKey> getReferencing() {
        return referencing;
    }

    public Set<RpslObjectKey> getReferencedBy() {
        return referencedBy;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}