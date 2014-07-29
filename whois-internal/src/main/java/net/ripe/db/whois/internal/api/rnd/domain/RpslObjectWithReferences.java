package net.ripe.db.whois.internal.api.rnd.domain;

import javax.annotation.CheckForNull;
import java.util.Set;

public class RpslObjectWithReferences {
    private final boolean isDeleted;
    private Set<RpslObjectKey> outgoing;
    private Set<RpslObjectKey> incoming;


    public RpslObjectWithReferences(final boolean isDeleted, @CheckForNull final Set<RpslObjectKey> outgoing) {
        this.isDeleted = isDeleted;
        this.outgoing = outgoing;
    }


    public void setOutgoing(final Set<RpslObjectKey> outgoing) {
        this.outgoing = outgoing;
    }

    public Set<RpslObjectKey> getOutgoing() {
        return outgoing;
    }

    public Set<RpslObjectKey> getIncoming() {
        return incoming;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}