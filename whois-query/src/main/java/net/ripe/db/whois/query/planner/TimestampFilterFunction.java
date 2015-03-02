package net.ripe.db.whois.query.planner;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TimestampsMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;

@Component
public class TimestampFilterFunction implements Function<ResponseObject, ResponseObject> {

    @Autowired
    private final TimestampsMode timestampsMode;

    @Autowired
    public TimestampFilterFunction(final TimestampsMode timestampsMode) {
        this.timestampsMode = timestampsMode;
    }

    @Nullable
    @Override
    public ResponseObject apply(final ResponseObject input) {

        if (timestampsMode.isTimestampsOff() && input instanceof RpslObject){
            final RpslObject object = (RpslObject) input;
            return new RpslObjectBuilder(object).removeAttributeTypes(
                    Collections.unmodifiableList(Lists.newArrayList(AttributeType.CREATED, AttributeType.LAST_MODIFIED))).get();
        }

        return input;
    }

}
