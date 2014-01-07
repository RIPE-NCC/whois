package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import difflib.DiffUtils;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.io.ByteArrayOutput;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Immutable
public class RpslObject implements Identifiable, ResponseObject {
    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults();

    private final ObjectType type;
    private final RpslAttribute typeAttribute;
    private final CIString key;

    // TODO: [AH] add sequence_id here too (to form the basis of versioning)
    private Integer objectId;

    private List<RpslAttribute> attributes;
    private Map<AttributeType, List<RpslAttribute>> typeCache;
    private int hash;

    public RpslObject(final RpslObject oldObject, final List<RpslAttribute> attributes) {
        this(oldObject.objectId, attributes);
    }

    public RpslObject(final Integer objectId, final List<RpslAttribute> attributes) {
        this(attributes);
        this.objectId = objectId;
    }

    public RpslObject(final Integer objectId, final RpslObject rpslObject) {
        this.objectId = objectId;
        this.attributes = rpslObject.attributes;
        this.type = rpslObject.type;
        this.typeAttribute = rpslObject.typeAttribute;
        this.key = rpslObject.key;
        this.typeCache = rpslObject.typeCache;
        this.hash = rpslObject.hash;
    }

    public RpslObject(final List<RpslAttribute> attributes) {
        Validate.notEmpty(attributes);

        this.typeAttribute = attributes.get(0);
        this.type = ObjectType.getByName(typeAttribute.getKey());
        this.attributes = Collections.unmodifiableList(attributes);

        Set<AttributeType> keyAttributes = ObjectTemplate.getTemplate(type).getKeyAttributes();
        if (keyAttributes.size() == 1) {
            this.key = getValueForAttribute(keyAttributes.iterator().next());
            Validate.notEmpty(this.key.toString(), "key attributes must have value");
        } else {
            StringBuilder keyBuilder = new StringBuilder(32);
            for (AttributeType keyAttribute : keyAttributes) {
                String key = getValueForAttribute(keyAttribute).toString();
                Validate.notEmpty(key, "key attributes must have value");
                keyBuilder.append(key);
            }
            this.key = CIString.ciString(keyBuilder.toString());
        }
    }

    public static RpslObject parse(final String input) {
        return new RpslObject(RpslObjectBuilder.getAttributes(input));
    }

    public static RpslObject parse(final byte[] input) {
        return new RpslObject(RpslObjectBuilder.getAttributes(input));
    }

    public static RpslObject parse(final Integer objectId, final String input) {
        return new RpslObject(objectId, RpslObjectBuilder.getAttributes(input));
    }

    public static RpslObject parse(final Integer objectId, final byte[] input) {
        return new RpslObject(objectId, RpslObjectBuilder.getAttributes(input));
    }

    @Override
    public int getObjectId() {
        return objectId;
    }

    @CheckForNull
    public ObjectType getType() {
        return type;
    }

    public List<RpslAttribute> getAttributes() {
        return attributes;
    }

    public int size() {
        return attributes.size();
    }

    public final CIString getKey() {
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

        if (!(obj instanceof RpslObject)) {
            return false;
        }

        final RpslObject other = (RpslObject) obj;
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

            typeCache = map;
        }

        return typeCache;
    }

    public RpslAttribute findAttribute(final AttributeType attributeType) {
        final List<RpslAttribute> foundAttributes = findAttributes(attributeType);
        switch (foundAttributes.size()) {
            case 0:
                throw new IllegalArgumentException("No attribute of type: " + attributeType);
            case 1:
                return foundAttributes.get(0);
            default:
                throw new IllegalArgumentException("Multiple attributes of type: " + attributeType);
        }
    }

    public List<RpslAttribute> findAttributes(final AttributeType attributeType) {
        final List<RpslAttribute> list = getOrCreateCache().get(attributeType);
        return list == null ? Collections.<RpslAttribute>emptyList() : Collections.unmodifiableList(list);
    }

    public List<RpslAttribute> findAttributes(final Iterable<AttributeType> attributeTypes) {
        final List<RpslAttribute> result = Lists.newArrayList();

        for (final AttributeType attributeType : attributeTypes) {
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

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        writeTo(new OutputStreamWriter(out, Charsets.ISO_8859_1));
    }

    public void writeTo(final Writer writer) throws IOException {
        for (final RpslAttribute attribute : getAttributes()) {
            attribute.writeTo(writer);
        }

        writer.flush();
    }

    @Override
    public byte[] toByteArray() {
        try {
            final ByteArrayOutput baos = new ByteArrayOutput();
            writeTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Should never occur", e);
        }
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

    public CIString getValueOrNullForAttribute(final AttributeType attributeType) {
        List<RpslAttribute> attributes = findAttributes(attributeType);
        if (attributes.isEmpty()) {
            return null;
        }
        return attributes.get(0).getCleanValue();
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

    public String diff(final RpslObject rpslObject) {
        final StringBuilder builder = new StringBuilder();

        final List<String> originalLines = Lists.newArrayList(LINE_SPLITTER.split(rpslObject.toString()));
        final List<String> revisedLines = Lists.newArrayList(LINE_SPLITTER.split(this.toString()));

        final List<String> diff = DiffUtils.generateUnifiedDiff(null, null, originalLines, DiffUtils.diff(originalLines, revisedLines), 1);

        for (int index = 2; index < diff.size(); index++) {
            // skip unified diff header lines
            builder.append(diff.get(index));
            builder.append('\n');
        }

        return builder.toString();
    }
}
