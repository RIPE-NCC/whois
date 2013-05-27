package net.ripe.db.whois.common.domain;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import javax.annotation.concurrent.Immutable;
import java.util.Set;

@Immutable
public class CIString implements Comparable<CIString>, CharSequence {
    private final String value;
    private final String lcValue;

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

    public static Set<CIString> ciSet(final Iterable<String> values) {
        return Sets.newHashSet(Iterables.transform(values, new Function<String, CIString>() {
            @Override
            public CIString apply(final String input) {
                return CIString.ciString(input);
            }
        }));
    }

    private CIString(final String value) {
        this.value = value;
        this.lcValue = value.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && lcValue.equals(((CIString) o).lcValue);
    }

    @Override
    public int hashCode() {
        return lcValue.hashCode();
    }

    @Override
    public int compareTo(final CIString o) {
        return lcValue.compareTo(o.lcValue);
    }

    @Override
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
    public char charAt(int index) {
        return value.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return value.subSequence(start, end);
    }

    public boolean startsWith(final CIString value) {
        return lcValue.startsWith(value.lcValue);
    }

    public boolean contains(final CIString value) {
        return lcValue.contains(value.lcValue);
    }

    public boolean endsWith(final CIString value) {
        return lcValue.endsWith(value.lcValue);
    }

    public CIString append(final CIString other) {
        return ciString(value + other.value);
    }
}
