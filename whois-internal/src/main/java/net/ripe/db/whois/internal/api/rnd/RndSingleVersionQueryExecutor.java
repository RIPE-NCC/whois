package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class RndSingleVersionQueryExecutor implements QueryExecutor {

    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private final VersionDao versionDao;
    private final BasicSourceContext sourceContext;

    @Autowired
    public RndSingleVersionQueryExecutor (final BasicSourceContext sourceContext,  final VersionDao versionDao) {
        this.versionDao = versionDao;
        this.sourceContext = sourceContext;
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean supports(Query query) {
        return query.isObjectTimestampVersion();
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        for (final ResponseObject responseObject : getResponseObjects(query)) {
            responseHandler.handle(responseObject);
        }
    }


    private Iterable<? extends ResponseObject> getResponseObjects(final Query query) {
        final List<ResponseObject> results = new ArrayList<>();

        final List<VersionInfo> versionInfos = versionDao.getVersionsBeforeTimestamp(
                query.getObjectTypes().iterator().next(), // internal REST API will allow only one object type
                query.getSearchValue(),
                query.getObjectTimestamp());

        if (CollectionUtils.isEmpty(versionInfos)){
            return makeListWithNoResultsMessage();
        }

        final VersionDateTime maxTimestamp = versionInfos.get(0).getTimestamp();
        final List<VersionInfo> latestVersionInfos = Lists.newArrayList(
                Iterables.filter(versionInfos, new Predicate<VersionInfo>() {
                    @Override
                    public boolean apply(@Nullable VersionInfo input) {
                        return input.getTimestamp().equals(maxTimestamp) || input.getOperation() != Operation.DELETE;
                    }
                }));

        Collections.sort(latestVersionInfos);

        if (latestVersionInfos.isEmpty()) {
            return makeListWithNoResultsMessage();
        }

        if (latestVersionInfos.size() > 1) {
            results.add(new MessageObject(multipleVersionsForTimestamp(latestVersionInfos.size())));
        }

        final RpslObject rpslObject = versionDao.getRpslObject(Iterables.getLast(latestVersionInfos));
        results.add(new RpslObjectWithTimestamp(
                        decorateRpslObject(rpslObject),
                        latestVersionInfos.size(),
                        Iterables.getLast(latestVersionInfos).getTimestamp()));

        return results;
    }

    private RpslObject decorateRpslObject(final RpslObject rpslObject) {
        return FILTER_EMAIL_FUNCTION.apply(FILTER_AUTH_FUNCTION.apply(rpslObject));
    }

    public static Message multipleVersionsForTimestamp(final int count) {
        return new Message(Messages.Type.WARNING, "%s versions for timestamp found.", count);
    }

    private Collection makeListWithNoResultsMessage() {
        return Collections.singletonList(new MessageObject(QueryMessages.noResults(sourceContext.getCurrentSource().getName())));
    }
}
