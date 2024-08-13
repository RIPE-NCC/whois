package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ripe.db.nrtm4.domain.DeltaFileRecord;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmKeyRecord;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.TransactionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
@Transactional(transactionManager = TransactionConfiguration.NRTM_UPDATE_TRANSACTION)
public class UpdateNrtmFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNrtmFileRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public UpdateNrtmFileRepository(@Qualifier("nrtmMasterDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public void saveDeltaVersion(final NrtmVersionInfo version, final int serialIDTo, final List<DeltaFileRecord> deltas) throws JsonProcessingException {
        if (deltas.isEmpty()) {
            LOGGER.info("No delta changes found for source {}", version.source().getName());
            return;
        }

        final NrtmVersionInfo newVersion = saveNewDeltaVersion(version, serialIDTo);
        final DeltaFile deltaFile = getDeltaFile(newVersion, deltas);

        saveDeltaFile(deltaFile.versionId(), deltaFile.name(), deltaFile.hash(), deltaFile.payload());
        LOGGER.info("Created {} delta version {}", newVersion.source().getName(), newVersion.version());
    }

    public void saveSnapshotVersion(final NrtmVersionInfo version, final String fileName, final String hash, final byte[] payload)  {

        final NrtmVersionInfo newVersion = saveNewSnapshotVersion(version);
        final SnapshotFile snapshotFile = SnapshotFile.of(newVersion.id(), fileName, hash);

        saveSnapshot(snapshotFile, payload);
        LOGGER.info("Created {} snapshot version {}", version.source().getName(), version.version());
    }

    public void rotateKey(final NrtmKeyRecord newActiveKey, final NrtmKeyRecord oldActiveKey) {
        LOGGER.warn("NRTMv4 rotating the key");

        jdbcTemplate.update("UPDATE key_pair k1 JOIN key_pair k2 " +
                                 "SET k1.is_active = 1, k2.is_active = 0 " +
                                 "WHERE k1.id = ? AND k2.id=?",
                newActiveKey.id(), oldActiveKey.id());

        LOGGER.info("Key rotated successfully");
    }

    private DeltaFile getDeltaFile(final NrtmVersionInfo newVersion, final List<DeltaFileRecord> deltas) throws JsonProcessingException {

        final StringBuilder json = NrtmFileUtil.convertToJSONTextSeq(new StringBuilder(),new NrtmVersionRecord(newVersion, NrtmDocumentType.DELTA));

        for (final DeltaFileRecord delta : deltas) {
            NrtmFileUtil.convertToJSONTextSeq(json, delta);
        }

        final String hash = NrtmFileUtil.calculateSha256(json.toString().getBytes(StandardCharsets.UTF_8));
        return DeltaFile.of(newVersion.id(), NrtmFileUtil.newFileName(newVersion), hash, json.toString());
    }

    private NrtmVersionInfo saveNewDeltaVersion(final NrtmVersionInfo version, final int lastSerialID) {
        return saveVersionInfo(version.source(), version.version() + 1, version.sessionID(), NrtmDocumentType.DELTA, lastSerialID);
    }

    public NrtmVersionInfo saveNewSnapshotVersion(final NrtmVersionInfo version) {
        return saveVersionInfo(version.source(), version.version(), version.sessionID(), NrtmDocumentType.SNAPSHOT, version.lastSerialId());
    }

    private NrtmVersionInfo saveVersionInfo(
            final NrtmSource source,
            final long version,
            final String sessionID,
            final NrtmDocumentType type,
            final int lastSerialId) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        jdbcTemplate.update(connection -> {
                    final String sql = """
                    INSERT INTO version_info (source_id, version, session_id, type, last_serial_id, created)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                    final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setLong(1, source.getId());
                    pst.setLong(2, version);
                    pst.setString(3, sessionID);
                    pst.setString(4, type.name());
                    pst.setInt(5, lastSerialId);
                    pst.setLong(6, now);
                    return pst;
                }, keyHolder
        );
        return new NrtmVersionInfo(keyHolder.getKeyAs(Long.class), source, version, sessionID, type, lastSerialId, now);
    }

    public void saveDeltaFile(
            final long versionId,
            final String name,
            final String hash,
            final String payload
    ) {
        final String sql = """
            INSERT INTO delta_file (version_id, name, hash, payload)
            VALUES (?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql, versionId, name, hash, payload);
    }

    public void saveSnapshot(final SnapshotFile snapshotFile, final byte[] payload) {
        final String sql = """
            INSERT INTO snapshot_file (version_id, name, hash, payload)
            VALUES (?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
                snapshotFile.versionId(),
                snapshotFile.name(),
                snapshotFile.hash(),
                payload);
    }

    public void deleteSnapshotFiles(final List<Long> versionIds) {
        if(versionIds.isEmpty()) {
            return;
        }

        final int rows = namedParameterJdbcTemplate.update("DELETE FROM snapshot_file WHERE version_id IN (:versionIds)", Map.of("versionIds", versionIds));
        if (rows != 1) {
            throw new IllegalArgumentException("Unable to delete snapshot file with version id's: " + versionIds);
        }
        deleteVersionInfos(versionIds);
    }

    public void deleteDeltaFiles(final List<Long> versionIds) {
        if(versionIds.isEmpty()) {
            return;
        }

        final int rows = namedParameterJdbcTemplate.update("DELETE FROM delta_file WHERE  version_id IN (:versionIds)", Map.of("versionIds", versionIds));
        if (rows != versionIds.size()) {
            throw new IllegalArgumentException("Unable to delete few old delta files with version id's " + versionIds);
        }
        deleteVersionInfos(versionIds);
    }
    
    public void cleanupNrtmv4Database() {
        LOGGER.warn("Cleaning up NRTMv4 Database");

        jdbcTemplate.update("delete from snapshot_file");
        jdbcTemplate.update("delete from delta_file");
        jdbcTemplate.update("delete from notification_file");
        jdbcTemplate.update("delete from version_info");
        jdbcTemplate.update("delete from key_pair");
        jdbcTemplate.update("delete from source");
    }

    private void deleteVersionInfos(final List<Long> versionIds) {
        final int rows = namedParameterJdbcTemplate.update("DELETE FROM version_info WHERE id IN (:versionIds)", Map.of("versionIds", versionIds));
        if (rows != versionIds.size()) {
            throw new IllegalArgumentException("Unable to delete old version info with version ids");
        }
    }
}
