package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DummifierRC extends DummifierCurrent {

    @Override
    public RpslObject dummify(final int version, final RpslObject rpslObject) {
        final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());

        for (int i = 0; i < attributes.size(); i++) {
            RpslAttribute replacement = attributes.get(i);

            replacement = dummifyOrgName(rpslObject, replacement);
            replacement = dummifyDescr(replacement);
            replacement = dummifyRemarks(replacement);
            replacement = dummifyOwner(replacement);

            attributes.set(i, replacement);
        }


        return super.dummify(version, new RpslObject(rpslObject, attributes));
    }

    private RpslAttribute dummifyOrgName(final RpslObject rpslObject, final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.ORG_NAME) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.ORG_NAME, String.format("Dummy org-name for %s", rpslObject.getKey().toUpperCase()));
    }

    private RpslAttribute dummifyDescr(final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.DESCR) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.DESCR, "***");
    }

    private RpslAttribute dummifyRemarks(final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.REMARKS) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.REMARKS, "***");
    }

    private RpslAttribute dummifyOwner(final RpslAttribute rpslAttribute) {
        if (rpslAttribute.getType() != AttributeType.OWNER) {
            return rpslAttribute;
        }

        return new RpslAttribute(AttributeType.OWNER, "***");
    }
}
