package net.ripe.db.whois.query.executor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
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

import java.util.Set;

@Component
public class SearchQueryExecutor implements QueryExecutor {
    private final SourceContext sourceContext;
    private final RpslObjectSearcher rpslObjectSearcher;
    private final RpslResponseDecorator rpslResponseDecorator;

    @Autowired
    public SearchQueryExecutor(final SourceContext sourceContext,
                               final RpslObjectSearcher rpslObjectSearcher,
                               final RpslResponseDecorator rpslResponseDecorator) {
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
                || query.isObjectVersion()
                || query.isVersionDiff()) {
            return false;
        }

        return true;
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        //TODO intentional lack of RsplObject in results should not give below error (-> add+implement Query.shouldProduceRpslObjects())
        boolean noResults = true;

        final Set<Source> sources = getSources(query);
        for (final Source source : sources) {
            try {
                sourceContext.setCurrent(source);
                final Iterable<? extends ResponseObject> searchResults = rpslObjectSearcher.search(query);

                for (final ResponseObject responseObject : rpslResponseDecorator.getResponse(query, searchResults)) {

                    responseHandler.handle(responseObject);

                    if (!(responseObject instanceof MessageObject)) {
                        noResults = false;
                    }
                }
            } catch (IllegalSourceException e) {
                responseHandler.handle(new MessageObject(QueryMessages.unknownSource(source.getName())));
                noResults = false;
            } finally {
                sourceContext.removeCurrentSource();
            }
        }

        if (noResults) {
            responseHandler.handle(new MessageObject(QueryMessages.noResults(Joiner.on(',').join(Iterables.transform(sources, new Function<Source, String>() {
                @Override
                public String apply(final Source input) {
                    return input.getName().toUpperCase();
                }
            })))));
        }
    }

    private Set<Source> getSources(final Query query) {
        final Set<Source> sources = Sets.newLinkedHashSet();

        if (query.isResource()) {
            for (CIString source : sourceContext.getGrsSourceNames()) {
                sources.add(Source.slave(source));
            }
        }

        if (query.isAllSources()) {
            for (CIString source : sourceContext.getAllSourceNames()) {
                if (!sourceContext.isVirtual(source)) {
                    sources.add(Source.slave(source));
                }
            }
        }

        if (query.hasSources()) {
            for (String source : query.getSources()) {
                sources.add(Source.slave(source));
            }
        } else {
            if (!sourceContext.getAdditionalSourceNames().isEmpty()) {
                sources.add(sourceContext.getWhoisSlaveSource());
                sources.addAll(Sets.newLinkedHashSet(Iterables.transform(sourceContext.getAdditionalSourceNames(), new Function<CIString, Source>() {
                    @Override
                    public Source apply(final CIString input) {
                        return Source.slave(input);
                    }
                })));
            }
        }

        if (sources.isEmpty()) {
            sources.add(sourceContext.getWhoisSlaveSource());
        }

        return sources;
    }
}
