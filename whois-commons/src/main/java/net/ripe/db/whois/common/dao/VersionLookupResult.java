package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.query.VersionDateTime;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

@Immutable
public class VersionLookupResult {
    private final List<VersionInfo> versionInfos;
    private final ObjectType objectType;
    private final String pkey;

    private final VersionDateTime lastDeletionTimestamp;

    public VersionLookupResult(List<VersionInfo> daoLookupResults, ObjectType objectType, String pkey) {
        this.pkey = pkey;
        this.objectType = objectType;

        for (int i = daoLookupResults.size() - 1; i >= 0; i--) {
            if (daoLookupResults.get(i).getOperation() == Operation.DELETE) {
                versionInfos = Collections.unmodifiableList(daoLookupResults.subList(i + 1, daoLookupResults.size()));  // could be empty
                lastDeletionTimestamp = daoLookupResults.get(i).getTimestamp();
                return;
            }
        }

        versionInfos = Collections.unmodifiableList(daoLookupResults);
        lastDeletionTimestamp = null;
    }

    public int getVersionIdFor(RpslObjectUpdateInfo updateInfo) {
        final int objectId = updateInfo.getObjectId();
        final int sequenceId = updateInfo.getSequenceId();

        for (int i = versionInfos.size() - 1; i >= 0; i--) {
            if (versionInfos.get(i).getObjectId() == objectId && versionInfos.get(i).getSequenceId() == sequenceId) {
                return i + 1;
            }
        }
        throw new VersionVanishedException("Update not found in version lookup result: " + updateInfo);
    }

    public List<VersionInfo> getVersionInfos() {
        return versionInfos;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getPkey() {
        return pkey;
    }

    public VersionDateTime getLastDeletionTimestamp() {
        return lastDeletionTimestamp;
    }
}
