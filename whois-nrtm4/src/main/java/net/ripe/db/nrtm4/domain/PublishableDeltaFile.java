package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;


@JsonPropertyOrder({ "nrtm_version", "type", "source", "session_id", "version", "changes"})
public class PublishableDeltaFile extends PublishableNrtmFile {

    private List<DeltaChange> changes;

    public PublishableDeltaFile(final NrtmVersionInfo nextVersion, final List<DeltaChange> deltas) {
        super(nextVersion);
        this.changes = deltas;
    }

    public List<DeltaChange> getChanges() {
        return changes;
    }

}
