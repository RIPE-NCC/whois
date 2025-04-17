package net.ripe.db.whois.common.rpsl;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
        // do not instantiate
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
        final Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);
        final List<String> diff = UnifiedDiffUtils.generateUnifiedDiff("original", "revised", originalLines, patch, 1);
        for (int index = 2; index < diff.size(); index++) {
            // skip unified diff header lines
            builder.append(diff.get(index));
            builder.append('\n');
        }

        return builder.toString();
    }


    public static RpslObjectBuilder keepKeyAttributesOnly(final RpslObjectBuilder builder) {
        final ObjectTemplate template = ObjectTemplate.getTemplate(ObjectType.getByFirstAttribute(builder.getTypeAttribute()));
        final Set<AttributeType> keyAttributes = Sets.newHashSet();
        keyAttributes.addAll(template.getLookupAttributes());
        keyAttributes.addAll(template.getKeyAttributes());
        return builder.retainAttributeTypes(keyAttributes);
    }

    public static RpslObjectBuilder setFiltered(final RpslObjectBuilder builder) {
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
        final RpslAttribute attribute = object.findAttribute(AttributeType.SOURCE);
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
        final List<RpslAttribute> attributeList = new ArrayList<>();
        for (String attribute : attributes) {
            attributeList.addAll(RpslObjectBuilder.getAttributes(attribute));
        }

        final EnumSet<AttributeType> seenTypes = EnumSet.noneOf(AttributeType.class);
        for (RpslAttribute rpslAttribute : attributeList) {
            seenTypes.add(rpslAttribute.getType());
        }

        builder.removeAttributeTypes(seenTypes);
        builder.append(attributeList);
        return builder.sort().get();
    }

    public static boolean ignoreGeneratedAttributesEqual(final RpslObject object1, final RpslObject object2) {
        return Iterables.elementsEqual(
                Iterables.filter(object1.getAttributes(), RpslObjectFilter::notGenerated),
                Iterables.filter(object2.getAttributes(), RpslObjectFilter::notGenerated));
    }

    //[TP]: we do not use the generated tag in from the object template because it has side effects for autnum status
    // and sponsoring org. Other systems need to modify these objects and not receive a NOOP instead of modify
    private static final List<AttributeType> PURELY_GENERATED_ATTRIBUTES =
            Lists.newArrayList(
                    AttributeType.CREATED,
                    AttributeType.CHANGED,
                    AttributeType.LAST_MODIFIED,
                    AttributeType.FINGERPR,
                    AttributeType.OWNER,
                    AttributeType.METHOD);

    private static boolean notGenerated(final RpslAttribute input) {
        return !PURELY_GENERATED_ATTRIBUTES.contains(input.getType());
    }

}
