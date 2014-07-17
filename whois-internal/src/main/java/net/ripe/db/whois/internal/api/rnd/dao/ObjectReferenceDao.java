package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;

import java.util.List;

public interface ObjectReferenceDao {

    List<ObjectReference> getReferencing(final ObjectType fromObjectType, final String fromPkey, final long timestamp);
    List<ObjectReference> getReferencedBy(final ObjectType toObjectType, final String toPkey, final long timestamp);

}
