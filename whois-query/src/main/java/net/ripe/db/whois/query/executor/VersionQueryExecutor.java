package net.ripe.db.whois.query.executor;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.dao.VersionDao;
import net.ripe.db.whois.query.dao.VersionInfo;
import net.ripe.db.whois.query.domain.*;
import net.ripe.db.whois.query.planner.VersionResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO [AK] This thing has become unmaintainable, so refactor is in place
@Component
public class VersionQueryExecutor implements QueryExecutor {
    private final VersionDao versionDao;
    private final VersionResponseDecorator versionResponseDecorator;
    private final static String VERSION_HEADER = "rev#";
    private final static String DATE_HEADER = "Date";
    private final static String OPERATION_HEADER = "Op.";

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
        return query.isVersionList() || query.isObjectVersion();
    }

    @Override
    public void execute(final Query query, final ResponseHandler responseHandler) {
        final Iterable<? extends ResponseObject> responseObjects = query.isVersionList() ? getAllVersions(query) : getVersion(query);

        for (final ResponseObject responseObject : versionResponseDecorator.getResponse(responseObjects)) {
            if (query.isObjectVersion() && responseObject instanceof RpslObject) {
                final VersionWithRpslResponseObject withVersion = new VersionWithRpslResponseObject((RpslObject) responseObject, query.getObjectVersion());
                responseHandler.handle(withVersion);
            } else {
                responseHandler.handle(responseObject);
            }
        }
    }

    private Iterable<? extends ResponseObject> getAllVersions(final Query query) {
        final List<VersionInfo> versions = getVersionInfo(query);
        List<VersionInfo> concern = filterVersionInfo(versions);
        if (concern.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ResponseObject> messages = Lists.newArrayList();
        final VersionInfo firstVersionInfo = concern.get(0);
        final ObjectType objectType = firstVersionInfo.getObjectType();

        if (objectType == ObjectType.PERSON || objectType == ObjectType.ROLE) {
            return Collections.singletonList(new MessageObject(QueryMessages.versionPersonRole(objectType.getName().toUpperCase(), query.getSearchValue())));
        }

        messages.add(new MessageObject(QueryMessages.versionListHeader(objectType.getName().toUpperCase(), query.getCleanSearchValue())));

        if (firstVersionInfo.getOperation() == Operation.DELETE) {
            messages.add(new DeletedVersionResponseObject(firstVersionInfo.getTimestamp(), firstVersionInfo.getObjectType(), firstVersionInfo.getKey()));
            if (concern.size() == 1) {
                return messages;
            }
            concern = concern.subList(1, concern.size());
        }

        // Minimum is the column header size
        int versionPadding = String.valueOf(concern.size()).length();
        if (versionPadding < VERSION_HEADER.length()) {
            versionPadding = VERSION_HEADER.length();
        }

        messages.add(new MessageObject(String.format("\n%-" + versionPadding + "s  %-16s  %-7s\n", VERSION_HEADER, DATE_HEADER, OPERATION_HEADER)));

        for (int i = 0; i < concern.size(); i++) {
            final VersionInfo versionInfo = concern.get(i);
            messages.add(new VersionResponseObject(versionPadding, versionInfo.getOperation(), i + 1, versionInfo.getTimestamp(), versionInfo.getObjectType(), versionInfo.getKey()));
        }

        messages.add(new MessageObject(""));

        return messages;
    }

    private List<VersionInfo> filterVersionInfo(List<VersionInfo> versions) {
        if (versions.isEmpty()) {
            return Collections.emptyList();
        }
        for (int i = versions.size() - 1; i >= 0; i--) {
            if (versions.get(i).getOperation() == Operation.DELETE) {
                return versions.subList(i, versions.size());
            }
        }
        return versions;
    }

    private Iterable<? extends ResponseObject> getVersion(final Query query) {
        final int version = query.getObjectVersion();
        List<VersionInfo> concern = filterVersionInfo(getVersionInfo(query));

        if (!concern.isEmpty() && concern.get(0).getOperation() == Operation.DELETE) {
            if (concern.size() == 1) {
                return Collections.singletonList(new MessageObject(QueryMessages.versionDeleted(concern.get(0).getTimestamp().toString())));
            }
            concern = concern.subList(1, concern.size());
        }

        if (version < 1 || version > concern.size()) {
            return Collections.singletonList(new MessageObject(QueryMessages.versionOutOfRange(concern.size())));
        }

        final VersionInfo info = concern.get(version - 1);
        final RpslObject rpslObject = versionDao.getRpslObject(info);

        if (rpslObject.getType() == ObjectType.PERSON || rpslObject.getType() == ObjectType.ROLE) {
            return Collections.singletonList(new MessageObject(QueryMessages.versionPersonRole(rpslObject.getType().getName().toUpperCase(), query.getSearchValue())));
        }

        return Lists.newArrayList(
                new MessageObject(QueryMessages.versionInformation(version, (version == concern.size()), rpslObject.getKey(), info.getOperation(), info.getTimestamp())),
                rpslObject);
    }

    private Collection<ObjectType> getObjectType(final Query query) {
        if (query.hasObjectTypesSpecified()) {
            return query.getObjectTypes();
        }

        return versionDao.getObjectType(query.getCleanSearchValue());
    }

    public List<VersionInfo> getVersionInfo(final Query query) {
        for (ObjectType type : getObjectType(query)) {
            final List<VersionInfo> found = versionDao.findByKey(type, query.getCleanSearchValue());
            if (!found.isEmpty()) {
                return found;
            }
        }

        return Collections.emptyList();
    }
}
