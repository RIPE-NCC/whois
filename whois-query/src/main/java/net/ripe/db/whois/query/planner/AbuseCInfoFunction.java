package net.ripe.db.whois.query.planner;


import com.google.common.base.Function;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

// TODO: [AH] it's really inefficient to make an Iterable<ResponseObject> for each ResponseObject; should use IterableTransformer
class AbuseCInfoFunction implements Function<ResponseObject, Iterable<? extends ResponseObject>> {
    private static final Set<ObjectType> OBJECT_TYPES = Sets.newHashSet(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);
    private final AbuseCFinder abuseCFinder;

    public AbuseCInfoFunction(final AbuseCFinder abuseCFinder) {
        this.abuseCFinder = abuseCFinder;
    }

    @Override
    public Iterable<? extends ResponseObject> apply(@Nullable final ResponseObject input) {
        if (input instanceof RpslObject) {
            final RpslObject object = (RpslObject) input;

            if (OBJECT_TYPES.contains(object.getType())) {
                final Map<CIString, CIString> abuseContacts = abuseCFinder.getAbuseContacts(object);

                if (abuseContacts.isEmpty()) {
                    return Arrays.asList(new MessageObject(QueryMessages.abuseCNotRegistered(object.getKey())), input);
                }  else {
                    return Arrays.asList(new MessageObject(QueryMessages.abuseCShown(abuseContacts)), input);
                }
            }
        }
        return Collections.singletonList(input);
    }
}
