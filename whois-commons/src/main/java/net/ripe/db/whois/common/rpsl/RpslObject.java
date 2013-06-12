package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import difflib.DiffUtils;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.io.ByteArrayOutput;
import org.apache.commons.lang.Validate;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

// TODO [AK] This should be moved to whois queries, something like RpslObjectResponse
@Immutable
public class RpslObject implements ResponseObject, Identifiable {
    private final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults();

    private final Integer objectId;
    private final RpslObjectBase base;

    public RpslObject(final RpslObject oldObject, final List<RpslAttribute> attributes) {
        this(oldObject.objectId, attributes);
    }

    public RpslObject(final RpslObjectBase rpslObjectBase) {
        this(-1, rpslObjectBase);
    }

    public RpslObject(final Integer objectId, final List<RpslAttribute> attributes) {
        this.base = new RpslObjectBase(attributes);
        this.objectId = objectId;

        Validate.notNull(getType(), "Type cannot be null");
        Validate.notNull(getKey(), "Key cannot be null");
        Validate.notEmpty(getKey().toString(), "Key cannot be empty");
    }

    private RpslObject(final Integer objectId, final RpslObjectBase base) {
        this.objectId = objectId;
        this.base = base;

        Validate.notNull(getType(), "Type cannot be null");
        Validate.notNull(getKey(), "Key cannot be null");
        Validate.notEmpty(getKey().toString(), "Key cannot be empty");
    }

    public static RpslObject parse(final String input) {
        return parse(input.getBytes(Charsets.ISO_8859_1));
    }

    public static RpslObject parse(final Integer objectId, final String input) {
        return parse(objectId, input.getBytes(Charsets.ISO_8859_1));
    }

    public static RpslObject parseFully(final String input) {
        final RpslObject rpslObject = parse(input.getBytes(Charsets.ISO_8859_1));
        rpslObject.base.getOrCreateCache();
        return rpslObject;
    }

    static RpslObject parse(final byte[] input) {
        return parse(null, input);
    }

    public static RpslObject parse(final Integer objectId, final byte[] input) {
        Validate.notNull(input, "input cannot be null");
        Validate.isTrue(input.length > 0, "input cannot be empty");
        return new RpslObject(objectId, new RpslObjectBase(input));
    }

    @Override
    public int getObjectId() {
        return objectId;
    }

    public List<RpslAttribute> getAttributes() {
        return base.getAttributes();
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
        return base.equals(other.base);
    }

    @Override
    public int hashCode() {
        return base.hashCode();
    }

    public ObjectType getType() {
        return base.getType();
    }

    public RpslAttribute getTypeAttribute() {
        return base.getTypeAttribute();
    }

    public RpslAttribute findAttribute(final AttributeType attributeType) {
        return base.findAttribute(attributeType);
    }

    public List<RpslAttribute> findAttributes(final AttributeType attributeType) {
        return base.findAttributes(attributeType);
    }

    public List<RpslAttribute> findAttributes(final Iterable<AttributeType> attributeTypes) {
        return base.findAttributes(attributeTypes);
    }

    public List<RpslAttribute> findAttributes(final AttributeType... attributeTypes) {
        return base.findAttributes(attributeTypes);
    }

    public boolean containsAttributes(final Collection<AttributeType> attributeTypes) {
        return base.containsAttributes(attributeTypes);
    }

    public boolean containsAttribute(final AttributeType attributeType) {
        return base.containsAttribute(attributeType);
    }

    public CIString getKey() {
        return base.getKey();
    }

    public String getFormattedKey() {
        return base.getFormattedKey();
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        base.writeTo(new OutputStreamWriter(out, Charsets.ISO_8859_1));
    }

    public void writeTo(final Writer writer) throws IOException {
        base.writeTo(writer);
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
        return base.toString();
    }

    public CIString getValueForAttribute(final AttributeType attributeType) {
        return base.getValueForAttribute(attributeType);
    }

    public Set<CIString> getValuesForAttribute(final AttributeType attributeType) {
        return base.getValuesForAttribute(attributeType);
    }

    public void forAttributes(final AttributeType attributeType, final AttributeCallback attributeCallback) {
        for (final RpslAttribute rpslAttribute : findAttributes(attributeType)) {
            for (final CIString value : rpslAttribute.getCleanValues()) {
                attributeCallback.execute(rpslAttribute, value);
            }
        }
    }

    public interface AttributeCallback {
        void execute(RpslAttribute attribute, CIString value);
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
