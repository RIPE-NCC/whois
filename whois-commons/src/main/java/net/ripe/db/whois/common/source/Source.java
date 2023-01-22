package net.ripe.db.whois.common.source;

import net.ripe.db.whois.common.domain.CIString;

import jakarta.annotation.concurrent.Immutable;

import java.util.Objects;

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
        return name.endsWith("-grs");
    }

    public boolean isTest() {
        return name.equals("TEST");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Source source = (Source) o;

        return Objects.equals(name, source.name) &&
                Objects.equals(type, source.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, type);
    }
}
