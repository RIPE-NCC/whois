package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class RpslObjectUpdateInfo extends RpslObjectInfo {
    final int sequenceId;

    public RpslObjectUpdateInfo(final int objectId, final int sequenceId, final ObjectType objectType, final String key) {
        super(objectId, objectType, key);
        this.sequenceId = sequenceId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("objectId", getObjectId())
                .append("objectType", getObjectType())
                .append("key", getKey())
                .append("sequenceId", sequenceId)
                .toString();
    }
}
