package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Cardinality.MULTIPLE;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Cardinality.SINGLE;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key.INVERSE_KEY;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key.LOOKUP_KEY;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Key.PRIMARY_KEY;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Order;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Order.TEMPLATE_ORDER;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Order.USER_ORDER;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.DEPRECATED;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.GENERATED;
import static net.ripe.db.whois.common.rpsl.AttributeTemplate.Requirement.MANDATORY;

public abstract class ObjectTemplate implements Comparable<ObjectTemplate> {
    protected static Map<ObjectType, ObjectTemplate> TEMPLATE_MAP;

    @SuppressWarnings("unchecked")
    private class AttributeTypeComparator implements Comparator<RpslAttribute> {
        private EnumMap<AttributeType, Integer> order = new EnumMap(AttributeType.class);

        public AttributeTypeComparator(final AttributeTemplate... attributeTemplates) {
            int i = 0;
            Order prevOrder = null;

            for (AttributeTemplate attributeTemplate : attributeTemplates) {
                final Order actOrder = attributeTemplate.getOrder();

                if (prevOrder == USER_ORDER && actOrder == TEMPLATE_ORDER) {
                    i++;
                }

                order.put(attributeTemplate.getAttributeType(), i);

                if (actOrder == TEMPLATE_ORDER) {
                    i++;
                }

                prevOrder = actOrder;
            }
        }

        @Override
        public int compare(final RpslAttribute o1, final RpslAttribute o2) {
            try {
                return order.get(o1.getType()) - order.get(o2.getType());
            } catch (NullPointerException e) {
                return 0;
            }
        }
    }

    private ObjectType objectType;
    private int orderPosition;
    private Map<AttributeType, AttributeTemplate> attributeTemplateMap;
    private List<AttributeTemplate> attributeTemplates;
    private Set<AttributeType> allAttributeTypes;
    private Set<AttributeType> keyAttributes;
    private Set<AttributeType> lookupAttributes;
    private AttributeType keyLookupAttribute;
    private Set<AttributeType> inverseLookupAttributes;
    private Set<AttributeType> mandatoryAttributes;
    private Set<AttributeType> multipleAttributes;
    private Set<AttributeType> generatedAttributes;

    private Comparator<RpslAttribute> comparator;

    public ObjectTemplate() {
        init();
    }

    protected abstract void init();

    protected ObjectTemplate(final ObjectType objectType, final int orderPosition, final AttributeTemplate... attributeTemplates) {
        this.objectType = objectType;
        this.orderPosition = orderPosition;

        this.attributeTemplates = ImmutableList.copyOf(attributeTemplates);
        this.allAttributeTypes = Collections.unmodifiableSet(Sets.newLinkedHashSet(Iterables.transform(this.attributeTemplates, new Function<AttributeTemplate, AttributeType>() {
            @Nullable
            @Override
            public AttributeType apply(final AttributeTemplate input) {
                return input.getAttributeType();
            }
        })));

        this.attributeTemplateMap = Maps.newEnumMap(AttributeType.class);
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            this.attributeTemplateMap.put(attributeTemplate.getAttributeType(), attributeTemplate);
        }

        keyAttributes = getAttributes(attributeTemplates, PRIMARY_KEY);
        lookupAttributes = getAttributes(attributeTemplates, LOOKUP_KEY);
        inverseLookupAttributes = getAttributes(attributeTemplates, INVERSE_KEY);
        mandatoryAttributes = getAttributes(attributeTemplates, MANDATORY);
        multipleAttributes = getAttributes(attributeTemplates, MULTIPLE);
        generatedAttributes = getAttributes(attributeTemplates, GENERATED);

        keyLookupAttribute = Iterables.getOnlyElement(Sets.intersection(keyAttributes, lookupAttributes));

        comparator = new AttributeTypeComparator(attributeTemplates);
    }

    private Set<AttributeType> getAttributes(final AttributeTemplate[] attributeTemplates, final Key key) {
        final Set<AttributeType> attributeTypes = Sets.newLinkedHashSet();
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            if (attributeTemplate.getKeys().contains(key)) {
                attributeTypes.add(attributeTemplate.getAttributeType());
            }
        }

        return Collections.unmodifiableSet(attributeTypes);
    }

    private Set<AttributeType> getAttributes(final AttributeTemplate[] attributeTemplates, final AttributeTemplate.Requirement requirement) {
        final Set<AttributeType> attributeTypes = Sets.newLinkedHashSet();
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            if (attributeTemplate.getRequirement() == requirement) {
                attributeTypes.add(attributeTemplate.getAttributeType());
            }
        }

        return Collections.unmodifiableSet(attributeTypes);
    }

    private Set<AttributeType> getAttributes(final AttributeTemplate[] attributeTemplates, final AttributeTemplate.Cardinality cardinality) {
        final Set<AttributeType> attributeTypes = Sets.newLinkedHashSet();
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            if (attributeTemplate.getCardinality() == cardinality) {
                attributeTypes.add(attributeTemplate.getAttributeType());
            }
        }

        return Collections.unmodifiableSet(attributeTypes);
    }

    ObjectTemplate getTemplate(final ObjectType type) {
        final ObjectTemplate objectTemplate = TEMPLATE_MAP.get(type);
        if (objectTemplate == null) {
            throw new IllegalStateException("No template for " + type);
        }

        return objectTemplate;
    }

    static Collection<ObjectTemplate> getTemplates() {
        return TEMPLATE_MAP.values();
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public List<AttributeTemplate> getAttributeTemplates() {
        return attributeTemplates;
    }

    public Set<AttributeType> getAllAttributes() {
        return allAttributeTypes;
    }

    public Set<AttributeType> getKeyAttributes() {
        return keyAttributes;
    }

    public Set<AttributeType> getLookupAttributes() {
        return lookupAttributes;
    }

    public AttributeType getKeyLookupAttribute() {
        return keyLookupAttribute;
    }

    public Set<AttributeType> getMandatoryAttributes() {
        return mandatoryAttributes;
    }

    public Set<AttributeType> getInverseLookupAttributes() {
        return inverseLookupAttributes;
    }

    public Set<AttributeType> getGeneratedAttributes() {
        return generatedAttributes;
    }

    public Set<AttributeType> getMultipleAttributes() {
        return multipleAttributes;
    }

    public Comparator<RpslAttribute> getAttributeTypeComparator() {
        return comparator;
    }

    public boolean isSet() {
        return ObjectType.getSets().contains(objectType);
    }

    private String stripDashes( String in) {
        return StringUtils.replace(in, "-", "");
    }

    private static String toCamelCase( final String in) {
        final StringBuilder camelCaseString = new StringBuilder("");
        final String[] parts = StringUtils.split(in, "-");

        for (String part : parts){
            final String capitalized = StringUtils.capitalize(part);
            camelCaseString.append(capitalized);
        }
        return camelCaseString.toString();
    }

    private static String toFirstLower( final String in ) {
        return StringUtils.uncapitalize(in);
    }

    public String getNameToFirstUpper(  ) {
        return toCamelCase(this.objectType.getName());
    }

    public String getNameToFirstLower( ) {
        return toFirstLower(toCamelCase(this.objectType.getName()));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && objectType == ((ObjectTemplate) o).objectType;
    }

    @Override
    public int hashCode() {
        return objectType.hashCode();
    }

    @Override
    public int compareTo(@Nonnull final ObjectTemplate o) {
        return orderPosition - o.orderPosition;
    }

    public void validateStructure(final RpslObject rpslObject, final ObjectMessages objectMessages) {
        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            final AttributeType attributeType = attribute.getType();
            if (attributeType == null) {
                objectMessages.addMessage(attribute, ValidationMessages.unknownAttribute(attribute.getKey()));
            } else {
                final AttributeTemplate attributeTemplate = attributeTemplateMap.get(attributeType);
                if (attributeTemplate == null) {
                    objectMessages.addMessage(attribute, ValidationMessages.invalidAttributeForObject(attributeType));
                }
            }
        }
    }

    public void validateSyntax(final RpslObject rpslObject, final ObjectMessages objectMessages, final boolean skipGenerated) {
        final ObjectType rpslObjectType = rpslObject.getType();

        final Map<AttributeType, Integer> attributeCount = Maps.newEnumMap(AttributeType.class);
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            attributeCount.put(attributeTemplate.getAttributeType(), 0);
        }

        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            final AttributeType attributeType = attribute.getType();

            if (attributeType != null) {
                final AttributeTemplate attributeTemplate = attributeTemplateMap.get(attributeType);
                if (attributeTemplate != null) {
                    if (skipGenerated && attributeTemplate.getRequirement() == GENERATED) continue;
                    attribute.validateSyntax(rpslObjectType, objectMessages);
                    attributeCount.put(attributeType, attributeCount.get(attributeType) + 1);
                }
            }
        }

        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            if (skipGenerated && attributeTemplate.getRequirement() == GENERATED) continue;

            final AttributeType attributeType = attributeTemplate.getAttributeType();
            final int attributeTypeCount = attributeCount.get(attributeType);

            if( attributeTemplate.getRequirement() == DEPRECATED && attributeTypeCount > 0) {
                if (attributeType.equals(AttributeType.CHANGED)) {
                    objectMessages.addMessage(ValidationMessages.changedAttributeRemoved());
                    continue;
                }

                objectMessages.addMessage(ValidationMessages.deprecatedAttributeFound(attributeType));
            }

            if (attributeTemplate.getRequirement() == MANDATORY && attributeTypeCount == 0) {
                objectMessages.addMessage(ValidationMessages.missingMandatoryAttribute(attributeType));
            }

            if (attributeTemplate.getCardinality() == SINGLE && attributeTypeCount > 1) {
                objectMessages.addMessage(ValidationMessages.tooManyAttributesOfType(attributeType));
            }
        }
    }

    public ObjectMessages validate(final RpslObject rpslObject) {
        final ObjectMessages objectMessages = new ObjectMessages();
        validateStructure(rpslObject, objectMessages);
        validateSyntax(rpslObject, objectMessages, false);
        return objectMessages;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            result.append(attributeTemplate).append('\n');
        }

        return result.toString();
    }

    public String toVerboseString() {
        final StringBuilder result = new StringBuilder();

        result.append("The ")
                .append(objectType.getName())
                .append(" class:\n\n")
                .append(ObjectDocumentation.getDocumentation(objectType))
                .append('\n')
                .append(toString())
                .append("\nThe content of the attributes of the ")
                .append(objectType.getName())
                .append(" class are defined below:\n\n");

        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            final AttributeType attributeType = attributeTemplate.getAttributeType();

            String attributeDescription = attributeType.getDescription(objectType);
            if (attributeDescription.indexOf('\n') == -1) {
                attributeDescription = WordUtils.wrap(attributeDescription, 70);
            }

            if (attributeDescription.endsWith("\n")) {
                attributeDescription = attributeDescription.substring(0, attributeDescription.length() - 1);
            }

            String syntaxDescription = attributeType.getSyntax().getDescription(objectType);
            if (syntaxDescription.endsWith("\n")) {
                syntaxDescription = syntaxDescription.substring(0, syntaxDescription.length() - 1);
            }

            result.append(attributeType.getName())
                    .append("\n\n   ")
                    .append(attributeDescription.replaceAll("\n", "\n   "))
                    .append("\n\n     ")
                    .append(syntaxDescription.replaceAll("\n", "\n     "))
                    .append("\n\n");
        }

        return result.toString();
    }

    public boolean hasAttribute(final AttributeType attributeType) {
        return getAllAttributes().contains(attributeType);
    }
}
