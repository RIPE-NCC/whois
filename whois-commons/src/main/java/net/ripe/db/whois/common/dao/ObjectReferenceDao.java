package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;

import java.util.List;

public interface ObjectReferenceDao {

    List<net.ripe.db.whois.common.domain.ObjectReference> getReferencing(final ObjectType fromObjectType, final String fromPkey, final long timestamp);
    List<net.ripe.db.whois.common.domain.ObjectReference> getReferenced(final ObjectType toObjectType, final String toPkey, final long timestamp);

}
