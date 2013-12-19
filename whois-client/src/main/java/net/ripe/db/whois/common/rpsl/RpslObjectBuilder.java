package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RpslObjectBuilder {
    private RpslObject original;
    private final List<RpslAttribute> attributes;

    public RpslObjectBuilder() {
        this.attributes = Lists.newArrayList();
    }

    public RpslObjectBuilder(List<RpslAttribute> attributes) {
        this.attributes = attributes;
    }

    public RpslObjectBuilder(RpslObject rpslObject) {
        this.original = rpslObject;
        this.attributes = Lists.newArrayList(rpslObject.getAttributes());
    }

    public RpslObjectBuilder(String input) {
        this(getAttributes(input));
    }

    public RpslObjectBuilder(byte[] input) {
        this(getAttributes(input));
    }

    public RpslObject get() {
        return original == null ? new RpslObject(attributes) : new RpslObject(original, attributes);
    }

    public List<RpslAttribute> getAttributes() {
        return attributes;
    }

    // Note: we use ISO_8859_1 encoding everywhere as it is the only one that maps directly from byte to char (as in, it effectively is a '(char)byteValue')
    static List<RpslAttribute> getAttributes(String input) {
        return getAttributes(input.getBytes(Charsets.ISO_8859_1));
    }

    static List<RpslAttribute> getAttributes(byte[] buf) {
        Validate.notNull(buf, "Object can not be null");

        final List<RpslAttribute> newAttributes = new ArrayList<>(32);

        int pos = 0;
        while (pos < buf.length) {
            int start = pos;

            boolean readKey = false;
            for (; pos < buf.length; pos++) {
                int c = buf[pos] & 0xff;

                if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == ':' || c == '*' || c == ' ')) {
                    throw new IllegalArgumentException("Read illegal character in key: '" + (char) c + "'");
                }

                if (c == ':') {
                    readKey = true;
                    break;
                }
            }

            if (!readKey) throw new IllegalArgumentException("No key found");
            if (start == pos) throw new IllegalArgumentException("Read zero sized key");

            final String key = new String(buf, start, pos - start, Charsets.ISO_8859_1);

            // skip over ':' and continue reading the attribute value
            start = ++pos;
            int stop = pos;

            processStream:
            for (; pos < buf.length; ) {
                int c = buf[pos++] & 0xff;

                if (c == '\r') {
                    continue;
                }

                if (c == '\n') {
                    int next = (pos < buf.length) ? buf[pos] & 0xff : -1;

                    switch (next) {
                        case ' ':
                        case '\t':
                        case '+':
                            break;
                        default:
                            break processStream;
                    }
                }

                stop = pos;
            }

            final String value = new String(buf, start, stop - start, Charsets.ISO_8859_1);
            newAttributes.add(new RpslAttribute(key, value));
        }

        return newAttributes;
    }

    public RpslAttribute getAttribute(int index) {
        return attributes.get(index);
    }

    public int size() {
        return attributes.size();
    }

    public RpslObjectBuilder setAttribute(int index, RpslAttribute attribute) {
        attributes.set(index, attribute);
        return this;
    }

    public RpslObjectBuilder addAttribute(final RpslAttribute newAttribute) {
        attributes.add(newAttribute);
        return this;
    }

    public RpslObjectBuilder addAttributes(final Collection<RpslAttribute> newAttributes) {
        attributes.addAll(newAttributes);
        return this;
    }

    /** by attribute order in template */
    public RpslObjectBuilder sort() {
        AttributeType attributeType = getTypeAttributeOrNull();
        if (attributeType != null) {
            Collections.sort(attributes, ObjectTemplate.getTemplate(ObjectType.getByFirstAttribute(attributeType)).getAttributeTypeComparator());
        }
        return this;
    }

    public AttributeType getTypeAttributeOrNull() {
        try {
            return attributes.get(0).getType();
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public AttributeType getTypeAttribute() {
        AttributeType type = attributes.get(0).getType();
        if (type == null) {
            throw new IllegalArgumentException(attributes.get(0) + " is not a known type");
        }
        return type;
    }

    public RpslObjectBuilder addAttributes(final int index, final Collection<RpslAttribute> newAttributes) {
        attributes.addAll(index, newAttributes);
        return this;
    }

    public RpslObjectBuilder replaceAttributes(final Map<RpslAttribute, RpslAttribute> attributesToReplace) {
        if (attributesToReplace.isEmpty()) {
            return this;
        }

        for (int i = 0; i < attributes.size(); i++) {
            RpslAttribute newValue = attributesToReplace.get(attributes.get(i));
            if (newValue != null) {
                attributes.set(i, newValue);
            }
        }
        return this;
    }

    public RpslObjectBuilder replaceAttribute(final RpslAttribute oldAttribute, final RpslAttribute newAttribute) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).equals(oldAttribute)) {
                attributes.set(i, newAttribute);
                return this;
            }
        }
        return this;
    }

    public RpslObjectBuilder removeAttribute(final RpslAttribute attribute) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).equals(attribute)) {
                attributes.remove(i);
                return this;
            }
        }
        return this;
    }

    public RpslObjectBuilder removeAttributeType(final AttributeType attributeType) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getType() == attributeType) {
                attributes.remove(i--);
            }
        }
        return this;
    }

    public RpslObjectBuilder removeAttributeTypes(final Collection<AttributeType> attributeTypes) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributeTypes.contains(attributes.get(i).getType())) {
                attributes.remove(i--);
            }
        }
        return this;
    }

    public RpslObjectBuilder retainAttributeType(final AttributeType attributeType) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getType() != attributeType) {
                attributes.remove(i--);
            }
        }
        return this;
    }

    public RpslObjectBuilder retainAttributeTypes(final Collection<AttributeType> attributeTypes) {
        for (int i = 0; i < attributes.size(); i++) {
            if (!attributeTypes.contains(attributes.get(i).getType())) {
                attributes.remove(i--);
            }
        }
        return this;
    }
}
