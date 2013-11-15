package net.ripe.db.whois.common.source;

import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public final class Source {
    public enum Type {MASTER, SLAVE}

    private final Type type;
    private final CIString name;

    public static Source slave(final String name) {
        return new Source(Type.SLAVE, ciString(name));
    }

    public static Source slave(final CIString name) {
        return new Source(Type.SLAVE, name);
    }

    public static Source master(final String name) {
        return new Source(Type.MASTER, ciString(name));
    }

    public static Source master(final CIString name) {
        return new Source(Type.MASTER, name);
    }

    private Source(final Type type, final CIString name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public CIString getName() {
        return name;
    }

    public boolean isGrs() {
        return name.toLowerCase().endsWith("-grs");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Source source = (Source) o;
        return name.equals(source.name) && type == source.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, type);
    }
}
