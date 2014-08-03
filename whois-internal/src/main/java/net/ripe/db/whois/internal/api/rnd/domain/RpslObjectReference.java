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
}
