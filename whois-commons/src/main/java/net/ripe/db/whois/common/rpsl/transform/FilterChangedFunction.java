package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Function;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

@ThreadSafe
@Named
public class FilterChangedFunction implements Function<RpslObject, RpslObject> {

    @Nullable
    @Override
    public RpslObject apply(final RpslObject rpslObject) {
        return new RpslObjectBuilder(rpslObject).removeAttributeType(AttributeType.CHANGED).get();
    }
}
