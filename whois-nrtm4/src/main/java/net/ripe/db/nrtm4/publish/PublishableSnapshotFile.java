package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.annotation.JsonRawValue;
import net.ripe.db.nrtm4.persist.VersionInformation;

import static org.apache.commons.lang.StringUtils.isEmpty;


public class PublishableSnapshotFile extends PublishableNrtmDocument {

    private String objects;

    public PublishableSnapshotFile(
        final VersionInformation version
    ) {
        super(version);
    }

    @JsonRawValue
    public String getObjects() {
        return isEmpty(objects) ? "[]" : objects;
    }

    public void setObjectsString(final String objectStr) {
        objects = objectStr;
    }

}
