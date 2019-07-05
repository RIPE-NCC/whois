package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Set;

public class Latin1ConversionResult {

    private final RpslObject rpslObject;
    private final boolean globalSubstitution;
    private Set<RpslAttribute> substitutedAttributes;

    public Latin1ConversionResult(final RpslObject rpslObject,
                                  final boolean globalSubstitution,
                                  final Set<RpslAttribute> substitutedAttributes) {
        this.rpslObject = rpslObject;
        this.globalSubstitution = globalSubstitution;
        this.substitutedAttributes = substitutedAttributes;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }

    public boolean isGlobalSubstitution() {
        return globalSubstitution;
    }

    public Set<RpslAttribute> getSubstitutedAttributes() {
        return substitutedAttributes;
    }
}
