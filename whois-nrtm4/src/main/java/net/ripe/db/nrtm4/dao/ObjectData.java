package net.ripe.db.nrtm4.dao;

import java.util.Objects;


public record ObjectData(int objectId, int sequenceId) implements Comparable<ObjectData> {

    @Override
    public int compareTo(final ObjectData o) {
        final int c1 = Integer.compare(objectId, o.objectId);
        if (c1 != 0) {
            // order by objectId
            return c1;
        }

        return (sequenceId == 0) ? 1 : (o.sequenceId == 0) ? -1 :
            Integer.compare(sequenceId, o.sequenceId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof final ObjectData that)) return false;
        return Objects.equals(objectId, that.objectId) &&
            Objects.equals(sequenceId, that.sequenceId);
    }

}
