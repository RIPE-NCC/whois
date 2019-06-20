package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Function;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

@ThreadSafe
@Named
public class FilterNicHandleFunction implements Function<RpslObject, RpslObject>  {

    @Override
    public RpslObject apply(@Nullable RpslObject rpslObject) {
        return new RpslObjectBuilder(rpslObject)
                .removeAttributeType(AttributeType.ADMIN_C)
                .removeAttributeType(AttributeType.TECH_C)
                .removeAttributeType(AttributeType.AUTHOR)
                .removeAttributeType(AttributeType.PING_HDL)
                .removeAttributeType(AttributeType.ZONE_C)
                .get();
    }

}
