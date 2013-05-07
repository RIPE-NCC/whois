package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.CheckForNull;
import java.util.Map;
import java.util.Set;

public interface RpslObjectUpdateDao {

    RpslObjectUpdateInfo lookupObject(ObjectType type, String pkey);

    RpslObjectUpdateInfo deleteObject(int objectId, String pkey);

    RpslObjectUpdateInfo updateObject(int objectId, RpslObject object);

    RpslObjectUpdateInfo createObject(RpslObject object);

    boolean isReferenced(RpslObject object);

    Set<RpslObjectInfo> getReferences(RpslObject object);

    Map<RpslAttribute, Set<String>> getInvalidReferences(RpslObject object);

    @CheckForNull
    RpslObjectInfo getAttributeReference(AttributeType attributeType, CIString keyValue);
}
