package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

@ThreadSafe
public class FilterEmailFunction implements FilterFunction {
    private final Set<AttributeType> filterAttributes = Sets.immutableEnumSet(
            AttributeType.NOTIFY,
            AttributeType.CHANGED,
            AttributeType.REF_NFY,
            AttributeType.MNT_NFY,
            AttributeType.UPD_TO,
            AttributeType.E_MAIL);

    @Override
    public RpslObject apply(RpslObject rpslObject) {
        RpslObject filtered = RpslObjectFilter.removeAttributeTypes(rpslObject, filterAttributes);
        return filtered == rpslObject ? rpslObject : RpslObjectFilter.setFiltered(filtered);
    }
}
