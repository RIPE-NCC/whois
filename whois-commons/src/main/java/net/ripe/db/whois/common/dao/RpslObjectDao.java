package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.collect.ProxyLoader;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

// these should return Collection<> instead of List<> to allow for greater flexibility in implementation
public interface RpslObjectDao extends ProxyLoader<Identifiable, RpslObject> {
    RpslObject getById(int objectId);

<<<<<<< HEAD
    LocalDateTime getLastUpdated(int objectId);
=======
    RpslObject getByKey(ObjectType type, CIString key);
>>>>>>> master

    RpslObject getByKey(ObjectType type, String searchKey);

    @Nullable
    RpslObject getByKeyOrNull(ObjectType type, CIString key);

    @Nullable
    RpslObject getByKeyOrNull(ObjectType type, String searchKey);

    List<RpslObject> getByKeys(ObjectType type, Collection<CIString> searchKeys);

    RpslObject findAsBlock(long begin, long end);

    List<RpslObject> findAsBlockIntersections(long begin, long end);

    RpslObjectInfo findByKey(ObjectType type, String searchKey);

    RpslObjectInfo findByKey(ObjectType type, CIString searchKey);

    @Nullable
    RpslObjectInfo findByKeyOrNull(ObjectType type, String searchKey);

    @Nullable
    RpslObjectInfo findByKeyOrNull(ObjectType type, CIString searchKey);

    List<RpslObjectInfo> findByAttribute(AttributeType attributeType, String attributeValue);

    List<RpslObjectInfo> findMemberOfByObjectTypeWithoutMbrsByRef(ObjectType objectType, String attributeValue);

    Collection<RpslObjectInfo> relatedTo(RpslObject identifiable, Set<ObjectType> excludeObjectTypes);
}
