package net.ripe.db.whois.internal.api.rnd.domain;

import java.util.List;

public class RpslObjectReference {
    private RpslObjectKey key;
    private List<Integer> revisions;


    public RpslObjectReference(RpslObjectKey key, List<Integer> revisions) {
        this.key = key;
        this.revisions = revisions;
    }

    public RpslObjectKey getKey() {
        return key;
    }

    public List<Integer> getRevisions() {
        return revisions;
    }

    public void addRevision(int revision) {
        revisions.add(revision);
    }

    public void clearRevisions() {
        revisions.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RpslObjectReference)) {
            return false;
        }

        return obj == this || ((RpslObjectReference) obj).key.toString().equals(this.key.toString());

    }

    @Override
    public int hashCode()
    {
        return key.toString().hashCode();
    }
}
