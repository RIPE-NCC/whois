package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ripe.db.nrtm4.DeltaChange;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;

import java.util.List;


@JsonPropertyOrder({ "nrtm_version", "type", "source", "session_id", "version", "changes"})
public class PublishableDeltaFile extends PublishableNrtmDocument {

    private List<DeltaChange> changes;
    @JsonIgnore // Not published in delta file
    private String fileName;
    @JsonIgnore // Not published in delta file
    private String sha256hex;

    public PublishableDeltaFile(final NrtmVersionInfo nextVersion, final List<DeltaChange> deltas) {
        super(nextVersion);
        this.changes = deltas;
    }

    public List<DeltaChange> getChanges() {
        return changes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getSha256hex() {
        return sha256hex;
    }

    public void setSha256hex(final String sha256hex) {
        this.sha256hex = sha256hex;
    }

}
