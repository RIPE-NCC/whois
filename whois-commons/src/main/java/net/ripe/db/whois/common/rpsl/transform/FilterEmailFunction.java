package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;

import javax.annotation.concurrent.ThreadSafe;
import javax.validation.constraints.NotNull;
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

    @Override @NotNull
    public RpslObject apply(RpslObject rpslObject) {
        RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject).removeAttributeTypes(filterAttributes);
        return rpslObject.size() == builder.size() ? rpslObject : RpslObjectFilter.setFiltered(builder).get();
    }
}
