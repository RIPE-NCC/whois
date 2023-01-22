package net.ripe.db.whois.common.rpsl.transform;

import jakarta.inject.Named;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.function.Function;

@ThreadSafe
@Named
public class FilterPersonalDataFunction implements Function<RpslObject, RpslObject> {

    @Override
    public RpslObject apply(@Nullable final RpslObject rpslObject) {
        return new RpslObjectBuilder(rpslObject)
                .removeAttributeType(AttributeType.ADDRESS)
                .removeAttributeType(AttributeType.ADMIN_C)
                .removeAttributeType(AttributeType.AUTHOR)
                .removeAttributeType(AttributeType.E_MAIL)
                .removeAttributeType(AttributeType.NOTIFY)
                .removeAttributeType(AttributeType.PING_HDL)
                .removeAttributeType(AttributeType.TECH_C)
                .removeAttributeType(AttributeType.ZONE_C)
                .get();
    }

}
