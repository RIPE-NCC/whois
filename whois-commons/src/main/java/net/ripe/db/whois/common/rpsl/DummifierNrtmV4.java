package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component("dummifierNrtmV4")
public class DummifierNrtmV4 extends DummifierNrtm {

    static final Map<AttributeType, String> DUMMIFICATION_REPLACEMENTS = Maps.newEnumMap(AttributeType.class);
    static {
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.CERTIF, "Dummified");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.DESCR, "Dummified");
        DUMMIFICATION_REPLACEMENTS.put(AttributeType.REMARKS, "Dummified");
    }

    private final Set<ObjectType> writtenPlaceHolders = Sets.newHashSet();

    public RpslObject dummify(final RpslObject rpslObject) {
        final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());
        dummifyAttributes(attributes, rpslObject.getKey());

        return dummify(4, new RpslObject(rpslObject, attributes));
    }

    public boolean shouldCreatePlaceHolder(final RpslObject rpslObject) {
        final ObjectType objectType = rpslObject.getType();
        return writtenPlaceHolders.add(objectType) && (objectType.equals(ObjectType.ROLE) || objectType.equals(ObjectType.PERSON));
    }

    public boolean isAllowed(final RpslObject rpslObject) {
        return isAllowed(4, rpslObject);
    }

    private void dummifyAttributes(final List<RpslAttribute> attributes, final CIString key) {
        final Set<AttributeType> seenAttributes = Sets.newHashSet();

        for (int i = 0; i < attributes.size(); i++) {
            final RpslAttribute attribute = attributes.get(i);

            final AttributeType attributeType = attribute.getType();
            final String replacementValue = DUMMIFICATION_REPLACEMENTS.get(attributeType);
            if (replacementValue == null) {
                continue;
            }

            final RpslAttribute replacement;
            if (seenAttributes.add(attributeType)) {
                replacement = new RpslAttribute(attribute.getKey(), String.format(replacementValue, key));
            } else {
                replacement = null;
            }

            attributes.set(i, replacement);
        }

        attributes.removeIf(Objects::isNull);
    }
}
