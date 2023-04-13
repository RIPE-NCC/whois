package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class NrtmFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmFileRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;


    public NrtmFileRepository(@Qualifier("nrtmDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDeltaVersion(final NrtmVersionInfo version, final int serialIDTo, final List<DeltaChange> deltas) throws JsonProcessingException {
       if(deltas.isEmpty()) {
           LOGGER.info("No delta changes found for source {}", version.source().getName());
           return;
       }

       final NrtmVersionInfo newVersion = saveNewDeltaVersion(version, serialIDTo);
       final DeltaFile deltaFile = getDeltaFile(newVersion, deltas);

       saveDeltaFile(deltaFile.versionId(), deltaFile.name(), deltaFile.hash(), deltaFile.payload());
       LOGGER.info("Created {} delta version {}", newVersion.source().getName(), newVersion.version());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSnapshotVersion(final NrtmVersionInfo version, final String fileName, final String hash, final byte[] payload)  {

        final NrtmVersionInfo newVersion = saveNewSnapshotVersion(version);
        final SnapshotFile snapshotFile = SnapshotFile.of(newVersion.id(), fileName, hash);

        saveSnapshot(snapshotFile, payload);
        LOGGER.info("Created {} snapshot version {}", version.source().getName(), version.version());
    }

    public DeltaFile getDeltaFile(final NrtmVersionInfo newVersion, final List<DeltaChange> deltas) throws JsonProcessingException {
        final PublishableDeltaFile publishableDeltaFile = new PublishableDeltaFile(newVersion, deltas);
        final String json = new ObjectMapper().writeValueAsString(publishableDeltaFile);
        final String hash = NrtmFileUtil.calculateSha256(json.getBytes(StandardCharsets.UTF_8));
        return DeltaFile.of(newVersion.id(), NrtmFileUtil.newFileName(newVersion), hash, json);
    }

    public NrtmVersionInfo saveNewDeltaVersion(final NrtmVersionInfo version, final int lastSerialID) {
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
}
