package net.ripe.db.whois.query.executor.decorators;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Set;

@Component
public class FilterPersonalDecorator implements ResponseDecorator {
    private static final Set<ObjectType> PERSONAL = Sets.newEnumSet(Sets.newHashSet(ObjectType.PERSON, ObjectType.ROLE), ObjectType.class);

    @Override
    public Iterable<? extends ResponseObject> decorate(final Query query, final Iterable<? extends ResponseObject> input) {
        if (query.hasOption(QueryFlag.SHOW_PERSONAL) || !query.hasOption(QueryFlag.NO_PERSONAL)) {
            return input;
        }

        return new IterableTransformer<ResponseObject>(input) {
            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject) || !PERSONAL.contains(((RpslObject) input).getType())) {
                    result.add(input);
                }
            }
        }.setHeader(new MessageObject(QueryMessages.noPersonal()));
    }
}
