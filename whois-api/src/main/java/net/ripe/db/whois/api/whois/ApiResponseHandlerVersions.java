package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;

import java.util.List;

class ApiResponseHandlerVersions extends ApiResponseHandler {
    final List<VersionResponseObject> versionObjects = Lists.newArrayList();
    final List<DeletedVersionResponseObject> deletedObjects = Lists.newArrayList();
    VersionWithRpslResponseObject versionWithRpslResponseObject;

    public List<VersionResponseObject> getVersionObjects() {
        return versionObjects;
    }

    public List<DeletedVersionResponseObject> getDeletedObjects() {
        return deletedObjects;
    }

    public VersionWithRpslResponseObject getVersionWithRpslResponseObject() {
        return versionWithRpslResponseObject;
    }

    @Override
    public void handle(final ResponseObject responseObject) {
        if (responseObject instanceof VersionWithRpslResponseObject) {
            versionWithRpslResponseObject = (VersionWithRpslResponseObject) responseObject;
        }

        if (responseObject instanceof VersionResponseObject) {
            versionObjects.add((VersionResponseObject) responseObject);
        }

        if (responseObject instanceof DeletedVersionResponseObject) {
            deletedObjects.add((DeletedVersionResponseObject) responseObject);
        }
    }
}
