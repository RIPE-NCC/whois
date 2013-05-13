package net.ripe.db.whois.query.planner;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;

// TODO [AK] Merge into version query executor
@Component
public class VersionResponseDecorator {
    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();
    private final SourceContext sourceContext;

    @Autowired
    public VersionResponseDecorator(final SourceContext sourceContext) {
        this.sourceContext = sourceContext;
    }

    public Iterable<? extends ResponseObject> getResponse(final Iterable<? extends ResponseObject> result) {
        final Iterable<ResponseObject> responseObjects = Iterables.transform(result, new Function<ResponseObject, ResponseObject>() {
            @Nullable
            @Override
            public ResponseObject apply(final ResponseObject input) {
                if (input instanceof RpslObject) {
                    return FILTER_EMAIL_FUNCTION.apply(FILTER_AUTH_FUNCTION.apply((RpslObject) input));
                }
                return input;
            }
        });

        if (Iterables.isEmpty(responseObjects)) {
            return Collections.singletonList(new MessageObject(QueryMessages.noResults(sourceContext.getCurrentSource().getName())));
        }

        return responseObjects;
    }
}
