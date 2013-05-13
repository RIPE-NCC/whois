package net.ripe.db.whois.common.domain;

import org.joda.time.LocalDateTime;

public class BlockEvent {
    public enum Type {
        BLOCK_TEMPORARY,
        BLOCK_PERMANENTLY,
        UNBLOCK
    }

    private final LocalDateTime time;
    private final int limit;
    private final Type type;

    public BlockEvent(final LocalDateTime time, final int limit, final Type type) {
        this.time = time;
        this.limit = limit;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BlockEvent that = (BlockEvent) o;
        return time.equals(that.time) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public int getLimit() {
        return limit;
    }

    public Type getType() {
        return type;
    }
}
