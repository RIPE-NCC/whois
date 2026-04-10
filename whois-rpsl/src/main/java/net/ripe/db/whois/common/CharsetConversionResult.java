package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Map;

public class CharsetConversionResult {

    private final RpslObject rpslObject;
    private final Map<RpslAttribute, RpslAttribute> substitutedAttributes;

    public CharsetConversionResult(final RpslObject rpslObject, final Map<RpslAttribute, RpslAttribute> substitutedAttributes) {
        this.rpslObject = rpslObject;
        this.substitutedAttributes = substitutedAttributes;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }

    public Map<RpslAttribute, RpslAttribute> getSubstitutedAttributes() {
        return substitutedAttributes;
    }
}
