package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.TagResponseObject;

import java.util.List;

public class TaggedRpslObject {
    public RpslObject rpslObject;
    public List<TagResponseObject> tagResponseObjects;

    public TaggedRpslObject(RpslObject o, List<TagResponseObject> tags) {
        rpslObject = o;
        tagResponseObjects = tags;
    }
}
