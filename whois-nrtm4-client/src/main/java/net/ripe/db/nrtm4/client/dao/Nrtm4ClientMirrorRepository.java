package net.ripe.db.nrtm4.client.dao;

import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Repository
@Conditional(Nrtm4ClientCondition.class)
public class Nrtm4ClientMirrorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(Nrtm4ClientMirrorRepository.class);

    private final JdbcTemplate jdbcMasterTemplate;
    private final JdbcTemplate jdbcSlaveTemplate;
    private final DateTimeProvider dateTimeProvider;

    public Nrtm4ClientMirrorRepository(@Qualifier("nrtmClientMasterDataSource") final DataSource masterDataSource,
                                       @Qualifier("nrtmClientSlaveDataSource") final DataSource slaveDataSource,
                                       final DateTimeProvider dateTimeProvider) {
        this.jdbcMasterTemplate = new JdbcTemplate(masterDataSource);
        this.jdbcSlaveTemplate = new JdbcTemplate(slaveDataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    public void saveUpdateNotificationFileVersion(final String source,
                                                  final long version,
                                                  final String sessionID){
        saveVersionInfo(source, version, sessionID, NrtmClientDocumentType.NOTIFICATION);
    }

    public void saveSnapshotFileVersion(final String source,
                                        final long version,
                                        final String sessionID){
        saveVersionInfo(source, version, sessionID, NrtmClientDocumentType.SNAPSHOT);
    }

    public List<NrtmClientVersionInfo> getNrtmLastVersionInfoForUpdateNotificationFile(){
        final String sql = """
            SELECT id, source, MAX(version), session_id, type, created
            FROM version_info
            WHERE type = 'update-notification-file'
            GROUP BY source
            """;
        return jdbcSlaveTemplate.query(sql,
                (rs, rn) -> new NrtmClientVersionInfo(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getLong(3),
                        rs.getString(4),
                        NrtmClientDocumentType.fromValue(rs.getString(5)),
                        rs.getLong(6)
                        ));
    }

    public void persistRpslObject(final RpslObject rpslObject){
        try {
            final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
            jdbcMasterTemplate.update("INSERT INTO last_mirror (object, object_type, pkey, timestamp) VALUES (?, ?, ?, ?)",
                    getRpslObjectBytes(rpslObject),
                    ObjectTypeIds.getId(rpslObject.getType()),
                    rpslObject.getKey().toString(),
                    now);
        } catch (IOException e) {
            LOGGER.error("unable to get the bytes of the object {}", rpslObject.getKey(), e);
        }
    }

    public void truncateTables(){
        jdbcMasterTemplate.update("TRUNCATE version_info");
        jdbcMasterTemplate.update("TRUNCATE last_mirror");
    }

    private void saveVersionInfo(
            final String source,
            final long version,
            final String sessionID,
            final NrtmClientDocumentType type) {
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        jdbcMasterTemplate.update("INSERT INTO version_info (source, version, session_id, type, created) VALUES (?, ?, ?, ?, ?)",
                source,
                version,
                sessionID,
                type.getFileNamePrefix(),
                now);
    }

    private static byte[] getRpslObjectBytes(final RpslObject rpslObject) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        rpslObject.writeTo(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}
