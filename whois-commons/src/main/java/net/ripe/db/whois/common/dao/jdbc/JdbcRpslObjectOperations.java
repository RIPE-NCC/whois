package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.AttributeTemplate;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JdbcRpslObjectOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcRpslObjectOperations.class);
    private static final int MAX_NUMBER_OF_SERIALS = 100_000_000;

    public static void insertIntoTables(final JdbcTemplate jdbcTemplate, final RpslObjectInfo rpslObjectInfo, final RpslObject rpslObject) {
        final Set<CIString> missing = insertIntoTablesIgnoreMissing(jdbcTemplate, rpslObjectInfo, rpslObject);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing references: " + missing);
        }
    }

    public static Set<CIString> insertIntoTablesIgnoreMissing(final JdbcTemplate jdbcTemplate, final RpslObjectInfo rpslObjectInfo, final RpslObject rpslObject) {
        final Set<CIString> missingReferences = Sets.newHashSet();
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObject.getType());

        final Set<AttributeType> keyAttributes = objectTemplate.getKeyAttributes();
        for (final AttributeType keyAttributeType : keyAttributes) {
            missingReferences.addAll(insertAttributeIndex(jdbcTemplate, rpslObjectInfo, rpslObject, keyAttributeType));
        }

        for (final AttributeTemplate attributeTemplate : objectTemplate.getAttributeTemplates()) {
            final AttributeType attributeType = attributeTemplate.getAttributeType();
            if (!keyAttributes.contains(attributeType)) {
                missingReferences.addAll(insertAttributeIndex(jdbcTemplate, rpslObjectInfo, rpslObject, attributeType));
            }
        }

        return missingReferences;
    }

    private static Set<CIString> insertAttributeIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo rpslObjectInfo, final RpslObject rpslObject, final AttributeType attributeType) {
        final Set<CIString> missingReferences = Sets.newHashSet();

        final IndexStrategy indexStrategy = IndexStrategies.get(attributeType);

        final Set<CIString> uniqueValues = Sets.newHashSet();
        final List<RpslAttribute> attributes = rpslObject.findAttributes(attributeType);
        for (final RpslAttribute attribute : attributes) {
            for (final CIString value : attribute.getReferenceValues()) {
                if (uniqueValues.add(value)) {
                    try {
                        final int rows = indexStrategy.addToIndex(jdbcTemplate, rpslObjectInfo, rpslObject, value.toString());
                        if (rows < 1) {
                            throw new DataIntegrityViolationException("Rows affected: " + rows);
                        }
                    } catch (IllegalArgumentException e) {
                        LOGGER.debug("Missing reference: " + value, e);
                        missingReferences.add(value);
                    }
                }
            }
        }

        return missingReferences;
    }

    public static RpslObjectUpdateInfo lookupRpslObjectUpdateInfo(final JdbcTemplate jdbcTemplate, final ObjectType type, final String pkey) {
        return jdbcTemplate.queryForObject("" +
                        "SELECT last.object_id, last.sequence_id, last.object_type " +
                        "FROM last " +
                        "WHERE last.object_type = ? AND last.pkey = ? AND last.sequence_id > 0",
                new RowMapper<RpslObjectUpdateInfo>() {
                    @Override
                    public RpslObjectUpdateInfo mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new RpslObjectUpdateInfo(rs.getInt(1), rs.getInt(2), ObjectTypeIds.getType(rs.getInt(3)), pkey);
                    }
                }, ObjectTypeIds.getId(type), pkey
        );
    }

    public static RpslObjectUpdateInfo lookupRpslObjectUpdateInfo(final JdbcTemplate jdbcTemplate, final int objectId, final String pkey) {
        return jdbcTemplate.queryForObject("" +
                        "SELECT last.object_id, last.sequence_id, last.object_type " +
                        "FROM last " +
                        "WHERE last.object_id = ? AND last.sequence_id > 0",
                new RowMapper<RpslObjectUpdateInfo>() {
                    @Override
                    public RpslObjectUpdateInfo mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new RpslObjectUpdateInfo(rs.getInt(1), rs.getInt(2), ObjectTypeIds.getType(rs.getInt(3)), pkey);
                    }
                }, objectId
        );
    }

    public static void deleteFromTables(final JdbcTemplate jdbcTemplate, final RpslObjectInfo rpslObjectInfo) {
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObjectInfo.getObjectType());

        for (AttributeTemplate attributeTemplate : objectTemplate.getAttributeTemplates()) {
            IndexStrategies.get(attributeTemplate.getAttributeType()).removeFromIndex(jdbcTemplate, rpslObjectInfo);
        }
    }

    public static void copyToHistoryAndUpdateSerials(final JdbcTemplate jdbcTemplate, final RpslObjectUpdateInfo rpslObjectInfo) {
        int rows = jdbcTemplate.update("" +
                        "INSERT INTO history " +
                        "SELECT object_id, sequence_id, timestamp, object_type, object, pkey FROM last " +
                        "WHERE object_id = ? and sequence_id = ?",
                rpslObjectInfo.getObjectId(), rpslObjectInfo.getSequenceId()
        );
        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by INSERT INTO history: " + rows);
        }

        rows = jdbcTemplate.update("" +
                        "UPDATE serials SET atlast = 0 " +
                        "WHERE object_id = ? " +
                        "AND sequence_id = ? ",
                rpslObjectInfo.getObjectId(), rpslObjectInfo.getSequenceId()
        );

        switch (rows) {
            case 0:
                // Missing entries in serials table (data was deleted incorrectly in 2004..)
                LOGGER.debug("Object id: {} is missing entry in serials table?", rpslObjectInfo.getObjectId());
                break;
            case 1:
                // expected, do nothing
                break;
            default:
                throw new DataIntegrityViolationException("Rows affected by UPDATE serials table: " + rows);
        }
    }

    public static void deleteFromLastAndUpdateSerials(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObjectUpdateInfo rpslObjectInfo) {
        deleteFromLast(dateTimeProvider, jdbcTemplate, rpslObjectInfo);
        int rows = jdbcTemplate.update("" +
                        "INSERT INTO serials (object_id, sequence_id, atlast, operation) " +
                        "VALUES (?, ?, 0, ?)",
                rpslObjectInfo.getObjectId(), rpslObjectInfo.getSequenceId() + 1, Operation.DELETE.getCode()
        );
        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by INSERT INTO serials table: " + rows);
        }
    }

    public static void deleteFromLastAndSetSerials(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObjectUpdateInfo rpslObjectInfo, final int serialId) {
        deleteFromLast(dateTimeProvider, jdbcTemplate, rpslObjectInfo);

        int rows = jdbcTemplate.update("" +
                        "INSERT INTO serials (serial_id, object_id, sequence_id, atlast, operation) " +
                        "VALUES (?, ?, ?, 0, ?)",
                serialId, rpslObjectInfo.getObjectId(), rpslObjectInfo.getSequenceId() + 1, Operation.DELETE.getCode()
        );
        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by INSERT INTO serials table: " + rows);
        }
    }

    private static void deleteFromLast(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObjectUpdateInfo rpslObjectInfo) {
        int rows = jdbcTemplate.update("" +
                        "UPDATE last SET object = '', timestamp = ?, sequence_id = 0 " +
                        "WHERE object_id = ? AND sequence_id > 0",
                now(dateTimeProvider), rpslObjectInfo.getObjectId()
        );
        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by UPDATE last table is: " + rows);
        }
    }

    public static int updateLastAndUpdateSerials(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObjectUpdateInfo rpslObjectInfo, final RpslObject object) {
        final int newSequenceId = updateLast(dateTimeProvider, jdbcTemplate, rpslObjectInfo, object);
        int rows = jdbcTemplate.update("INSERT INTO serials "
                        + " (object_id, sequence_id, atlast, operation) "
                        + " VALUES "
                        + " (?, ?, 1, ?)",
                rpslObjectInfo.getObjectId(), newSequenceId, Operation.UPDATE.getCode()
        );

        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by INSERT INTO serials table: " + rows);
        }

        return newSequenceId;
    }

    public static int updateLastAndSetSerials(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate,
                                              final RpslObjectUpdateInfo rpslObjectInfo, final RpslObject object, final int serialId) {
        final int newSequenceId = updateLast(dateTimeProvider, jdbcTemplate, rpslObjectInfo, object);
        int rows = updateSetSerials(jdbcTemplate, serialId, rpslObjectInfo.getObjectId(), Operation.UPDATE, newSequenceId);

        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by INSERT INTO serials table: " + rows);
        }

        return newSequenceId;
    }

    private static int updateLast(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObjectUpdateInfo rpslObjectInfo, final RpslObject object) {
        final int newSequenceId = rpslObjectInfo.getSequenceId() + 1;
        int rows = jdbcTemplate.update("" +
                        "UPDATE last " +
                        "SET object = ?, timestamp = ?, sequence_id = ? " +
                        "WHERE object_id = ?",
                object.toByteArray(), now(dateTimeProvider), newSequenceId, rpslObjectInfo.getObjectId()
        );
        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by UPDATE last table is: " + rows);
        }
        return newSequenceId;
    }

    public static RpslObjectUpdateInfo insertIntoLastAndUpdateSerials(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObject object) {
        final Integer objectTypeId = ObjectTypeIds.getId(object.getType());
        final String pkey = object.getKey().toString();

        final int objectId = insertIntoLast(dateTimeProvider, jdbcTemplate, object, objectTypeId, pkey);
        final int rows = jdbcTemplate.update("INSERT INTO serials "
                        + " (object_id, sequence_id, atlast, operation) "
                        + " VALUES "
                        + " (?, ?, 1, ?)",
                objectId, 1, Operation.UPDATE.getCode()
        );

        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by INSERT INTO serials table: " + rows);
        }

        return new RpslObjectUpdateInfo(objectId, 1, object.getType(), pkey);
    }

    public static RpslObjectUpdateInfo insertIntoLastAndSetSerials(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObject object, final int serialId) {
        final Integer objectTypeId = ObjectTypeIds.getId(object.getType());
        final String pkey = object.getKey().toString();

        final int objectId = insertIntoLast(dateTimeProvider, jdbcTemplate, object, objectTypeId, pkey);

        int rows = updateSetSerials(jdbcTemplate, serialId, objectId, Operation.UPDATE, 1);

        if (rows != 1) {
            throw new DataIntegrityViolationException("Rows affected by INSERT INTO serials table: " + rows);
        }

        return new RpslObjectUpdateInfo(objectId, 1, object.getType(), pkey);
    }

    private static int insertIntoLast(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final RpslObject object, final Integer objectTypeId, final String pkey) {
        // FIXME: [AH] put a unique index on (`pkey`, `object_type`) on last (and history) instead of this extra lookup
        // TODO: [ES] this query is very time consuming (>100ms) if there is a large version history for this object_type & pkey
        final int count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*)\n" +
                        "    FROM last\n" +
                        "    WHERE object_type=?\n" +
                        "    AND pkey=?\n" +
                        "    AND sequence_id > 0",
                Integer.class,
                objectTypeId, pkey
        );

        if (count != 0) {
            throw new IllegalStateException("Object with type: " + objectTypeId + " and pkey: " + pkey + " already exists");
        }

        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("last")
                .usingColumns("object", "timestamp", "sequence_id", "object_type", "pkey")
                .usingGeneratedKeyColumns("object_id")
                .executeAndReturnKey(new HashMap<String, Object>() {{
                    put("object", object.toByteArray());
                    put("timestamp", now(dateTimeProvider));
                    put("sequence_id", 1);
                    put("object_type", objectTypeId);
                    put("pkey", pkey);
                }}).intValue();
    }

    private static int updateSetSerials(final JdbcTemplate jdbcTemplate, final int serialId, final int objectId, final Operation operation, final int sequenceId) {
        return jdbcTemplate.update("INSERT INTO serials "
                        + " (serial_id, object_id, sequence_id, atlast, operation) "
                        + " VALUES "
                        + " (?, ?, ?, 1, ?)",
                serialId, objectId, sequenceId, operation.getCode()
        );
    }

    public static int now(final DateTimeProvider dateTimeProvider) {
        return (int) Timestamp.from(dateTimeProvider.getCurrentDateTime()).getValue();
    }

    public static void truncateTables(final JdbcTemplate... jdbcTemplates) {
        for (final JdbcTemplate jdbcTemplate : jdbcTemplates) {
            if (jdbcTemplate == null) {
                continue;
            }

            try {
                sanityCheck(jdbcTemplate);
            } catch (IllegalStateException e) {
                LOGGER.warn("sanityCheck failed due to {}", e.getMessage());
                return;
            }

            final List<String> statements = Lists.newArrayList();
            statements.add("SET FOREIGN_KEY_CHECKS = 0");
            statements.addAll(Lists.transform(jdbcTemplate.queryForList("SHOW TABLES", String.class), table -> String.format("TRUNCATE TABLE %s", table)));
            statements.add("SET FOREIGN_KEY_CHECKS = 1");

            jdbcTemplate.batchUpdate(statements.toArray(new String[statements.size()]));
        }
    }

    public static void loadScripts(final JdbcTemplate jdbcTemplate, final String... initSql) {
        sanityCheck(jdbcTemplate);

        final ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        final ResourceLoader resourceLoader = new DefaultResourceLoader();

        for (final String sql : initSql) {
            databasePopulator.addScript(resourceLoader.getResource(sql));
        }

        DatabasePopulatorUtils.execute(databasePopulator, jdbcTemplate.getDataSource());
    }

    public static void sanityCheck(final JdbcTemplate jdbcTemplate) {
        try {
            final String dbName = jdbcTemplate.queryForObject("SELECT database()", String.class);
            if (!dbName.matches("(?i).*_mirror_.+_grs.*") && !dbName.matches("(?i).*test.*")) {
                throw new IllegalStateException(String.format("%s has no 'test' or 'grs' in the name, exiting", dbName));
            }

            if (jdbcTemplate.queryForList("SHOW TABLES", String.class).contains("serials")) {
                if (jdbcTemplate.queryForObject("SELECT count(*) FROM serials", Integer.class) > MAX_NUMBER_OF_SERIALS) {
                    LOGGER.info("{} has more than {} serials", dbName, MAX_NUMBER_OF_SERIALS);
                }
            }
        } catch (DataAccessException e) {
            // TODO: possibly "unknown database" error
            throw new IllegalStateException(e);
        }
    }

    public static SerialRange getSerials(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.query("SELECT MIN(serial_id), MAX(serial_id) FROM serials", new ResultSetExtractor<SerialRange>() {
            @Override
            public SerialRange extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return new SerialRange(rs.getInt(1), rs.getInt(2));
            }
        });
    }

    public static RpslObject getObjectById(final JdbcTemplate jdbcTemplate, final Identifiable identifiable) {
        return getObjectById(jdbcTemplate, identifiable.getObjectId());
    }

    public static RpslObject getObjectById(final JdbcTemplate jdbcTemplate, final int objectId) {
        return jdbcTemplate.queryForObject("" +
                        "SELECT object_id, object FROM last " +
                        "WHERE object_id = ? " +
                        "AND sequence_id != 0",
                new RpslObjectRowMapper(),
                objectId
        );
    }

    @CheckForNull
    public static SerialEntry getSerialEntry(final JdbcTemplate jdbcTemplate, final int serialId) {
        try {
            return getSerialEntryWithBlobs(jdbcTemplate, serialId);
        } catch (EmptyResultDataAccessException e) {
            LOGGER.debug("SerialDao.getById({})", serialId, e);
            return null;
        }
    }


    @CheckForNull
    public static Map<Integer, Integer> getMaxSerialIdWithObjectCount(final JdbcTemplate jdbcTemplate) {
        final Integer maxSerialId =  jdbcTemplate.queryForObject("SELECT MAX(serial_id) FROM serials", Integer.class);
        final Integer countInDb = jdbcTemplate.queryForObject( "SELECT count(*) FROM last WHERE sequence_id > 0", Integer.class);

        return Collections.singletonMap(maxSerialId,countInDb);
    }

    public static List<SerialEntry> getSerialEntriesBetween(final JdbcTemplate jdbcTemplate, final int serialIdFrom, final int serialIdTo) {
        try {
            return getSerialEntryWithBlobsSinceSerialForNrtm4(jdbcTemplate, serialIdFrom, serialIdTo);
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("SerialDao.getSerialEntriesSince({}, {}) returned no rows", serialIdFrom, serialIdTo, e);
            return List.of();
        }
    }

    @CheckForNull
    public static SerialEntry getSerialEntryForNrtm(final JdbcTemplate jdbcTemplate, final int serialId) {
        try {
            return getSerialEntryWithBlobsForNrtm(jdbcTemplate, serialId);
        } catch (EmptyResultDataAccessException e) {
            LOGGER.debug("SerialDao.getByIdForNrtm({})", serialId, e);
            return null;
        }
    }

    @CheckForNull
    public static Integer getAgeOfExactOrNextExistingSerial(final DateTimeProvider dateTimeProvider, final JdbcTemplate jdbcTemplate, final int serialId) {
        try {
            //[TP] this is for NRTM to cover some cases where there are gaps in the serials table.
            final int minSerialId = jdbcTemplate.queryForObject(
                    "SELECT IF (nextmin, nextmin, 0) " +
                    "FROM   (SELECT min(serial_id) AS nextMin " +
                    "        FROM   serials " +
                    "        WHERE  serial_id >= ?) AS nextMinTable ", Integer.class, serialId);

            if (minSerialId == 0) {
                return null;
            }

            final SerialEntry serialEntry = getSerialEntryWithoutBlobs(jdbcTemplate, minSerialId);
            int effectiveTimestamp;

            if (serialEntry.getOperation() == Operation.DELETE || serialEntry.isAtLast()) {
                effectiveTimestamp = serialEntry.getLastTimestamp();
            } else {
                effectiveTimestamp = serialEntry.getHistoryTimestamp();
            }

            return now(dateTimeProvider) - effectiveTimestamp;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // TODO: [AH] remove legacy references from this query once we deprecated legacy
    // TODO: [AH] fix legacy history to match rdp's approach
    // note: this is really kludgy, because legacy decreased serials.sequence_id by 1 on deletion, to make sure once
    //       could join from the deletion serials record directly to history.
    // Attention [TP]: this method returns the object version from last even if the input serialId is historical!!!
    private static SerialEntry getSerialEntryWithBlobs(final JdbcTemplate jdbcTemplate, final int serialId) {
        return jdbcTemplate.queryForObject("" +
                "SELECT serials.serial_id," +
                "       serials.operation," +
                "       serials.atlast," +
                "       serials.object_id," +
                "       last.timestamp," +
                "       COALESCE(legacy_history.timestamp, rdp_history.timestamp)," +
                "       IF(last.sequence_id, last.object, COALESCE(legacy_history.object," +
                "                                         rdp_history.object)), " +
                "       COALESCE(last.pkey, rdp_history.pkey, legacy_history.pkey) " +
                "FROM   serials" +
                "       LEFT JOIN last" +
                "              ON last.object_id = serials.object_id" +
                "       LEFT JOIN history legacy_history" +
                "              ON legacy_history.object_id = serials.object_id" +
                "                 AND legacy_history.sequence_id = serials.sequence_id" +
                "       LEFT JOIN history rdp_history" +
                "              ON rdp_history.object_id = serials.object_id" +
                "                 AND rdp_history.sequence_id = serials.sequence_id - 1 " +
                "WHERE  serials.serial_id = ?", new RowMapper<SerialEntry>() {
            @Override
            public SerialEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
                    return new SerialEntry(rs.getInt(1), Operation.getByCode(rs.getInt(2)), rs.getBoolean(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getBytes(7), rs.getString(8));
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Failed at serial_id " + serialId, e);
                }
            }
        }, serialId);
    }

    private static List<SerialEntry> getSerialEntryWithBlobsSinceSerialForNrtm4(
        final JdbcTemplate jdbcTemplate,
        final int serialIDFrom,
        final int serialIDTo
    ) {
        return jdbcTemplate.query("""
            SELECT
                serials.serial_id,
                serials.operation,
                serials.atlast,
                serials.object_id,
                IF(last.sequence_id, last.object, COALESCE(history.object, rdp_history.object)),
                COALESCE(last.pkey, history.pkey)
            FROM serials
            LEFT JOIN last
                ON last.object_id = serials.object_id
            LEFT JOIN history history
                ON history.object_id = serials.object_id
                AND history.sequence_id = serials.sequence_id
            LEFT JOIN history rdp_history
                ON rdp_history.object_id = serials.object_id
                AND rdp_history.sequence_id = serials.sequence_id - 1
            WHERE  serials.serial_id > ?
              AND  serials.serial_id <= ?
            ORDER BY serials.serial_id
        """, (rs, rowNum) -> new SerialEntry(
                    rs.getInt(1),
                    Operation.getByCode(rs.getInt(2)),
                    rs.getBoolean(3),
                    rs.getInt(4),
                    rs.getBytes(5),
                    rs.getString(6)
        ), serialIDFrom, serialIDTo);
    }

    // exact same, but omit blob lookup for performance reasons
    private static SerialEntry getSerialEntryWithoutBlobs(final JdbcTemplate jdbcTemplate, final int serialId) {
        return jdbcTemplate.queryForObject("" +
                "SELECT serials.serial_id, " +
                "       serials.operation, " +
                "       serials.atlast, " +
                "       serials.object_id, " +
                "       last.timestamp, " +
                "       COALESCE(legacy_history.timestamp, rdp_history.timestamp), " +
                "       IF(last.sequence_id, last.object, COALESCE(legacy_history.object, " +
                "                                         rdp_history.object))," +
                "       COALESCE(last.pkey, rdp_history.pkey, legacy_history.pkey) " +
                "FROM   serials " +
                "       LEFT JOIN last " +
                "              ON last.object_id = serials.object_id " +
                "       LEFT JOIN history legacy_history " +
                "              ON legacy_history.object_id = serials.object_id " +
                "                 AND legacy_history.sequence_id = serials.sequence_id " +
                "       LEFT JOIN history rdp_history " +
                "              ON rdp_history.object_id = serials.object_id " +
                "                 AND rdp_history.sequence_id = serials.sequence_id - 1 " +
                "WHERE  serials.serial_id = ? ", new RowMapper<SerialEntry>() {
            @Override
            public SerialEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new SerialEntry(rs.getInt(1), Operation.getByCode(rs.getInt(2)), rs.getBoolean(3), rs.getInt(5), rs.getInt(6), rs.getString(7));
            }
        }, serialId);
    }


    //   [TP] If operation is delete return the previous version of the object [needed by NRTM],
    //        otherwise return the exact version which is either in history or in last
    private static SerialEntry getSerialEntryWithBlobsForNrtm(final JdbcTemplate jdbcTemplate, final int serialId) {
        return jdbcTemplate.queryForObject("" +
                "SELECT serials.serial_id, " +
                "       serials.operation, " +
                "       serials.atlast, " +
                "       serials.object_id, " +
                "       IF(serials.operation = 2, " +
                "                rdp_history.object, " +
                "                COALESCE(legacy_history.object, last.object)) as object," +
                "       COALESCE(last.pkey, rdp_history.pkey, legacy_history.pkey) " +
                "FROM   serials " +
                "       LEFT JOIN last " +
                "              ON last.object_id = serials.object_id" +
                "                 AND last.sequence_id = serials.sequence_id  " +
                "       LEFT JOIN history legacy_history " +
                "              ON legacy_history.object_id = serials.object_id " +
                "                 AND legacy_history.sequence_id = serials.sequence_id " +
                "       LEFT JOIN history rdp_history " +
                "              ON rdp_history.object_id = serials.object_id " +
                "                 AND rdp_history.sequence_id = serials.sequence_id - 1 " +
                "WHERE  serials.serial_id = ?", new RowMapper<SerialEntry>() {
            @Override
            public SerialEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
                    return SerialEntry.createSerialEntryWithoutTimestamps
                            (rs.getInt(1), Operation.getByCode(rs.getInt(2)), rs.getBoolean(3), rs.getInt(4), rs.getBytes(5), rs.getString(6));
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Failed at serial_id " + serialId, e);
                }
            }
        }, serialId);
    }
}
