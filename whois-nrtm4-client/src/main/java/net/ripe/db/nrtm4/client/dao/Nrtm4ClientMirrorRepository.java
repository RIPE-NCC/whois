package net.ripe.db.nrtm4.client.dao;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class Nrtm4ClientMirrorRepository {

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
        saveVersionInfo(source, version, sessionID, "update-notification-file");
    }

    public List<NrtmVersionInfo> getNrtmLastVersionInfo(){
        final String sql = """
            SELECT id, source, MAX(version), session_id, type, created
            FROM version_info
            """;
        return jdbcSlaveTemplate.query(sql,
                (rs, rn) -> new NrtmVersionInfo(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getLong(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getLong(6)
                        ));
    }

    public void truncateTables(){
        jdbcMasterTemplate.update("TRUNCATE version_info");
        jdbcMasterTemplate.update("TRUNCATE last_mirror");
    }

    private void saveVersionInfo(
            final String source,
            final long version,
            final String sessionID,
            final String type) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        jdbcMasterTemplate.update(connection -> {
                    final String sql = """
                    INSERT INTO version_info (source, version, session_id, type, created)
                    VALUES (?, ?, ?, ?, ?)
                    """;
                    final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, source);
                    pst.setLong(2, version);
                    pst.setString(3, sessionID);
                    pst.setString(4, type);
                    pst.setLong(5, now);
                    return pst;
                }, keyHolder
        );
        new NrtmVersionInfo(keyHolder.getKeyAs(Long.class), source, version, sessionID, type, now);
    }

}
