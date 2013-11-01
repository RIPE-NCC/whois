package net.ripe.db.whois.query.planner;


import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.executor.decorators.ResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;

@Component
class AbuseCInfoDecorator implements ResponseDecorator {
    private final AbuseCFinder abuseCFinder;
    private final SourceContext sourceContext;

    @Autowired
    public AbuseCInfoDecorator(final AbuseCFinder abuseCFinder, SourceContext sourceContext) {
        this.abuseCFinder = abuseCFinder;
        this.sourceContext = sourceContext;
    }

    @Override
    public Iterable<? extends ResponseObject> decorate(Query query, Iterable<? extends ResponseObject> input) {
        if (query.isBriefAbuseContact() || !sourceContext.isMain()) {
            return input;
        }

        return new IterableTransformer<ResponseObject>(input) {
            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject)) {
                    result.add(input);
                    return;
                }

                final RpslObject object = (RpslObject) input;

                final Map<CIString, CIString> abuseContacts = abuseCFinder.getAbuseContacts(object);

                if (abuseContacts.isEmpty()) {
                    result.add(new MessageObject(QueryMessages.abuseCNotRegistered(object.getKey())));
                } else {
                    result.add(new MessageObject(QueryMessages.abuseCShown(abuseContacts)));
                }

                result.add(input);
                return;
            }
        };
    }
}
