package net.ripe.db.whois.query.executor;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class VersionQueryExecutor implements QueryExecutor {
    public static final Set<ObjectType> NO_VERSION_HISTORY_FOR = Sets.immutableEnumSet(ObjectType.PERSON, ObjectType.ROLE);

    private final static String VERSION_HEADER = "rev#";
    private final static String DATE_HEADER = "Date";
    private final static String OPERATION_HEADER = "Op.";

    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    private final VersionDao versionDao;
    private final BasicSourceContext sourceContext;

    @Autowired
    public VersionQueryExecutor(final BasicSourceContext sourceContext, final VersionDao versionDao) {
        this.versionDao = versionDao;
        this.sourceContext = sourceContext;
    }

    @Override
    public boolean isAclSupported() {
        return false;
    }

    @Override
    public boolean supports(final Query query) {
        return query.isVersionList() || query.isObjectVersion() || query.isVersionDiff();
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        final Iterable<? extends ResponseObject> responseObjects = getResponseObjects(query);

        for (final ResponseObject responseObject : decorate(query, responseObjects)) {
            responseHandler.handle(responseObject);
        }
    }

    private Iterable<? extends ResponseObject> decorate(final Query query, Iterable<? extends ResponseObject> responseObjects) {

        final Iterable<ResponseObject> objects = Iterables.transform(responseObjects, new Function<ResponseObject, ResponseObject>() {
            @Override
            public ResponseObject apply(final ResponseObject input) {
                if (input instanceof RpslObject) {
                    ResponseObject filtered = FILTER_EMAIL_FUNCTION.apply(FILTER_AUTH_FUNCTION.apply((RpslObject) input));
                    if (query.isObjectVersion()) {
                        filtered = new VersionWithRpslResponseObject((RpslObject) filtered, query.getObjectVersion());
                    }
                    return filtered;
                }
                return input;
            }
        });

        if (Iterables.isEmpty(objects)) {
            return Collections.singletonList(new MessageObject(QueryMessages.noResults(sourceContext.getCurrentSource().getName())));
        }
        return objects;
    }

    // TODO: [AH] make this streaming, too; objects could have thousands of versions
    private Iterable<? extends ResponseObject> getResponseObjects(final Query query) {
        Collection<VersionLookupResult> versionLookupResults = getVersionInfo(query);

        // common & sanity checks
        if (versionLookupResults.isEmpty()) {
            return Collections.emptyList();
        }

        final String searchKey = query.getSearchValue();
        final List<ResponseObject> results = new ArrayList<>();
        for (VersionLookupResult versionLookupResult : versionLookupResults) {
            final ObjectType objectType = versionLookupResult.getObjectType();

            if (NO_VERSION_HISTORY_FOR.contains(objectType)) {
                results.add(new MessageObject(QueryMessages.versionPersonRole(objectType.getName().toUpperCase(), searchKey)));
                continue;
            }

            final List<VersionInfo> versionInfos = versionLookupResult.getVersionInfos();
            final VersionDateTime lastDeletionTimestamp = versionLookupResult.getLastDeletionTimestamp();
            if (versionInfos.isEmpty() && lastDeletionTimestamp != null) {
                results.add(new MessageObject(QueryMessages.versionListStart(objectType.getName().toUpperCase(), searchKey)));
                results.add(new DeletedVersionResponseObject(lastDeletionTimestamp, objectType, searchKey));
                continue;
            }

            final int version = query.getObjectVersion();
            final int[] versions = query.getObjectVersions();

            if (version > versionInfos.size() || versions[0] > versionInfos.size() || versions[1] > versionInfos.size()) {
                results.add(new MessageObject(QueryMessages.versionOutOfRange(versionInfos.size())));
                continue;
            }

            // all good, dispatch
            if (query.isVersionList()) {
                Iterables.addAll(results, getAllVersions(versionLookupResult, searchKey));
            } else if (query.isVersionDiff()) {
                Iterables.addAll(results, getVersionDiffs(versionLookupResult, versions));
            } else {
                Iterables.addAll(results, getVersion(versionLookupResult, version));
            }
        }
        return results;
    }

    private Iterable<? extends ResponseObject> getAllVersions(final VersionLookupResult res, final String searchKey) {
        final ObjectType objectType = res.getObjectType();
        final List<ResponseObject> messages = Lists.newArrayList();
        messages.add(new MessageObject(QueryMessages.versionListStart(objectType.getName().toUpperCase(), searchKey)));

        final VersionDateTime lastDeletionTimestamp = res.getLastDeletionTimestamp();
        final String pkey = res.getPkey();
        if (lastDeletionTimestamp != null) {
            messages.add(new DeletedVersionResponseObject(lastDeletionTimestamp, objectType, pkey));
        }

        final List<VersionInfo> versionInfos = res.getVersionInfos();
        int versionPadding = getPadding(versionInfos);

        messages.add(new MessageObject(String.format("\n%-" + versionPadding + "s  %-16s  %-7s\n", VERSION_HEADER, DATE_HEADER, OPERATION_HEADER)));

        for (int i = 0; i < versionInfos.size(); i++) {
            final VersionInfo versionInfo = versionInfos.get(i);
            messages.add(new VersionResponseObject(versionPadding, versionInfo.getOperation(), i + 1, versionInfo.getTimestamp(), objectType, pkey));
        }

        messages.add(new MessageObject(""));

        return messages;
    }

    private Iterable<? extends ResponseObject> getVersion(final VersionLookupResult res, final int version) {
        final List<VersionInfo> versionInfos = res.getVersionInfos();
        final VersionInfo info = versionInfos.get(version - 1);
        final RpslObject rpslObject = versionDao.getRpslObject(info);

        return Lists.newArrayList(
                new MessageObject(QueryMessages.versionInformation(version,
                        (version == versionInfos.size()),
                        rpslObject.getKey(),
                        info.getOperation() == Operation.UPDATE ? "UPDATE" : "DELETE",   // TODO: [AH] Operation is overloaded/abused (DAO + different interpretations per module)
                        info.getTimestamp())),
                rpslObject
        );
    }

    private Iterable<? extends ResponseObject> getVersionDiffs(final VersionLookupResult res, final int[] versions) {
        final List<VersionInfo> versionInfos = res.getVersionInfos();
        final RpslObject firstObject = filter(versionDao.getRpslObject(versionInfos.get(versions[0] - 1)));
        final RpslObject secondObject = filter(versionDao.getRpslObject(versionInfos.get(versions[1] - 1)));

        return Lists.newArrayList(
                new MessageObject(QueryMessages.versionDifferenceHeader(versions[0], versions[1], firstObject.getKey())),
                new MessageObject(RpslObjectFilter.diff(firstObject, secondObject)));
    }

    private Collection<ObjectType> getObjectType(final Query query) {
        if (query.hasObjectTypesSpecified()) {
            return query.getObjectTypes();
        }

        // if user did not specify object type filter, we only return the error message 'no person/role history allowed' if there would be no other match
        final Set<ObjectType> objectType = versionDao.getObjectType(query.getSearchValue());
        final Sets.SetView<ObjectType> noPersonal = Sets.difference(objectType, NO_VERSION_HISTORY_FOR);

        return noPersonal.isEmpty() ? objectType : noPersonal;
    }

    public Collection<VersionLookupResult> getVersionInfo(final Query query) {
        List<VersionLookupResult> versionLookupResults = new ArrayList<>();
        for (ObjectType type : getObjectType(query)) {
            final VersionLookupResult versionLookupResult = versionDao.findByKey(type, query.getSearchValue());
            if (versionLookupResult != null) {
                versionLookupResults.add(versionLookupResult);
            }
        }

        return versionLookupResults;
    }

    private int getPadding(List<VersionInfo> versionInfos) {
        // Minimum is the column header size
        int versionPadding = String.valueOf(versionInfos.size()).length();
        if (versionPadding < VERSION_HEADER.length()) {
            versionPadding = VERSION_HEADER.length();
        }
        return versionPadding;
    }

    private RpslObject filter(final RpslObject rpslObject) {
        return FILTER_AUTH_FUNCTION.apply(
                FILTER_EMAIL_FUNCTION.apply(rpslObject));
    }
}
