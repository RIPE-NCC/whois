package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ripe.db.nrtm4.DeltaChange;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;

import java.util.List;


@JsonPropertyOrder({ "nrtm_version", "type", "source", "session_id", "version", "changes"})
public class PublishableDeltaFile extends PublishableNrtmDocument {

    private List<DeltaChange> changes;

    public PublishableDeltaFile(final NrtmVersionInfo version) {
        super(version);
    }

    public List<DeltaChange> getChanges() {
        return changes;
    }

    public void setChanges(final List<DeltaChange> changes) {
        this.changes = changes;
    }

}
