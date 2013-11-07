package net.ripe.db.whois.common.domain.attrs;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;

import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.regex.Pattern;

@Immutable
public class SetObject {
    public enum Type {
        ASSET("as", ObjectType.AS_SET),
        ROUTESET("rs", ObjectType.ROUTE_SET),
        FILTERSET("fltr", ObjectType.FILTER_SET),
        PEERINGSET("prng", ObjectType.PEERING_SET),
        RTRSET("rtrs", ObjectType.RTR_SET);

        private final Pattern pattern;
        private final ObjectType objectType;
        private final String prefix;

        Type(String prefix, ObjectType objectType) {
            this.prefix = prefix;
            this.objectType = objectType;
            pattern =  Pattern.compile("(?i)^" + prefix + "-[A-Z0-9_-]*[A-Z0-9]$");
        }

        public Pattern getPattern() {
            return pattern;
        }

        @Override
        public String toString() {
            return objectType.getName();
        }

        public String getPrefix() {
            return prefix;
        }
    }

    private static final Splitter SPLITTER = Splitter.on(':');

    private Type type;
    private String value;

    public SetObject(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static SetObject parse(final Type type, final CIString value) {
        return parse(type, value.toString());
    }

    public static SetObject parse(final Type type, final String value) {
        if (value.length() > 254) {
            throw new AttributeParseException("Too long", value);
        }

        boolean hasSetName = false;
        int elementCount = 0;
        for (String element: SPLITTER.split(value)) {
            elementCount++;
            final int prefixLength = type.getPrefix().length();

            if (element.length() <= prefixLength) {
                throw new AttributeParseException(MessageFormat.format("Element '{0}' too short", element), value);
            }

            if (element.charAt(prefixLength) == '-') {
                if (!type.getPattern().matcher(element).matches()) {
                    throw new AttributeParseException(String.format("Invalid {0} element name '{1}'", type, element), value);
                }
                hasSetName = true;

            } else {
                AutNum.parse(element);
            }
        }

        if (!hasSetName) {
            if (elementCount == 1) {
                throw new AttributeParseException(MessageFormat.format("Invalid {0} name", type), value);
            } else {
                throw new AttributeParseException(MessageFormat.format("Hierarchical {0} name must include an {0} element", type), value);
            }
        }

        return new SetObject(type, value);
    }
}
