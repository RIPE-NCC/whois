package net.ripe.db.rndreference;

import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectReference;

import javax.annotation.CheckForNull;
import java.util.HashSet;
import java.util.Set;

public class RevisionWithReferences {
    private final boolean isDeleted;
    private Set<RpslObjectReference> outgoingReferences;
    private int revision;

    public RevisionWithReferences(final boolean isDeleted, @CheckForNull final Set<RpslObjectReference> outgoingReferences, int revision) {
        this.isDeleted = isDeleted;
        if (outgoingReferences != null) {
            this.outgoingReferences = outgoingReferences;
        } else {
            this.outgoingReferences = new HashSet<>();
        }
        this.revision = revision;
    }


    public void setOutgoingReferences(final Set<RpslObjectReference> outgoingReferences) {
        this.outgoingReferences = outgoingReferences;
    }

    public Set<RpslObjectReference> getOutgoingReferences() {
        return outgoingReferences;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public int getRevision() {
        return revision;
    }

}