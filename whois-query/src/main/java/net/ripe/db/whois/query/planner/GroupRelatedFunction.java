package net.ripe.db.whois.query.planner;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.query.Query;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

class GroupRelatedFunction implements GroupFunction {
    private final RpslObjectDao rpslObjectDao;
    private final Set<PrimaryObjectDecorator> decorators;
    private final Query query;

    public GroupRelatedFunction(final RpslObjectDao rpslObjectDao, final Query query, final Set<PrimaryObjectDecorator> decorators) {
        this.rpslObjectDao = rpslObjectDao;
        this.decorators = decorators;
        this.query = query;
    }

    @Override
    public Iterable<ResponseObject> apply(final ResponseObject input) {
        if (input instanceof RpslObject) {
            Iterable<ResponseObject> result = Arrays.asList(new MessageObject(QueryMessages.relatedTo(((RpslObject) input).getKey())), input);

            final SortedSet<RpslObjectInfo> relatedTo = Sets.newTreeSet();
            for (final PrimaryObjectDecorator decorator : decorators) {
                if (decorator.appliesToQuery(query)) {
                    relatedTo.addAll(decorator.decorate(query, (RpslObject) input));
                }
            }

            result = Iterables.concat(result, CollectionHelper.iterateProxy(rpslObjectDao, relatedTo));

            return result;
        }

        return Collections.singletonList(input);
    }

    @Override
    public Iterable<ResponseObject> getGroupedAfter() {
        return Collections.emptySet();
    }
}
