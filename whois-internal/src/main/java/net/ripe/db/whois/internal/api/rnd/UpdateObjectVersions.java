package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceUpdateDao;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class UpdateObjectVersions {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateObjectVersions.class);

    private final ObjectReferenceUpdateDao objectReferenceUpdateDao;
    private final VersionDao versionDao;
    private final JdbcTemplate jdbcTemplate;

    private final AtomicBoolean inProgress = new AtomicBoolean();

    @Autowired
    public UpdateObjectVersions(
            final ObjectReferenceUpdateDao objectReferenceUpdateDao,
            final VersionDao versionDao,
            @Qualifier("whoisUpdateMasterDataSource") final DataSource dataSource) {
        this.objectReferenceUpdateDao = objectReferenceUpdateDao;
        this.versionDao = versionDao;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // periodically update the object_version and object_reference tables
    @Scheduled(fixedDelay = 60 * 1000)
    @Transactional
    public void run() {
        if (!inProgress.compareAndSet(false, true)) {
            LOGGER.info("run already in progress, returning...");
        }

        try {
            final List<ObjectVersion> versions = updateObjectVersionsTable(objectReferenceUpdateDao.getLatestVersionTimestamp());
            updateOutgoingReferences(versions);
            updateIncomingReferences(versions);
        } finally {
            if (!inProgress.compareAndSet(true, false)) {
                LOGGER.warn("unexpectedly not in progress?");
            }
        }
    }

    private List<ObjectVersion> updateObjectVersionsTable(final long timestamp) {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final List<ObjectData> updates = Lists.newArrayList();

        jdbcTemplate.query(
                    "(SELECT pkey, object_type, timestamp, object_id, sequence_id FROM history WHERE timestamp > ?) " +
                    "UNION ALL " +
                    "(SELECT pkey, object_type, timestamp, object_id, sequence_id FROM last WHERE timestamp > ?)",
                new Object[] {timestamp, timestamp},
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        try {
                            final String pkey = rs.getString(1);
                            final ObjectType type = ObjectTypeIds.getType(rs.getInt(2));
                            final long timestamp = rs.getLong(3);
                            final int objectId = rs.getInt(4);
                            final int sequenceId = rs.getInt(5);

                            updates.add(new ObjectData(pkey, type, timestamp, objectId, sequenceId));
                        } catch (IllegalArgumentException e) {
                            // ignore - invalid object type
                        }
                    }
                });

        Collections.sort(updates);

        for (final ObjectData objectData : updates) {

            final List<ObjectVersion> versions = objectReferenceUpdateDao.getVersions(objectData.pkey, objectData.objectType);

            if (objectData.sequenceId == 0) {
                // delete - add end timestamp to existing version
                if (!versions.isEmpty()) {
                    final ObjectVersion previousVersion = versions.get(versions.size() - 1);
                    objectReferenceUpdateDao.updateVersionToTimestamp(previousVersion, objectData.timestamp);
                }
            } else {
                if (!versions.isEmpty()) {
                    // update - add end timestamp to existing version
                    final ObjectVersion previousVersion = versions.get(versions.size() - 1);
                    objectReferenceUpdateDao.updateVersionToTimestamp(previousVersion, objectData.timestamp);
                }

                // create new version
                final ObjectVersion newVersion = new ObjectVersion(
                        0,
                        objectData.objectType,
                        objectData.pkey,
                        objectData.timestamp,
                        0,
                        versions.isEmpty() ? 1 : versions.get(versions.size() - 1).getRevision() + 1);
                objectReferenceUpdateDao.createVersion(newVersion);
            }
        }

        LOGGER.info("Updated versions table in {}", stopwatch.stop());

        return objectReferenceUpdateDao.getVersions(timestamp);
    }

    private void updateOutgoingReferences(final List<ObjectVersion> versions) {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        for (final ObjectVersion version : versions) {

            final RpslObject rpslObject = findRpslObject(version);

            if (rpslObject != null) {
                // for each object version, find all outgoing object references for the duration of that revision
                // (revision may have been subsequently deleted)

                // find all outgoing references from the object (removing duplicates)

                Map<CIString,Set<ObjectType>> references = Maps.newHashMap();
                for (RpslAttribute attribute : rpslObject.getAttributes()) {
                    for (ObjectType objectType : attribute.getType().getReferences()) {
                        if (references.containsKey(attribute.getCleanValue())) {
                            references.get(attribute.getCleanValue()).add(objectType);
                        } else {
                            references.put(attribute.getCleanValue(), Sets.newHashSet(objectType));
                        }
                    }
                }

                // resolve references (all versions of the reference over the duration of the source object)

                for (Map.Entry<CIString, Set<ObjectType>> entry : references.entrySet()) {
                    for (ObjectType entryValue : entry.getValue()) {

                        final List<ObjectVersion> entryVersions = objectReferenceUpdateDao.getVersions(
                                entry.getKey().toString(),
                                entryValue,
                                version.getFromDate(),
                                version.getToDate());

                        for (ObjectVersion entryVersion : entryVersions) {
                            objectReferenceUpdateDao.createReference(version, entryVersion);
                        }
                    }
                }
            }
        }

        LOGGER.info("Updated outgoing references table in {}", stopwatch.stop());
    }

    private void updateIncomingReferences(final List<ObjectVersion> versions) {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        for (final ObjectVersion version : versions) {

            if (version.getRevision() > 1) {

                // find previous revision
                final ObjectVersion previousVersion = objectReferenceUpdateDao.getVersion(
                        version.getType(),
                        version.getPkey().toString(),
                        version.getRevision() - 1);

                // duplicate versions for the new version
                for (ObjectVersion incomingReference : objectReferenceUpdateDao.findIncomingReferences(previousVersion)) {

                    if ((incomingReference.getToDate() == null) ||
                            (incomingReference.getToDate().getMillis() > version.getFromDate().getMillis())) {
                        LOGGER.info("Create reference from {} to {}", incomingReference.getVersionId(), version.getVersionId());
                        objectReferenceUpdateDao.createReference(incomingReference, version);
                    }
                }
            }
        }

        LOGGER.info("Updated outgoing references table in {}", stopwatch.stop());
    }

    // map from a specific revision of an object, to the rpsl object itself
    @Nullable
    private RpslObject findRpslObject(final ObjectVersion objectVersion) {
        final VersionLookupResult result = versionDao.findByKey(objectVersion.getType(), objectVersion.getPkey().toString());
        if (result != null) {
            int revision = 0;
            for (VersionInfo versionInfo : result.getAllVersions()) {
                if (versionInfo.getSequenceId() == 0) {
                    // deletes are not revisions
                    continue;
                }
                revision++;
                if (revision == objectVersion.getRevision()) {
                    return versionDao.getRpslObject(versionInfo);
                }
            }
        }

        throw new IllegalStateException("Couldn't find RPSL object for " + objectVersion.getPkey() + " revision=" + objectVersion.getRevision());
    }

    ///

    private static class ObjectData implements Comparable<ObjectData> {
        private final String pkey;
        private final ObjectType objectType;
        private final long timestamp;
        private final int objectId;
        private final int sequenceId;

        public ObjectData(final String pkey, final ObjectType objectType, final long timestamp, final int objectId, final int sequenceId) {
            this.pkey = pkey;
            this.objectType = objectType;
            this.timestamp = timestamp;
            this.objectId = objectId;
            this.sequenceId = sequenceId;
        }

        @Override
        public int compareTo(final ObjectData o) {
            final int c1 = Long.compare(timestamp, o.timestamp);
            if (c1 != 0) {
                // order by date
                return c1;
            }

            final int c2 = Integer.compare(objectId, o.objectId);
            if (c2 != 0) {
                // order by objectId
                return c2;
            }

            // order by sequenceId
            return Integer.compare(sequenceId, o.sequenceId);
        }
    }
}
