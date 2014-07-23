package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class VersionDateTimeQueryExecutor implements QueryExecutor {

    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private final VersionDao versionDao;

    @Autowired
    public VersionDateTimeQueryExecutor(final VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean supports(Query query) {
        return false;
//        return query.isObjectTimestampVersion();
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

        if (CollectionUtils.isEmpty(versionInfos)) {
            return makeListWithNoResultsMessage(query.getSearchValue());
        }

        final VersionDateTime maxTimestamp = versionInfos.get(0).getTimestamp();

        final List<VersionInfo> latestVersionInfos = Lists.newArrayList(
                Iterables.filter(versionInfos, new Predicate<VersionInfo>() {
                    @Override
                    public boolean apply(@NotNull VersionInfo input) {
                        return input.getTimestamp().getTimestamp().withSecondOfMinute(0).withMillisOfSecond(0).
                                equals(maxTimestamp.getTimestamp().withSecondOfMinute(0).withMillisOfSecond(0))
                                && input.getOperation() != Operation.DELETE;
                    }
                }));

        if (latestVersionInfos.isEmpty()) {
            return makeListWithNoResultsMessage(query.getSearchValue());
        }

        if (latestVersionInfos.size() > 1) {
            results.add(new MessageObject(InternalMessages.multipleVersionsForTimestamp(latestVersionInfos.size())));
        }

        // sort in reverse order, so that first item is the object with the highest timestamp.
        Collections.sort(latestVersionInfos, Collections.reverseOrder());

        final RpslObject rpslObject = versionDao.getRpslObject(latestVersionInfos.get(0));
        results.add(new RpslObjectWithTimestamp(
                decorateRpslObject(rpslObject),
                latestVersionInfos.size(),
                Iterables.getLast(latestVersionInfos).getTimestamp()));

        return results;
    }

    private RpslObject decorateRpslObject(final RpslObject rpslObject) {
        return FILTER_EMAIL_FUNCTION.apply(FILTER_AUTH_FUNCTION.apply(rpslObject));
    }

    private Collection<? extends ResponseObject> makeListWithNoResultsMessage(String key) {
        return Collections.singletonList(new MessageObject(InternalMessages.noVersion(key)));
    }
}
