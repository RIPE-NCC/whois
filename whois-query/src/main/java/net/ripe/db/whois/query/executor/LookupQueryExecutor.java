package net.ripe.db.whois.query.executor;


import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.planner.RpslResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class LookupQueryExecutor implements QueryExecutor {

    private final SourceContext sourceContext;
    private RpslObjectDao rpslObjectDao;
    private final RpslResponseDecorator rpslResponseDecorator;

    @Autowired
    public LookupQueryExecutor(final SourceContext sourceContext,
                               final RpslObjectDao rpslObjectDao,
                               final RpslResponseDecorator rpslResponseDecorator) {
        this.sourceContext = sourceContext;
        this.rpslObjectDao = rpslObjectDao;
        this.rpslResponseDecorator = rpslResponseDecorator;
    }


    @Override
    public boolean isAclSupported() {
        return true;
    }

    @Override
    public boolean supports(Query query) {
        if (query.isMatchPrimaryKeyOnly() && query.via(Query.Origin.REST)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void execute(Query query, ResponseHandler responseHandler) {
        Source source = getSource(query);
        ObjectType type = query.getObjectTypes().iterator().next();

        try {
            sourceContext.setCurrent(getSource(query));
            final RpslObject rpslObject = rpslObjectDao.getByKey(type, query.getSearchValue());

            // TODO handler
        } catch (IllegalSourceException e) {
            responseHandler.handle(new MessageObject(QueryMessages.unknownSource(source.getName())));
        } catch (IncorrectResultSizeDataAccessException ex) {
            if (ex.getActualSize() == 0) {
                responseHandler.handle(new MessageObject(QueryMessages.noResults(source.getName().toUpperCase())));
            } else {
                responseHandler.handle(new MessageObject(format("Multiple objects found for type %s and key %s",
                        type.getName(),
                        query.getSearchValue())));
            }
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    private Source getSource(Query query) {
        // TODO grab the source

        return null;
    }
}
