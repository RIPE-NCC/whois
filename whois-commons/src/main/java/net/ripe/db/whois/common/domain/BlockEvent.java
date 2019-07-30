package net.ripe.db.whois.common.domain;

import java.time.LocalDateTime;

import java.util.Objects;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BlockEvent that = (BlockEvent) o;

        return Objects.equals(time, that.time) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
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
