package net.ripe.db.nrtm4.publish;

import com.google.common.collect.ImmutableList;
import net.ripe.db.nrtm4.persist.NrtmSource;

import java.util.List;


public class Snapshot extends PublishableNrtmDocument {

    private final ImmutableList<NrtmObject> objects;

    public Snapshot(
            final NrtmDocumentType type,
            final NrtmSource source,
            final String sessionID,
            final int version,
            final ImmutableList<NrtmObject> objects
    ) {
        super(type, source, sessionID, version);
        this.objects = objects;
    }

    public List<NrtmObject> getObjects() {
        return objects;
    }

}
