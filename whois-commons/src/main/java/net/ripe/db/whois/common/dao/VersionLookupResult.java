package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.VersionDateTime;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

@Immutable
public class VersionLookupResult {
    private final List<VersionInfo> mostRecentlyCreatedVersions;
    private final List<VersionInfo> allVersions;
    private final ObjectType objectType;
    private final String pkey;

    private final VersionDateTime lastDeletionTimestamp;

    public VersionLookupResult(final List<VersionInfo> daoLookupResults, final ObjectType objectType, final String pkey) {
        this.pkey = pkey;
        this.objectType = objectType;

        this.allVersions = Collections.unmodifiableList(daoLookupResults);

        for (int i = daoLookupResults.size() - 1; i >= 0; i--) {
            if (daoLookupResults.get(i).getOperation() == Operation.DELETE) {
                mostRecentlyCreatedVersions = Collections.unmodifiableList(daoLookupResults.subList(i + 1, daoLookupResults.size()));  // could be empty
                lastDeletionTimestamp = daoLookupResults.get(i).getTimestamp();
                return;
            }
        }

        mostRecentlyCreatedVersions = Collections.unmodifiableList(daoLookupResults);
        lastDeletionTimestamp = null;
    }

    public int getVersionIdFor(final RpslObjectUpdateInfo updateInfo) {
        final int objectId = updateInfo.getObjectId();
        final int sequenceId = updateInfo.getSequenceId();

        for (int i = mostRecentlyCreatedVersions.size() - 1; i >= 0; i--) {
            if (mostRecentlyCreatedVersions.get(i).getObjectId() == objectId && mostRecentlyCreatedVersions.get(i).getSequenceId() == sequenceId) {
                return i + 1;
            }
        }
        throw new VersionVanishedException("Update not found in version lookup result: " + updateInfo);
    }

    public List<VersionInfo> getMostRecentlyCreatedVersions() {
        return mostRecentlyCreatedVersions;
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

    public List<VersionInfo> getAllVersions() {
        return allVersions;
    }
}
