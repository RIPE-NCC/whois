package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TimestampsMode;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

//TODO [TP] remove when timestamps are always on
@Named
public class TimestampFilterFunction implements Function<ResponseObject, ResponseObject> {
    private final TimestampsMode timestampsMode;

    @Inject
    public TimestampFilterFunction(final TimestampsMode timestampsMode) {
        this.timestampsMode = timestampsMode;
    }

    @Nullable
    @Override
    public ResponseObject apply(final ResponseObject input) {
        if (timestampsMode.isTimestampsOff() && input instanceof RpslObject) {
            return new RpslObjectBuilder((RpslObject) input).removeAttributeTypes(
                    Collections.unmodifiableList(Lists.newArrayList(AttributeType.CREATED, AttributeType.LAST_MODIFIED))).get();
        }

        return input;
    }
}
