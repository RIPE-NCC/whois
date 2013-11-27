package net.ripe.db.whois.query.planner;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.executor.decorators.FilterPersonalDecorator;
import net.ripe.db.whois.query.executor.decorators.FilterPlaceholdersDecorator;
import net.ripe.db.whois.query.executor.decorators.FilterTagsDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

// TODO [AK] Wrap related response objects (messages + rpsl) in a single response object

// TODO [AK] Merge into SearchQueryExecutor

/**
 * Converts a query into an query result. Typically queries results are stored as proxies
 * and object data is loaded as the result is being written to the client (lazy evaluation).
 */
@Component
public class RpslResponseDecorator {
    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private final RpslObjectDao rpslObjectDao;
    private final FilterPersonalDecorator filterPersonalDecorator;
    private final SourceContext sourceContext;
    private final BriefAbuseCFunction briefAbuseCFunction;
    private final DummifyFunction dummifyFunction;
    private final SyntaxFilterFunction validSyntaxFilterFunction;
    private final SyntaxFilterFunction invalidSyntaxFilterFunction;
    private final FilterTagsDecorator filterTagsDecorator;
    private final FilterPlaceholdersDecorator filterPlaceholdersDecorator;
    private final AbuseCInfoDecorator abuseCInfoDecorator;
    private final Set<PrimaryObjectDecorator> decorators;

    @Autowired
    public RpslResponseDecorator(final RpslObjectDao rpslObjectDao,
                                 final FilterPersonalDecorator filterPersonalDecorator,
                                 final SourceContext sourceContext,
                                 final AbuseCFinder abuseCFinder,
                                 final DummifyFunction dummifyFunction,
                                 final FilterTagsDecorator filterTagsDecorator,
                                 final FilterPlaceholdersDecorator filterPlaceholdersDecorator,
                                 final AbuseCInfoDecorator abuseCInfoDecorator,
                                 final PrimaryObjectDecorator... decorators) {
        this.rpslObjectDao = rpslObjectDao;
        this.filterPersonalDecorator = filterPersonalDecorator;
        this.sourceContext = sourceContext;
        this.dummifyFunction = dummifyFunction;
        this.abuseCInfoDecorator = abuseCInfoDecorator;
        this.validSyntaxFilterFunction = new SyntaxFilterFunction(true);
        this.invalidSyntaxFilterFunction = new SyntaxFilterFunction(false);
        this.filterTagsDecorator = filterTagsDecorator;
        this.filterPlaceholdersDecorator = filterPlaceholdersDecorator;
        this.briefAbuseCFunction = new BriefAbuseCFunction(abuseCFinder);
        this.decorators = Sets.newHashSet(decorators);
    }

    public Iterable<? extends ResponseObject> getResponse(final Query query, Iterable<? extends ResponseObject> result) {
        Iterable<? extends ResponseObject> decoratedResult = filterPlaceholdersDecorator.decorate(query, result);

        decoratedResult = filterPlaceholdersDecorator.decorate(query, decoratedResult);
        decoratedResult = dummify(decoratedResult);

        decoratedResult = groupRelatedObjects(query, decoratedResult);
        decoratedResult = filterTagsDecorator.decorate(query, decoratedResult);
        decoratedResult = filterPersonalDecorator.decorate(query, decoratedResult);
        decoratedResult = abuseCInfoDecorator.decorate(query, decoratedResult);

        decoratedResult = applySyntaxFilter(query, decoratedResult);
        decoratedResult = filterEmail(query, decoratedResult);
        decoratedResult = filterAuth(query, decoratedResult);

        decoratedResult = applyOutputFilters(query, decoratedResult);

        return decoratedResult;
    }

    private Iterable<? extends ResponseObject> applySyntaxFilter(final Query query, final Iterable<? extends ResponseObject> result) {
        if (query.isValidSyntax()) {
            return Iterables.concat(Iterables.transform(result, validSyntaxFilterFunction));
        }
        if (query.isNoValidSyntax()) {
            return Iterables.concat(Iterables.transform(result, invalidSyntaxFilterFunction));
        }

        return result;
    }

    private Iterable<? extends ResponseObject> dummify(final Iterable<? extends ResponseObject> result) {
        if (sourceContext.isDummificationRequired()) {
            return Iterables.filter(Iterables.transform(result, dummifyFunction), Predicates.notNull());
        }

        return result;
    }

    private Iterable<? extends ResponseObject> groupRelatedObjects(final Query query, Iterable<? extends ResponseObject> primaryObjects) {
        final GroupFunction groupFunction = getGroupFunction(query);
        if (groupFunction == null) {
            return primaryObjects;
        }

        final Iterable<ResponseObject> groupInline = Iterables.concat(Iterables.transform(primaryObjects, groupFunction));

        return Iterables.concat(
                groupInline,
                groupFunction.getGroupedAfter());
    }

    @CheckForNull
    private GroupFunction getGroupFunction(final Query query) {
        if (query.isPrimaryObjectsOnly()) {
            return null;
        }

        if (query.isGrouping()) {
            return new GroupRelatedFunction(rpslObjectDao, query, decorators);
        }

        return new GroupObjectTypesFunction(rpslObjectDao, query, decorators);
    }

    private Iterable<? extends ResponseObject> filterAuth(Query query, final Iterable<? extends ResponseObject> objects) {
        List<String> passwords = query.getPasswords();
        final FilterAuthFunction filterAuthFunction = CollectionUtils.isEmpty(passwords) ? FILTER_AUTH_FUNCTION : new FilterAuthFunction(passwords);

        return Iterables.transform(objects, new Function<ResponseObject, ResponseObject>() {
            @Nullable
            @Override
            public ResponseObject apply(final ResponseObject input) {
                if (input instanceof RpslObject) {
                    return filterAuthFunction.apply((RpslObject) input);
                }

                return input;
            }
        });
    }

    private Iterable<? extends ResponseObject> filterEmail(final Query query, final Iterable<? extends ResponseObject> groupedObjects) {
        if (!sourceContext.isAcl() || !query.isFiltered() || query.isBriefAbuseContact()) {
            return groupedObjects;
        }

        return new IterableTransformer<ResponseObject>(groupedObjects) {
            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject)) {
                    result.add(input);
                } else {
                    result.add(FILTER_EMAIL_FUNCTION.apply((RpslObject) input));
                }

            }
        }.setHeader(new MessageObject(QueryMessages.outputFilterNotice()));
    }

    private Iterable<? extends ResponseObject> applyOutputFilters(final Query query, final Iterable<? extends ResponseObject> objects) {
        if (query.isShortHand()) {
            return Iterables.transform(objects, new ToShorthandFunction());
        }

        if (query.isBriefAbuseContact()) {
            return Iterables.filter(Iterables.transform(objects, briefAbuseCFunction), Predicates.notNull());
        }

        if (query.isKeysOnly()) {
            return Iterables.concat(
                    Collections.singletonList(new MessageObject(QueryMessages.primaryKeysOnlyNotice())),
                    Iterables.transform(objects, new ToKeysFunction()));
        }

        return objects;
    }
}
