package net.ripe.db.nrtm4.publish;

import net.ripe.db.nrtm4.DeltaChange;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;

import java.util.List;


public class PublishableDeltaFile extends PublishableNrtmDocument {

    private List<DeltaChange> changes;

    PublishableDeltaFile(final NrtmVersionInfo version) {
        super(version);
    }

    public List<DeltaChange> getChanges() {
        return changes;
    }

    public void setChanges(final List<DeltaChange> changes) {
        this.changes = changes;
    }

}