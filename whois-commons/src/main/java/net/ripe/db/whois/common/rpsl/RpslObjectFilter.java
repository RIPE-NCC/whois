package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.regex.Pattern;

// TODO: [AK] This should be renamed and moved to update
@Immutable
public class RpslObjectFilter {
    public static final String FILTERED = " # Filtered";

    private static final Splitter LINE_CONTINUATION_SPLITTER = Splitter.on(Pattern.compile("(\\n\\+|\\n[ ]|\\n\\t|\\n)")).trimResults();

    private final RpslObject object;

    public RpslObjectFilter(final RpslObject object) {
        this.object = object;
    }

    public String getCertificateFromKeyCert() {
        if (!ObjectType.KEY_CERT.equals(object.getType())) {
            throw new AuthenticationException("No key cert for object: " + object.getKey());
        }

        final StringBuilder builder = new StringBuilder();
        for (RpslAttribute next : object.findAttributes(AttributeType.CERTIF)) {
            for (String s : LINE_CONTINUATION_SPLITTER.split(next.getValue())) {
                builder.append(s).append('\n');
            }
        }

        return builder.toString();
    }

    @CheckForNull
    public CIString getSource() {
        final List<RpslAttribute> attributes = object.findAttributes(AttributeType.SOURCE);
        if (attributes.isEmpty()) {
            return null;
        }

        return attributes.get(0).getCleanValue();
    }

    public static boolean isFiltered(final RpslObject rpslObject) {
        final List<RpslAttribute> attributes = rpslObject.findAttributes(AttributeType.SOURCE);
        return !attributes.isEmpty() && attributes.get(0).getValue().contains(FILTERED);
    }

    public static RpslObject setFiltered(final RpslObject rpslObject) {
        final List<RpslAttribute> attributes = rpslObject.getAttributes();
        final List<RpslAttribute> result = Lists.newArrayListWithExpectedSize(attributes.size());
        for (RpslAttribute rpslAttribute : attributes) {
            if (rpslAttribute.getType() == AttributeType.SOURCE) {
                result.add(new RpslAttribute(AttributeType.SOURCE, rpslAttribute.getCleanValue() + FILTERED));
            } else {
                result.add(rpslAttribute);
            }
        }
        return new RpslObject(rpslObject, result);
    }

    public RpslObject addAttributes(final Collection<RpslAttribute> newAttributes) {
        if (newAttributes.isEmpty()) {
            return object;
        }

        final Map<AttributeType, List<RpslAttribute>> newAttributeMap = Maps.newEnumMap(AttributeType.class);
        for (final RpslAttribute newAttribute : newAttributes) {
            final AttributeType newAttributeType = newAttribute.getType();
            List<RpslAttribute> newAttributeList = newAttributeMap.get(newAttributeType);
            if (newAttributeList == null) {
                newAttributeList = Lists.newArrayList();
                newAttributeMap.put(newAttributeType, newAttributeList);
            }

            newAttributeList.add(newAttribute);
        }

        final List<RpslAttribute> attributes = Lists.newArrayList();
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(object.getType());
        for (final AttributeTemplate attributeTemplate : objectTemplate.getAttributeTemplates()) {
            final AttributeType attributeType = attributeTemplate.getAttributeType();
            attributes.addAll(object.findAttributes(attributeType));

            final List<RpslAttribute> newAttributeList = newAttributeMap.get(attributeType);
            if (newAttributeList != null) {
                attributes.addAll(newAttributeList);
            }
        }

        return new RpslObject(object, attributes);
    }

    public RpslObject addAttributes(final Collection<RpslAttribute> newAttributes, final int index) {
        final List<RpslAttribute> attributes = Lists.newArrayList(object.getAttributes());
        attributes.addAll(index, newAttributes);
        return new RpslObject(object, attributes);
    }

    // TODO: [AH] turn this into an rpslobjectbuilder instead
    public RpslObject replaceAttributes(final Map<RpslAttribute, RpslAttribute> attributesToReplace) {
        if (attributesToReplace.isEmpty()) {
            return object;
        }

        final List<RpslAttribute> newAttributes = Lists.newArrayList();
        for (final RpslAttribute attribute : object.getAttributes()) {
            final RpslAttribute replacement = attributesToReplace.get(attribute);
            if (replacement != null) {
                newAttributes.add(replacement);
            } else {
                newAttributes.add(attribute);
            }
        }

        return new RpslObject(object, newAttributes);
    }

    public static RpslObject removeAttribute(final RpslObject rpslObject, final int index) {
        final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());
        attributes.remove(index);
        return new RpslObject(rpslObject, attributes);
    }

    public static RpslObject removeAttributeTypes(final RpslObject rpslObject, final Collection<AttributeType> remove) {
        if (!rpslObject.containsAttributes(remove)) {
            return rpslObject;
        }

        final List<RpslAttribute> result = Lists.newArrayList();
        for (RpslAttribute rpslAttribute : rpslObject.getAttributes()) {
            if (!remove.contains(rpslAttribute.getType())) {
                result.add(rpslAttribute);
            }
        }
        return new RpslObject(rpslObject, result);
    }

    public static List<RpslObjectInfo> filterByType(final ObjectType type, final List<RpslObjectInfo> objectInfos) {
        final List<RpslObjectInfo> result = Lists.newArrayList();
        for (final RpslObjectInfo objectInfo : objectInfos) {
            if (objectInfo.getObjectType().equals(type)) {
                result.add(objectInfo);
            }
        }

        return result;
    }

    // leave only the type and key attributes of an object; used in first pass of 2-pass loading
    public static List<RpslAttribute> keepKeyAttributesOnly(RpslObject rpslObject) {
        final ObjectTemplate template = ObjectTemplate.getTemplate(rpslObject.getType());
        final Set<AttributeType> keyAttributes = Sets.newLinkedHashSet();
        keyAttributes.addAll(template.getLookupAttributes());
        keyAttributes.addAll(template.getKeyAttributes());
        return rpslObject.findAttributes(keyAttributes);
    }
}
