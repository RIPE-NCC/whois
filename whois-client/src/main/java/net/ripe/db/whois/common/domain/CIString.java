package net.ripe.db.whois.common.domain;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Set;

@Immutable
public final class CIString implements Comparable<CIString>, CharSequence {
    private final String value;
    private final String lcValue;

    @Nullable @Contract("null -> null;!null -> !null")
    public static CIString ciString(final String value) {
        if (value == null) {
            return null;
        }
        return new CIString(value);
    }

    public static Set<CIString> ciSet(final String... values) {
        final Set<CIString> result = Sets.newLinkedHashSetWithExpectedSize(values.length);
        for (final String value : values) {
            result.add(ciString(value));
        }
        return result;
    }

    public static Set<CIString> ciImmutableSet(final String... values) {
        return ciImmutableSet(Arrays.asList(values));
    }

    public static Set<CIString> ciSet(final Iterable<String> values) {
        final Set<CIString> result = Sets.newLinkedHashSet();
        for (final String value : values) {
            result.add(ciString(value));
        }
        return result;
    }

    public static Set<CIString> ciImmutableSet(final Iterable<String> values) {
        final ImmutableSet.Builder<CIString> builder = ImmutableSet.builder();
        for (final String value : values) {
            builder.add(ciString(value));
        }
        return builder.build();
    }

    public static boolean isBlank(CIString ciString) {
        return ciString == null || StringUtils.isBlank(ciString.value);
    }

    private CIString(final String value) {
        this.value = value;
        this.lcValue = value.toLowerCase();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof String) {
            return value.equalsIgnoreCase((String) o);
        }

        return getClass() == o.getClass() && lcValue.equals(((CIString) o).lcValue);
    }

    @Override
    public int hashCode() {
        return lcValue.hashCode();
    }

    @Override
    public int compareTo(@Nonnull final CIString o) {
        return lcValue.compareTo(o.lcValue);
    }

    @Override @Nonnull
    public String toString() {
        return value;
    }

    public String toLowerCase() {
        return lcValue;
    }

    public String toUpperCase() {
        return value.toUpperCase();
    }

    public int toInt() {
        return Integer.parseInt(value);
    }

    @Override
    public int length() {
        return value.length();
    }

    @Override
    public char charAt(final int index) {
        return value.charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return value.subSequence(start, end);
    }

    public boolean startsWith(final CIString value) {
        return lcValue.startsWith(value.lcValue);
    }

    public boolean startsWith(final String value) {
        return lcValue.startsWith(value.toLowerCase());
    }

    public boolean contains(final CIString value) {
        return lcValue.contains(value.lcValue);
    }

    public boolean contains(final String value) {
        return lcValue.contains(value.toLowerCase());
    }

    public boolean endsWith(final CIString value) {
        return lcValue.endsWith(value.lcValue);
    }

    public boolean endsWith(final String value) {
        return lcValue.endsWith(value.toLowerCase());
    }

    public CIString append(final CIString other) {
        return ciString(value + other.value);
    }
}
