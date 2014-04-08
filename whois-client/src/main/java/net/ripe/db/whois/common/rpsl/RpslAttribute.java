package net.ripe.db.whois.common.rpsl;

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
    private String cleanComment;

    private int hash;
    private Set<CIString> cleanValues;

    public RpslAttribute(final AttributeType attributeType, final CIString value) {
        this(attributeType, value.toString());
    }

    public RpslAttribute(final AttributeType attributeType, final String value) {
        Validate.notNull(attributeType);
        Validate.notNull(value);
        this.key = attributeType.getName();
        this.value = value;
        this.type = attributeType;
    }

    public RpslAttribute(final String key, final CIString value) {
        this(key, value.toString());
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

    public String getCleanComment() {
        if (cleanValues == null) {
            extractCleanValueAndComment(value);
        }
        return cleanComment;
    }

    public CIString getCleanValue() {
        final Set<CIString> values = getCleanValues();
        switch (values.size()) {
            case 0:
                throw new IllegalStateException("No " + type + ": value found");
            case 1:
                return values.iterator().next();
            default:
                throw new IllegalStateException("Multiple " + type + ": values found");
        }
    }

    public Set<CIString> getCleanValues() {
        if (cleanValues == null) {
            extractCleanValueAndComment(value);
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

    private void extractCleanValueAndComment(final String value) {
        final StringBuilder cleanedValue = new StringBuilder(value.length());
        final StringBuilder commentValue = new StringBuilder(value.length());

        boolean comment = false;
        boolean space = false;
        boolean newline = false;
        boolean valueWritten = false;
        boolean commentWritten = false;

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
                continue;
            }

            if (c == ' ' || c == '\t' || c == '\r') {
                space = true;
                continue;
            }

            if (comment) {
                if (commentWritten) {
                    if (space) {
                        commentValue.append(' ');
                        space = false;
                    }
                } else {
                    commentWritten = true;
                    space = false;
                }
                commentValue.append(c);
                continue;
            }

            if (valueWritten) {
                if (space) {
                    cleanedValue.append(' ');
                    space = false;
                }
            } else {
                valueWritten = true;
                space = false;
            }

            cleanedValue.append(c);
        }

        this.cleanComment = commentWritten ? commentValue.toString() : null;

        if (type == null) {
            cleanValues = Collections.singleton(ciString(cleanedValue.toString()));
        } else {
            cleanValues = ciImmutableSet(type.splitValue(cleanedValue.toString()));
        }
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
            hash = 31 * key.hashCode() + getCleanValues().hashCode();
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
