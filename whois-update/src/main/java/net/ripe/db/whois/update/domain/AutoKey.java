package net.ripe.db.whois.update.domain;

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
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AutoKey other = (AutoKey) o;
        return index == other.index && space.equals(other.space) && !(suffix != null ? !suffix.equals(other.suffix) : other.suffix != null);
    }

    @Override
    public int hashCode() {
        int result = space.hashCode();
        result = 31 * result + index;
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        return result;
    }
}