package net.ripe.db.whois.query.executor;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.planner.RpslResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class SearchQueryExecutor implements QueryExecutor {
    private final SourceContext sourceContext;
    private final RpslObjectSearcher rpslObjectSearcher;
    private final RpslResponseDecorator rpslResponseDecorator;

    @Autowired
    public SearchQueryExecutor(final SourceContext sourceContext, final RpslObjectSearcher rpslObjectSearcher, final RpslResponseDecorator rpslResponseDecorator) {
        this.sourceContext = sourceContext;
        this.rpslObjectSearcher = rpslObjectSearcher;
        this.rpslResponseDecorator = rpslResponseDecorator;
    }

    @Override
    public boolean isAclSupported() {
        return true;
    }

    @Override
    public boolean supports(final Query query) {
        if ((!query.hasOptions() && query.isHelp())
                || query.isSystemInfo()
                || query.isTemplate()
                || query.isVerbose()
                || query.isVersionList()
                || query.isObjectVersion()) {
            return false;
        }

        if (query.isInverse()) {
            return true;
        }

        final Collection<ObjectType> requestedTypes = query.getObjectTypes();
        for (final ObjectType objectType : requestedTypes) {
            if (query.matchesObjectType(objectType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        for (final Source source : getSources(query)) {
            try {
                sourceContext.setCurrent(source);
                final Iterable<? extends ResponseObject> searchResults = rpslObjectSearcher.search(query);
                boolean noResults = true;

                for (final ResponseObject responseObject : rpslResponseDecorator.getResponse(query, searchResults)) {

                    // TODO: [AH] make sure responseHandler implementation can handle executionHandler worker threads pushing data (think of suspend-on-write, buffer overflow, slow connections, etc...)
                    responseHandler.handle(responseObject);

                    if (responseObject instanceof RpslObject) {
                        noResults = false;
                    }
                }

                if (noResults) {
                    responseHandler.handle(new MessageObject(QueryMessages.noResults(source.getName())));
                }

            } catch (IllegalSourceException e) {
                responseHandler.handle(new MessageObject(QueryMessages.unknownSource(source.getName()) + "\n"));
            } finally {
                sourceContext.removeCurrentSource();
            }
        }
    }

    private Set<Source> getSources(final Query query) {
        final Set<Source> sources = Sets.newLinkedHashSet();

        if (query.isAllSources()) {
            sources.addAll(Sets.newLinkedHashSet(Iterables.transform(sourceContext.getGrsSourceNames(), new Function<CIString, Source>() {
                @Override
                public Source apply(final CIString input) {
                    return Source.slave(input);
                }
            })));
        }

        if (query.hasSources()) {
            sources.addAll(Sets.newLinkedHashSet(Iterables.transform(query.getSources(), new Function<String, Source>() {
                @Override
                public Source apply(final String input) {
                    return Source.slave(input);
                }
            })));
        }

        if (sources.isEmpty()) {
            sources.add(sourceContext.getWhoisSlaveSource());
        }

        return sources;
    }
}
