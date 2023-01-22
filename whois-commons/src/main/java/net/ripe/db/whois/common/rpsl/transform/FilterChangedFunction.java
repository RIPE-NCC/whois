package net.ripe.db.whois.common.rpsl.transform;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import jakarta.annotation.Nullable;
import jakarta.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;
import java.util.function.Function;

@ThreadSafe
@Named
public class FilterChangedFunction implements Function<RpslObject, RpslObject> {

    @Nullable
    @Override
    public RpslObject apply(final RpslObject rpslObject) {
        return new RpslObjectBuilder(rpslObject).removeAttributeType(AttributeType.CHANGED).get();
    }
}
