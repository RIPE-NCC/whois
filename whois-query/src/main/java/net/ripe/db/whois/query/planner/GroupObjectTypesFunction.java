package net.ripe.db.whois.query.planner;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

public final class GroupObjectTypesFunction implements GroupFunction {
    private final RpslObjectDao rpslObjectDao;
    private final Query query;
    private final Set<PrimaryObjectDecorator> decorators;
    private final SortedSet<RpslObjectInfo> relatedTo;

    public GroupObjectTypesFunction(final RpslObjectDao rpslObjectDao, final Query query, final Set<PrimaryObjectDecorator> decorators) {
        this.rpslObjectDao = rpslObjectDao;
        this.query = query;
        this.decorators = decorators;
        this.relatedTo = Sets.newTreeSet();
    }

    @Override
    public Iterable<ResponseObject> apply(final ResponseObject input) {
        if (input instanceof RpslObject) {
            for (PrimaryObjectDecorator decorator : decorators) {
                if (decorator.appliesToQuery(query)) {
                    relatedTo.addAll(decorator.decorate((RpslObject) input));
                }
            }
        }

        return Collections.singletonList(input);
    }

    @Override
    public Iterable<ResponseObject> getGroupedAfter() {
        return CollectionHelper.iterateProxy(rpslObjectDao, relatedTo);
    }
}
