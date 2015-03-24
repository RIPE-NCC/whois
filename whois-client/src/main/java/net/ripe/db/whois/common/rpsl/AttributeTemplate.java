package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Set;

public class AttributeTemplate {
    private static final String ATTRIBUTE_MANDATORY = "mandatory";
    private static final String ATTRIBUTE_OPTIONAL = "optional";
    private static final String ATTRIBUTE_GENERATED = "generated";
    private static final String ATTRIBUTE_DEPRECATED = "deprecated";

    public static enum Requirement {
        MANDATORY(ATTRIBUTE_MANDATORY,ATTRIBUTE_MANDATORY),
        OPTIONAL(ATTRIBUTE_OPTIONAL,ATTRIBUTE_OPTIONAL),
        GENERATED(ATTRIBUTE_GENERATED,ATTRIBUTE_GENERATED),
        DEPRECATED(ATTRIBUTE_DEPRECATED,ATTRIBUTE_OPTIONAL);

        private final String name;
        private final String externalName;

        private Requirement(final String name,final String externalName) {
            this.name = name;
            this.externalName = externalName;
        }

        public String getName() {
            return name;
        }
        public String getExternalName() {
            return externalName;
        }

    }

    public static enum Cardinality {
        SINGLE("single"),
        MULTIPLE("multiple");

        private final String name;

        private Cardinality(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static enum Key {
        PRIMARY_KEY("primary"),
        LOOKUP_KEY("lookup"),
        INVERSE_KEY("inverse");

        private final String name;

        private Key(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /** signifies if an attribute order matters or not */
    public static enum Order {
        TEMPLATE_ORDER, USER_ORDER;
    }

    private final AttributeType attributeType;
    private final Requirement requirement;
    private final Cardinality cardinality;
    private final Set<Key> keys;
    private final Order order;

    AttributeTemplate(final AttributeType attributeType, final Requirement requirement, final Cardinality cardinality, final Key... keys) {
        this(attributeType, requirement, cardinality, Order.TEMPLATE_ORDER, keys);
    }

    AttributeTemplate(final AttributeType attributeType, final Requirement requirement, final Cardinality cardinality, final Order order, final Key... keys) {
        this.attributeType = attributeType;
        this.requirement = requirement;
        this.cardinality = cardinality;
        this.order = order;
        this.keys = Sets.immutableEnumSet(Arrays.asList(keys));
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public Set<Key> getKeys() {
        return keys;
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public String toString() {
        final String name = attributeType.getName() + ":";
        final String requirementString = "[" + requirement.getExternalName() + "]";
        final String cardinalityString = "[" + cardinality.getName() + "]";
        final String keyString;

        if (keys.isEmpty()) {
            keyString = "[ ]";
        } else {
            keyString = "[" + StringUtils.join(keys, '/') + " key]";
        }

        return String.format("%1$-16s%2$-13s%3$-13s%4$s", name, requirementString, cardinalityString, keyString);
    }
}
