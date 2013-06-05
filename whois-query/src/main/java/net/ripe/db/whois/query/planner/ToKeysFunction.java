package net.ripe.db.whois.query.planner;

import com.google.common.base.Function;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ToKeysFunction implements Function<ResponseObject, ResponseObject> {
    @Override
    public ResponseObject apply(final @Nullable ResponseObject input) {
        if (!(input instanceof RpslObject)) {
            return input;
        }

        final RpslObject rpslObject = (RpslObject) input;

        final List<RpslAttribute> attributes = rpslObject.getAttributes();
        final List<RpslAttribute> newAttributes = new ArrayList<>(attributes.size());

        final ObjectTemplate template = ObjectTemplate.getTemplate(rpslObject.getType());
        final RpslAttribute typeAttribute = rpslObject.getTypeAttribute();
        final Set<AttributeType> keyAttributes = template.getKeyAttributes();
        if (keyAttributes.size() == 1 && keyAttributes.contains(typeAttribute.getType()) && !template.isSet()) {
            newAttributes.add(typeAttribute);
        } else {
            for (final RpslAttribute attribute : attributes) {
                final AttributeType attributeType = attribute.getType();

                if (keyAttributes.contains(attributeType) || (template.isSet() && AttributeType.MEMBERS.equals(attributeType))) {
                    newAttributes.add(attribute);
                }
            }
        }

        return new RpslAttributes(newAttributes);
    }
}
