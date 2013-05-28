package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.CheckForNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.*;

class GrsDao {
    private final Logger logger;
    private final DateTimeProvider dateTimeProvider;
    private final CIString sourceName;
    private final SourceContext sourceContext;

    private JdbcTemplate masterJdbcTemplate;
    private JdbcTemplate slaveJdbcTemplate;

    GrsDao(final Logger logger, final DateTimeProvider dateTimeProvider, final CIString sourceName, final SourceContext sourceContext) {
        this.logger = logger;
        this.dateTimeProvider = dateTimeProvider;
        this.sourceName = sourceName;
        this.sourceContext = sourceContext;
    }

    private void ensureInitialized() {
        if (masterJdbcTemplate == null) {
            try {
                masterJdbcTemplate = sourceContext.getSourceConfiguration(Source.master(sourceName)).getJdbcTemplate();
                slaveJdbcTemplate = sourceContext.getSourceConfiguration(Source.slave(sourceName)).getJdbcTemplate();
                JdbcRpslObjectOperations.sanityCheck(masterJdbcTemplate);
                JdbcRpslObjectOperations.sanityCheck(slaveJdbcTemplate);
            } catch (IllegalSourceException e) {
                throw new IllegalArgumentException(String.format("Source not configured: %s", e.getSource()));
            }
        }
    }

    void cleanDatabase() {
        ensureInitialized();
        JdbcRpslObjectOperations.truncateTables(masterJdbcTemplate);
    }

    List<Integer> getCurrentObjectIds() {
        ensureInitialized();
        return slaveJdbcTemplate.queryForList("" +
                "SELECT object_id " +
                "FROM last " +
                "WHERE sequence_id != 0",
                Integer.class);
    }

    @CheckForNull
    GrsObjectInfo get(final int objectId) {
        ensureInitialized();
        return CollectionHelper.uniqueResult(masterJdbcTemplate.query("" +
                "SELECT object_id, sequence_id, object " +
                "  FROM last " +
                "  WHERE object_id = ?" +
                "  AND sequence_id != 0 ",
                new RowMapper<GrsObjectInfo>() {
                    @Override
                    public GrsObjectInfo mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new GrsObjectInfo(
                                rs.getInt(1),
                                rs.getInt(2),
                                RpslObject.parse(rs.getString(3))
                        );
                    }
                },
                objectId
        ));
    }

    @CheckForNull
    GrsObjectInfo find(final String pkey, final ObjectType objectType) {
        ensureInitialized();
        return CollectionHelper.uniqueResult(masterJdbcTemplate.query("" +
                "SELECT object_id, sequence_id, object " +
                "  FROM last " +
                "  WHERE object_type = ?" +
                "  AND pkey = ?" +
                "  AND sequence_id != 0 ",
                new RowMapper<GrsObjectInfo>() {
                    @Override
                    public GrsObjectInfo mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new GrsObjectInfo(
                                rs.getInt(1),
                                rs.getInt(2),
                                RpslObject.parse(rs.getString(3))
                        );
                    }
                },
                ObjectTypeIds.getId(objectType),
                pkey
        ));
    }

    UpdateResult createObject(final RpslObject rpslObject) {
        ensureInitialized();
        final RpslObjectUpdateInfo rpslObjectInfo = insertIntoLastAndUpdateSerials(dateTimeProvider, masterJdbcTemplate, rpslObject);
        final Set<CIString> missingReferences = insertIntoTablesIgnoreMissing(masterJdbcTemplate, rpslObjectInfo, rpslObject);

        return new UpdateResult(rpslObjectInfo, missingReferences);
    }

    UpdateResult updateObject(final GrsObjectInfo grsObjectInfo, final RpslObject rpslObject) {
        ensureInitialized();
        final RpslObjectUpdateInfo rpslObjectInfo = grsObjectInfo.createUpdateInfo();

        deleteFromTables(masterJdbcTemplate, rpslObjectInfo);
        final Set<CIString> missingReferences = insertIntoTablesIgnoreMissing(masterJdbcTemplate, rpslObjectInfo, rpslObject);
        updateLastAndUpdateSerials(dateTimeProvider, masterJdbcTemplate, rpslObjectInfo, rpslObject);

        return new UpdateResult(rpslObjectInfo, missingReferences);
    }

    @Transactional
    Set<CIString> updateIndexes(final int objectId) {
        ensureInitialized();
        final GrsObjectInfo grsObjectInfo = get(objectId);
        if (grsObjectInfo == null) {
            logger.warn("Unable to update index for unexisting object with id: {}", objectId);
            return Collections.emptySet();
        }

        final RpslObjectInfo rpslObjectInfo = grsObjectInfo.createUpdateInfo();
        final RpslObject rpslObject = grsObjectInfo.getRpslObject();

        deleteFromTables(masterJdbcTemplate, rpslObjectInfo);
        final Set<CIString> missingReferences = insertIntoTablesIgnoreMissing(masterJdbcTemplate, rpslObjectInfo, rpslObject);
        if (!missingReferences.isEmpty()) {
            logger.debug("Ignore missing references for object with id {}: {}", objectId, missingReferences);
        }

        return missingReferences;
    }

    void deleteObject(final int objectId) {
        ensureInitialized();
        final GrsObjectInfo grsObjectInfo = get(objectId);
        if (grsObjectInfo == null) {
            logger.warn("Unable to delete unexisting object with id: {}", objectId);
            return;
        }

        final RpslObjectUpdateInfo rpslObjectInfo = grsObjectInfo.createUpdateInfo();
        deleteFromTables(masterJdbcTemplate, rpslObjectInfo);
        deleteFromLastAndUpdateSerials(dateTimeProvider, masterJdbcTemplate, rpslObjectInfo);
    }

    static class UpdateResult {
        private final int objectId;
        private final boolean hasMissingReferences;

        private UpdateResult(final RpslObjectInfo updateInfo, final Set<CIString> missingReferences) {
            this.objectId = updateInfo.getObjectId();
            this.hasMissingReferences = !missingReferences.isEmpty();
        }

        public int getObjectId() {
            return objectId;
        }

        public boolean hasMissingReferences() {
            return hasMissingReferences;
        }
    }
}
