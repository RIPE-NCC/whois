package net.ripe.db.whois.internal.api.rnd.domain;

import net.ripe.db.whois.internal.api.rnd.InternalMessages;

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Set;

public class RpslObjectWithReferences {
    private final boolean isDeleted;
    private Set<RpslObjectReference> outgoingReferences;
    private int revision;

    public RpslObjectWithReferences(final boolean isDeleted, @CheckForNull final Set<RpslObjectReference> outgoingReferences, int revision) {
        this.isDeleted = isDeleted;
        this.outgoingReferences = outgoingReferences;
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