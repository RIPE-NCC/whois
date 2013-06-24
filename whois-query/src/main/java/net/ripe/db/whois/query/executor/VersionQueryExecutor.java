package net.ripe.db.whois.query.executor;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.VersionDateTime;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterAuthFunction;
import net.ripe.db.whois.common.rpsl.transform.FilterEmailFunction;
import net.ripe.db.whois.query.domain.*;
import net.ripe.db.whois.query.planner.VersionResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class VersionQueryExecutor implements QueryExecutor {
    private final VersionDao versionDao;
    private final VersionResponseDecorator versionResponseDecorator;
    private final static String VERSION_HEADER = "rev#";
    private final static String DATE_HEADER = "Date";
    private final static String OPERATION_HEADER = "Op.";
    private static final FilterEmailFunction FILTER_EMAIL_FUNCTION = new FilterEmailFunction();
    private static final FilterAuthFunction FILTER_AUTH_FUNCTION = new FilterAuthFunction();

    @Autowired
    public VersionQueryExecutor(final VersionResponseDecorator versionResponseDecorator, final VersionDao versionDao) {
        this.versionResponseDecorator = versionResponseDecorator;
        this.versionDao = versionDao;
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

        // TODO: [AH] refactor this spaghetti
        for (final ResponseObject responseObject : versionResponseDecorator.getResponse(responseObjects)) {
            if (query.isObjectVersion() && responseObject instanceof RpslObject) {
                final VersionWithRpslResponseObject withVersion = new VersionWithRpslResponseObject((RpslObject) responseObject, query.getObjectVersion());
                responseHandler.handle(withVersion);
            } else {
                responseHandler.handle(responseObject);
            }
        }
    }

    private Iterable<? extends ResponseObject> getResponseObjects(final Query query) {
        VersionLookupResult res = getVersionInfo(query);

        if (res == null) {
            return Collections.emptyList();
        }

        final ObjectType objectType = res.getObjectType();
        if (objectType == ObjectType.PERSON || objectType == ObjectType.ROLE) {
            return Collections.singletonList(new MessageObject(QueryMessages.versionPersonRole(objectType.getName().toUpperCase(), query.getSearchValue())));
        }

        final List<VersionInfo> versionInfos = res.getVersionInfos();
        final VersionDateTime lastDeletionTimestamp = res.getLastDeletionTimestamp();
        if (versionInfos.isEmpty() && lastDeletionTimestamp != null) {
            return Collections.singletonList(new MessageObject(QueryMessages.versionDeleted(lastDeletionTimestamp.toString())));
        }

        if (query.isVersionList()) {
            return getAllVersions(res, query);
        } else if (query.isVersionDiff()) {
            return getVersionDiffs(res, query);
        } else {
            return getVersion(res, query);
        }
    }

    private Iterable<? extends ResponseObject> getAllVersions(final VersionLookupResult res, final Query query) {
        final ObjectType objectType = res.getObjectType();
        final List<ResponseObject> messages = Lists.newArrayList();
        messages.add(new MessageObject(QueryMessages.versionListHeader(objectType.getName().toUpperCase(), query.getCleanSearchValue())));

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

    private Iterable<? extends ResponseObject> getVersion(final VersionLookupResult res, final Query query) {
        final List<VersionInfo> versionInfos = res.getVersionInfos();
        final int version = query.getObjectVersion();

        if (version < 1 || version > versionInfos.size()) {
            return Collections.singletonList(new MessageObject(QueryMessages.versionOutOfRange(versionInfos.size())));
        }

        final VersionInfo info = versionInfos.get(version - 1);
        final RpslObject rpslObject = versionDao.getRpslObject(info);

        return Lists.newArrayList(
                new MessageObject(QueryMessages.versionInformation(version, (version == versionInfos.size()), rpslObject.getKey(), info.getOperation(), info.getTimestamp())),
                rpslObject);
    }

    private Iterable<? extends ResponseObject> getVersionDiffs(final VersionLookupResult res, final Query query) {
        final List<VersionInfo> versionInfos = res.getVersionInfos();
        final int[] versions = query.getObjectVersions();

        if ((versions[0] < 1 || versions[0] > versionInfos.size()) ||
                (versions[1] < 1 || versions[1] > versionInfos.size())) {
            return Collections.singletonList(new MessageObject(QueryMessages.versionOutOfRange(versionInfos.size())));
        }

        final VersionInfo firstInfo = versionInfos.get(versions[0] - 1);
        final RpslObject firstObject = filter(versionDao.getRpslObject(firstInfo));

        final VersionInfo secondInfo = versionInfos.get(versions[1] - 1);
        final RpslObject secondObject = filter(versionDao.getRpslObject(secondInfo));

        return Lists.newArrayList(
                new MessageObject(QueryMessages.versionDifferenceHeader(versions[0], versions[1], firstObject.getKey())),
                new MessageObject(secondObject.diff(firstObject)));
    }

    private Collection<ObjectType> getObjectType(final Query query) {
        if (query.hasObjectTypesSpecified()) {
            return query.getObjectTypes();
        }

        return versionDao.getObjectType(query.getCleanSearchValue());
    }

    @CheckForNull
    public VersionLookupResult getVersionInfo(final Query query) {
        for (ObjectType type : getObjectType(query)) {
            final VersionLookupResult found = versionDao.findByKey(type, query.getCleanSearchValue());
            if (found != null) {
                return found;
            }
        }

        return null;
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
