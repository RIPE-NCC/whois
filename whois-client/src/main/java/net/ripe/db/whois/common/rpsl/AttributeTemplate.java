package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Set;

public class AttributeTemplate {
    public static enum Requirement {
        MANDATORY("mandatory"),
        OPTIONAL("optional"),
        GENERATED("generated");

        private final String name;

        private Requirement(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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
        final String requirementString = "[" + requirement.getName() + "]";
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
