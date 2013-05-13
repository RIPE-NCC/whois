package net.ripe.db.whois.query.executor.decorators;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class FilterPersonalDecorator implements ResponseDecorator {
    private static final Set<ObjectType> PERSONAL = Sets.newEnumSet(Sets.newHashSet(ObjectType.PERSON, ObjectType.ROLE), ObjectType.class);

    @Override
    public Iterable<? extends ResponseObject> decorate(final Query query, final Iterable<? extends ResponseObject> input) {
        if (query.hasOption(QueryFlag.SHOW_PERSONAL) || !query.hasOption(QueryFlag.NO_PERSONAL)) {
            return input;
        }

        final Iterable<? extends ResponseObject> filteredResponse = Iterables.filter(input, new Predicate<ResponseObject>() {
            @Override
            public boolean apply(final ResponseObject input) {
                return !(input instanceof RpslObject) || !PERSONAL.contains(((RpslObject) input).getType());
            }
        });

        return Iterables.concat(
                Collections.singletonList(new MessageObject(QueryMessages.noPersonal())),
                filteredResponse);
    }
}
