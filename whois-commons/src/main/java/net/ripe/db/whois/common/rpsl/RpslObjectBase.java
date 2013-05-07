package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

// TODO [AK] This should be renamed to RpslObject
@Immutable
public class RpslObjectBase {
    private final ObjectType type;
    private final RpslAttribute typeAttribute;

    private List<RpslAttribute> attributes;
    private Map<AttributeType, List<RpslAttribute>> typeCache;
    private int hash;
    private CIString key;

    private byte buf[];
    private int pos;
    private int count;

    public RpslObjectBase(final List<RpslAttribute> attributes) {
        this.typeAttribute = attributes.get(0);
        this.type = ObjectType.getByNameOrNull(typeAttribute.getKey());
        this.attributes = Collections.unmodifiableList(attributes);

    }

    RpslObjectBase(final byte[] buf) {
        this.buf = buf;
        this.count = buf.length;
        this.typeAttribute = new RpslAttribute(readKey(), readValue());
        this.type = ObjectType.getByNameOrNull(typeAttribute.getKey());
    }

    public static RpslObjectBase parse(final String input) {
        return parse(input.getBytes(Charsets.ISO_8859_1));
    }

    public static RpslObjectBase parse(final byte[] input) {
        Validate.notNull(input, "input cannot be null");
        Validate.isTrue(input.length > 0, "input cannot be empty");
        return new RpslObjectBase(input);
    }

    private byte[] readKey() {
        int c;
        int start = pos;
        int stop = start;

        boolean readKey = false;
        while ((c = (pos < count) ? (buf[pos++] & 0xff) : -1) != -1) {
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == ':' || c == '*' || c == ' ')) {
                throw new IllegalArgumentException("Read illegal character in key: '" + c + "'");
            }

            if (c == ':') {
                stop = pos - 1;
                readKey = true;
                break;
            }
        }

        if (!readKey || start == stop) {
            throw new IllegalArgumentException("Read zero sized key");
        }

        final byte[] key = new byte[stop - start];
        System.arraycopy(buf, start, key, 0, stop - start);
        return key;
    }

    private byte[] readValue() {
        int c;
        int next;
        int start = pos;
        int stop = start;

        processStream:
        while ((c = (pos < count) ? (buf[pos++] & 0xff) : -1) != -1) {

            if (c == '\r') {
                continue;
            }

            if (c == '\n') {
                next = (pos < count) ? (buf[pos] & 0xff) : -1;

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

        final byte[] value = new byte[stop - start];
        System.arraycopy(buf, start, value, 0, stop - start);
        return value;
    }

    @CheckForNull
    public ObjectType getType() {
        return type;
    }

    public List<RpslAttribute> getAttributes() {
        if (attributes == null) {
            final List<RpslAttribute> newAttributes = new ArrayList<RpslAttribute>(32);
            newAttributes.add(typeAttribute);

            while (pos < count) {
                newAttributes.add(new RpslAttribute(readKey(), readValue()));
            }

            this.attributes = Collections.unmodifiableList(newAttributes);
            this.buf = null;
        }

        return attributes;
    }

    public CIString getKey() {
        if (key == null) {
            final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(type);
            final Iterator<AttributeType> keyAttributeIterator = objectTemplate.getKeyAttributes().iterator();

            CIString tmpKey = findAttribute(keyAttributeIterator.next()).getCleanValue();
            while (keyAttributeIterator.hasNext()) {
                tmpKey = tmpKey.append(findAttribute(keyAttributeIterator.next()).getCleanValue());
            }

            key = tmpKey;
        }

        return key;
    }

    public String getFormattedKey() {
        switch (type) {
            case PERSON:
            case ROLE:
                return String.format("[%s] %s   %s", type.getName(), getKey(), getAttributes().get(0).getCleanValue());
            default:
                return String.format("[%s] %s", type.getName(), getKey());
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RpslObjectBase)) {
            return false;
        }

        final RpslObjectBase other = (RpslObjectBase) obj;
        return Iterables.elementsEqual(getAttributes(), other.getAttributes());
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int result = getAttributes().hashCode();
            if (result == 0) {
                result--;
            }
            hash = result;
        }

        return hash;
    }

    public RpslAttribute getTypeAttribute() {
        return getAttributes().get(0);
    }

    Map<AttributeType, List<RpslAttribute>> getOrCreateCache() {
        if (typeCache == null) {
            final EnumMap<AttributeType, List<RpslAttribute>> map = Maps.newEnumMap(AttributeType.class);

            for (final RpslAttribute attribute : getAttributes()) {
                final AttributeType attributeType = attribute.getType();
                if (attributeType == null) {
                    continue;
                }

                List<RpslAttribute> list = map.get(attributeType);
                if (list == null) {
                    list = Lists.newArrayList();
                    map.put(attributeType, list);
                }

                list.add(attribute);
            }

            typeCache = Collections.unmodifiableMap(map);
        }

        return typeCache;
    }

    public RpslAttribute findAttribute(final AttributeType attributeType) {
        final List<RpslAttribute> attributes = findAttributes(attributeType);
        switch (attributes.size()) {
            case 0:
                throw new IllegalArgumentException("No attribute of type: " + attributeType);
            case 1:
                return attributes.get(0);
            default:
                throw new IllegalArgumentException("Multiple attributes of type: " + attributeType);
        }
    }

    public List<RpslAttribute> findAttributes(final AttributeType attributeType) {
        final List<RpslAttribute> list = getOrCreateCache().get(attributeType);
        return list == null ? Collections.<RpslAttribute>emptyList() : Collections.unmodifiableList(list);
    }

    public List<RpslAttribute> findAttributes(final Collection<AttributeType> attributeTypes) {
        final List<RpslAttribute> result = Lists.newArrayList();

        for (AttributeType attributeType : attributeTypes) {
            findCachedAttributes(result, attributeType);
        }

        return result;
    }

    public List<RpslAttribute> findAttributes(final AttributeType... attributeTypes) {
        final List<RpslAttribute> result = Lists.newArrayList();

        for (AttributeType attributeType : attributeTypes) {
            findCachedAttributes(result, attributeType);
        }

        return result;
    }

    private void findCachedAttributes(List<RpslAttribute> result, AttributeType attributeType) {
        final List<RpslAttribute> list = getOrCreateCache().get(attributeType);
        if (list != null) {
            result.addAll(list);
        }
    }

    public boolean containsAttributes(final Collection<AttributeType> attributeTypes) {
        for (AttributeType attributeType : attributeTypes) {
            if (containsAttribute(attributeType)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAttribute(final AttributeType attributeType) {
        return getOrCreateCache().containsKey(attributeType);
    }

    public void writeTo(final Writer writer) throws IOException {
        for (final RpslAttribute attribute : getAttributes()) {
            attribute.writeTo(writer);
        }

        writer.flush();
    }

    @Override
    public String toString() {
        try {
            final StringWriter writer = new StringWriter();
            for (final RpslAttribute attribute : getAttributes()) {
                attribute.writeTo(writer);
            }

            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Should never occur", e);
        }
    }

    public CIString getValueForAttribute(final AttributeType attributeType) {
        return findAttribute(attributeType).getCleanValue();
    }

    public Set<CIString> getValuesForAttribute(final AttributeType attributeType) {
        final List<RpslAttribute> attributeList = findAttributes(attributeType);
        if (attributeList.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<CIString> values = Sets.newLinkedHashSet();
        for (final RpslAttribute attribute : attributeList) {
            values.addAll(attribute.getCleanValues());
        }

        return values;
    }
}
