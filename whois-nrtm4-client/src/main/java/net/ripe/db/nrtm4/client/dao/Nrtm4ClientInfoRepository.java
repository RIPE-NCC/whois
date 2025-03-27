package net.ripe.db.nrtm4.client.dao;

import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.config.NrtmClientTransactionConfiguration;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.List;

@Repository
@Conditional(Nrtm4ClientCondition.class)
@Transactional(transactionManager = NrtmClientTransactionConfiguration.NRTM_CLIENT_INFO_TRANSACTION)
public class Nrtm4ClientInfoRepository {

    private final JdbcTemplate jdbcMasterTemplate;
    private final DateTimeProvider dateTimeProvider;

    public Nrtm4ClientInfoRepository(@Qualifier("nrtmClientMasterInfoSource") final DataSource masterInfoSource,
                                     final DateTimeProvider dateTimeProvider) {
        this.jdbcMasterTemplate = new JdbcTemplate(masterInfoSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    public void saveUpdateNotificationFileVersion(final String source,
                                                  final long version,
                                                  final String sessionID,
                                                  final String serviceName){
        saveVersionInfo(source, version, sessionID, serviceName, NrtmClientDocumentType.NOTIFICATION);
    }

    public void saveDeltaFileVersion(final String source,
                                     final long version,
                                     final String sessionID){
        saveVersionInfo(source, version, sessionID, null, NrtmClientDocumentType.DELTA);
    }

    public void saveSnapshotFileVersion(final String source,
                                        final long version,
                                        final String sessionID){
        saveVersionInfo(source, version, sessionID, null, NrtmClientDocumentType.SNAPSHOT);
    }

    public List<NrtmClientVersionInfo> getNrtmLastVersionInfoForUpdateNotificationFile(){
        final String sql = """
            SELECT id, source, MAX(version), session_id, type, hostname, created
            FROM version_info
            WHERE type = ?
            GROUP BY source
            """;

        return jdbcMasterTemplate.query(sql,
                nrtmClientVersionRowMapper(),
                NrtmClientDocumentType.NOTIFICATION.getFileNamePrefix());
    }

    @Nullable
    public NrtmClientVersionInfo getNrtmLastVersionInfoForDeltasPerSource(final String source){
        try {
            return jdbcMasterTemplate.queryForObject("""
                    SELECT id, source, MAX(version), session_id, type, hostname, created
                    FROM version_info
                    WHERE type = ?
                    AND source = ?
                    """,
                    nrtmClientVersionRowMapper(),
                    NrtmClientDocumentType.DELTA.getFileNamePrefix(), source);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void truncateTables(){
        jdbcMasterTemplate.update("DELETE FROM version_info");
    }

    private void saveVersionInfo(
            final String source,
            final long version,
            final String sessionID,
            final String serviceName,
            final NrtmClientDocumentType type) {
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        jdbcMasterTemplate.update("INSERT INTO version_info (source, version, session_id, type, hostname, created) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                source,
                version,
                sessionID,
                type.getFileNamePrefix(),
                serviceName,
                now);
    }

    private static RowMapper<NrtmClientVersionInfo> nrtmClientVersionRowMapper() {
        return  (rs, rn) -> {
            final Long version = rs.getLong("MAX(version)");
            if (rs.wasNull()){
                return null;
            }
            return new NrtmClientVersionInfo(
                rs.getLong("id"),
                rs.getString("source"),
                version,
                rs.getString("session_id"),
                NrtmClientDocumentType.fromValue(rs.getString("type")),
                rs.getString("hostname"),
                rs.getLong("created"));
        };
    }
}
