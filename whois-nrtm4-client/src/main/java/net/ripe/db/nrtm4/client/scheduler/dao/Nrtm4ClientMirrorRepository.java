package net.ripe.db.nrtm4.client.scheduler.dao;

import net.ripe.db.nrtm4.client.scheduler.NrtmDocumentType;
import net.ripe.db.nrtm4.client.scheduler.NrtmVersionInfo;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class Nrtm4ClientMirrorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(Nrtm4ClientMirrorRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Nrtm4ClientMirrorRepository(@Qualifier("nrtmClientMasterDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public NrtmVersionInfo saveUpdateNotificationFileVersion(final String source,
                                                             final long version,
                                                             final String sessionID){
        return saveVersionInfo(source, version, sessionID, NrtmDocumentType.NOTIFICATION);
    }

    private NrtmVersionInfo saveVersionInfo(
            final String source,
            final long version,
            final String sessionID,
            final NrtmDocumentType type) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        jdbcTemplate.update(connection -> {
                    final String sql = """
                    INSERT INTO version_info (source, version, session_id, type, last_serial_id, created)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                    final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, source);
                    pst.setLong(2, version);
                    pst.setString(3, sessionID);
                    pst.setString(4, type.name());
                    pst.setLong(5, now);
                    return pst;
                }, keyHolder
        );
        return new NrtmVersionInfo(keyHolder.getKeyAs(Long.class), source, version, sessionID, type, now);
    }

}
