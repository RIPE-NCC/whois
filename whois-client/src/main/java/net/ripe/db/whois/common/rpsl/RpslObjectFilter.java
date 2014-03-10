package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import difflib.DiffUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

// Helper class for less-frequently used information extractions from RpslObject
public class RpslObjectFilter {
    static final String FILTERED = " # Filtered";

    private static final Splitter LINE_CONTINUATION_SPLITTER = Splitter.on(Pattern.compile("(\\n\\+|\\n[ ]|\\n\\t|\\n)")).trimResults();
    private static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults();

    private RpslObjectFilter() {
    }

    public static String getCertificateFromKeyCert(final RpslObject object) {
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

    public static String diff(final RpslObject original, final RpslObject revised) {
        final StringBuilder builder = new StringBuilder();

        final List<String> originalLines = Lists.newArrayList(LINE_SPLITTER.split(original.toString()));
        final List<String> revisedLines = Lists.newArrayList(LINE_SPLITTER.split(revised.toString()));

        final List<String> diff = DiffUtils.generateUnifiedDiff(null, null, originalLines, DiffUtils.diff(originalLines, revisedLines), 1);

        for (int index = 2; index < diff.size(); index++) {
            // skip unified diff header lines
            builder.append(diff.get(index));
            builder.append('\n');
        }

        return builder.toString();
    }


    public static RpslObjectBuilder keepKeyAttributesOnly(RpslObjectBuilder builder) {
        final ObjectTemplate template = ObjectTemplate.getTemplate(ObjectType.getByFirstAttribute(builder.getTypeAttribute()));
        final Set<AttributeType> keyAttributes = Sets.newHashSet();
        keyAttributes.addAll(template.getLookupAttributes());
        keyAttributes.addAll(template.getKeyAttributes());
        return builder.retainAttributeTypes(keyAttributes);
    }

    public static RpslObjectBuilder setFiltered(RpslObjectBuilder builder) {
        for (int i = builder.size() - 1; i >= 0; i--) {
            RpslAttribute attribute = builder.get(i);
            if (attribute.getType() == AttributeType.SOURCE) {
                builder.set(i, new RpslAttribute(AttributeType.SOURCE, attribute.getCleanValue() + FILTERED));
                break;
            }
        }
        return builder;
    }

    public static void addFilteredSourceReplacement(final RpslObject object, final Map<RpslAttribute, RpslAttribute> replacementsMap) {
        RpslAttribute attribute = object.findAttribute(AttributeType.SOURCE);
        replacementsMap.put(attribute, new RpslAttribute(AttributeType.SOURCE, attribute.getCleanValue() + FILTERED));
    }

    public static boolean isFiltered(final RpslObject rpslObject) {
        final List<RpslAttribute> attributes = rpslObject.findAttributes(AttributeType.SOURCE);
        return !attributes.isEmpty() && attributes.get(0).getValue().contains(FILTERED);
    }

    /** slow way to build specific objects from object skeletons/templates */
    public static RpslObject buildGenericObject(final RpslObject object, final String ... attributes) {
        return buildGenericObject(new RpslObjectBuilder(object), attributes);
    }

    /** slow way to build specific objects from object skeletons/templates */
    public static RpslObject buildGenericObject(final String object, final String ... attributes) {
        return buildGenericObject(new RpslObjectBuilder(object), attributes);
    }

    /** slow way to build specific objects from object skeletons/templates */
    public static RpslObject buildGenericObject(final RpslObjectBuilder builder, final String ... attributes) {
        List<RpslAttribute> attributeList = new ArrayList<>();
        for (String attribute : attributes) {
            attributeList.addAll(RpslObjectBuilder.getAttributes(attribute));
        }

        EnumSet<AttributeType> seenTypes = EnumSet.noneOf(AttributeType.class);
        for (RpslAttribute rpslAttribute : attributeList) {
            seenTypes.add(rpslAttribute.getType());
        }

        builder.removeAttributeTypes(seenTypes);
        builder.prepend(attributeList);
        return builder.sort().get();
    }

}
