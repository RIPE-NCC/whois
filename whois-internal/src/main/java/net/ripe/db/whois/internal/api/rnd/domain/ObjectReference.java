package net.ripe.db.whois.internal.api.rnd.domain;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ObjectReference {

    private final ObjectVersion from;
    private final ObjectVersion to;


    public ObjectReference(final ObjectVersion from, final ObjectVersion to) {
        this.from = from;
        this.to = to;
    }

    public ObjectVersion getFrom() {
        return from;
    }

    public ObjectVersion getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectReference)) return false;

        ObjectReference that = (ObjectReference) o;

        return ((that.from != null && that.from.equals(this.from)) &&
                (that.to != null && that.to.equals(this.to)));
    }

    @Override
    public int hashCode() {
        int result = (this.from != null ? this.from.hashCode() : 0);
        result = 31 * result + (this.to != null ? this.to.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ObjectReference{" +
                "from=" + this.from.toString() +
                ", to=" + this.to.toString() +
                "}";
    }
}

