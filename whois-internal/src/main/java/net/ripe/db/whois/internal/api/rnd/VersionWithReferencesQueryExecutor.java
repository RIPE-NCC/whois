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
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
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
public class VersionWithReferencesQueryExecutor implements QueryExecutor {

    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private final ObjectReferenceDao objectReferenceDao;
    private final VersionDao versionDao;

    @Autowired
    public VersionWithReferencesQueryExecutor(final ObjectReferenceDao objectReferenceDao, final VersionDao versionDao) {
        this.objectReferenceDao = objectReferenceDao;
        this.versionDao = versionDao;
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean supports(final Query query) {
        return query.isObjectInternalVersion();
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        for (final ResponseObject responseObject : getResponseObjects(query)) {
            responseHandler.handle(responseObject);
        }
    }

    private Iterable<? extends ResponseObject> getResponseObjects(final Query query) {
        final List<ResponseObject> results = new ArrayList<>();


        final ObjectVersion version = objectReferenceDao.getVersion(
                query.getObjectTypes().iterator().next(), // internal REST API will allow only one object type
                query.getSearchValue(),
                query.getObjectInternalVersion());

        if (version == null) {
            return makeListWithNoResultsMessage(query.getSearchValue());
        }

        final List<VersionInfo> entriesInSameVersion = lookupRpslObjectByVersion(version);
        final RpslObject rpslObject = versionDao.getRpslObject(entriesInSameVersion.get(0));
        final List<ObjectReference> outgoing = objectReferenceDao.getOutgoing(version.getVersionId());
        final List<ObjectReference> incoming = objectReferenceDao.getIncoming(version.getVersionId());

        final RpslObjectWithTimestamp rpslObjectWithTimestamp = new RpslObjectWithTimestamp(
                decorateRpslObject(rpslObject),
                entriesInSameVersion.size(),
                new VersionDateTime(version.getInterval().getStartMillis()/1000L),
                outgoing,
                incoming);

        results.add(rpslObjectWithTimestamp);

        return results;
    }

    private List<VersionInfo> lookupRpslObjectByVersion(final ObjectVersion version) {
       //TODO: [TP] A list is returned because there may be multiple modifications of an object on the same timestamp.

        final List<VersionInfo> versionInfos = versionDao.getVersionsForTimestamp(
                version.getType(),
                version.getPkey().toString(),
                version.getInterval().getStart().getMillis());

        if (CollectionUtils.isEmpty(versionInfos)) {
            throw new IllegalStateException("There should be one or more objects");
        }

        final VersionDateTime maxTimestamp = versionInfos.get(0).getTimestamp();

        final List<VersionInfo> latestVersionInfos = Lists.newArrayList(
                Iterables.filter(versionInfos, new Predicate<VersionInfo>() {
                    @Override
                    public boolean apply(@NotNull VersionInfo input) {
                        return input.getTimestamp().getTimestamp().
                                equals(maxTimestamp.getTimestamp())
                                && input.getOperation() != Operation.DELETE;
                    }
                }));

        if (latestVersionInfos.isEmpty()) {
            throw new IllegalStateException("There should be one or more objects");
        }

        // sort in reverse order, so that first item is the object with the highest timestamp.
        Collections.sort(latestVersionInfos, Collections.reverseOrder());

        return latestVersionInfos;

    }

    private RpslObject decorateRpslObject(final RpslObject rpslObject) {
        return FILTER_EMAIL_FUNCTION.apply(FILTER_AUTH_FUNCTION.apply(rpslObject));
    }

    private Collection<? extends ResponseObject> makeListWithNoResultsMessage(final String key) {
        return Collections.singletonList(new MessageObject(InternalMessages.noVersion(key)));
    }
}
