package net.ripe.db.whois.update.domain;

import java.util.Objects;

public abstract class AutoKey {
    private final String space;
    private final int index;
    private final String suffix;

    protected AutoKey(final String space, final int index, final String suffix) {
        this.space = space;
        this.index = index;
        this.suffix = suffix;
    }

    public String getSpace() {
        return space;
    }

    public int getIndex() {
        return index;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AutoKey other = (AutoKey) o;

        return Objects.equals(index, other.index) &&
                Objects.equals(space, other.space) &&
                Objects.equals(suffix, other.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(space, index, suffix);
    }
}
