package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.attrs.MntRoutes;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciImmutableSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public final class RpslAttribute {
    private static final int LEADING_CHARS = 16;
    private static final int LEADING_CHARS_SHORTHAND = 5;

    private final AttributeType type;
    private final String key;
    private final String value;     // non-clean, contains EOL comments too

    private int hash;
    private Set<CIString> cleanValues;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        // Constructor is only used by RpslObject, which behaves, so no need to copy arrays
    RpslAttribute(final byte[] key, final byte[] value) {
        Validate.notNull(key);
        Validate.notNull(value);

        this.key = new String(key, Charsets.ISO_8859_1).toLowerCase();
        this.value = new String(value, Charsets.ISO_8859_1);
        this.type = AttributeType.getByNameOrNull(this.key);
    }

    public RpslAttribute(final AttributeType attributeType, final String value) {
        this(attributeType.getName(), value);
    }

    public RpslAttribute(final String key, final String value) {
        Validate.notNull(key);
        Validate.notNull(value);
        this.key = key.toLowerCase();
        this.value = value;
        this.type = AttributeType.getByNameOrNull(this.key);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public CIString getCleanValue() {
        final Set<CIString> values = getCleanValues();
        switch (values.size()) {
            case 0:
                throw new IllegalStateException("No value found");
            case 1:
                return values.iterator().next();
            default:
                throw new IllegalStateException("Multiple clean values found: " + values);
        }
    }

    public Set<CIString> getCleanValues() {
        if (cleanValues == null) {
            final String cleanedValue = determineCleanValue(value);

            if (type == null) {
                cleanValues = Collections.singleton(ciString(cleanedValue));
            } else {
                cleanValues = ciImmutableSet(type.splitValue(cleanedValue));
            }
        }

        return cleanValues;
    }

    /**
     * Get all reference values.
     * <p/>
     * A reference value is the cleaned value. If part of a clean value references another object,
     * the rest of that value is discarded and only the reference is retained.
     */
    public Set<CIString> getReferenceValues() {
        if (AttributeType.MNT_ROUTES.equals(type)) {
            final Set<CIString> values = getCleanValues();
            final Set<CIString> result = Sets.newLinkedHashSetWithExpectedSize(values.size());
            for (final CIString value : values) {
                result.add(MntRoutes.parse(value).getMaintainer());
            }

            return result;
        } else {
            return getCleanValues();
        }
    }

    /**
     * Get the reference value.
     * <p/>
     * Reference values are explaned in the {@link #getReferenceValues() getReferenceValues} method.
     *
     * @throws IllegalStateException If there is not exactly one reference value.
     */
    public CIString getReferenceValue() {
        final Set<CIString> values = getReferenceValues();
        switch (values.size()) {
            case 0:
                throw new IllegalStateException("No value found");
            case 1:
                return values.iterator().next();
            default:
                throw new IllegalStateException("Multiple reference values found: " + values);
        }
    }

    private static String determineCleanValue(final String value) {
        final StringBuilder result = new StringBuilder(value.length());

        boolean comment = false;
        boolean space = false;
        boolean newline = false;
        boolean written = false;

        for (final char c : value.toCharArray()) {
            if (c == '\n') {
                newline = true;
                space = true;
                comment = false;
                continue;
            }

            if (newline) {
                newline = false;
                if (c == '+') {
                    continue;
                }
            }

            if (c == '#') {
                comment = true;
            }

            if (comment) {
                continue;
            }

            if (c == ' ' || c == '\t' || c == '\r') {
                space = true;
                continue;
            }

            if (written) {
                if (space) {
                    result.append(' ');
                    space = false;
                }
            } else {
                written = true;
                space = false;
            }

            result.append(c);
        }

        return result.toString();
    }

    public void validateSyntax(final ObjectType objectType, final ObjectMessages objectMessages) {
        for (final CIString cleanValue : getCleanValues()) {
            if (!type.getSyntax().matches(objectType, cleanValue.toString())) {
                objectMessages.addMessage(this, ValidationMessages.syntaxError(cleanValue.toString()));
            }
        }
    }

    public void writeTo(final Writer writer) throws IOException {
        writer.write(key);
        writer.write(':');

        final int column = key.startsWith("*") ? LEADING_CHARS_SHORTHAND : LEADING_CHARS;
        final char[] chars = value.toCharArray();

        int leadColumn = key.length() + 1;
        int spaces = 0;

        for (final char c : chars) {
            if (leadColumn == 0 && spaces == 0 && c == '+') {
                writer.write(c);
                leadColumn++;
            } else if (c == ' ' || c == '\t' || c == '\r') {
                spaces++;
            } else if (c == '\n') {
                leadColumn = 0;
                spaces = 0;
                writer.write(c);
            } else {
                if (leadColumn < column) {
                    spaces = column - leadColumn;
                    leadColumn = column;
                }

                while (spaces > 0) {
                    writer.write(' ');
                    spaces--;
                }

                writer.write(c);
            }
        }

        writer.write('\n');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RpslAttribute attribute = (RpslAttribute) o;

        if (type == null) {
            if (attribute.type != null) {
                return false;
            }
            return key.equals(attribute.key);
        } else {
            if (type != attribute.type) {
                return false;
            }
            return Iterables.elementsEqual(getCleanValues(), attribute.getCleanValues());
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = 31*key.hashCode() + getCleanValues().hashCode();
        }
        return hash;
    }

    @CheckForNull
    public AttributeType getType() {
        return type;
    }

    @Override
    public String toString() {
        try {
            final StringWriter writer = new StringWriter();
            writeTo(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Should never occur", e);
        }
    }
}
