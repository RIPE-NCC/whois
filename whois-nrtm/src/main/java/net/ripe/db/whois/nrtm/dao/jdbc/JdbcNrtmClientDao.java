package net.ripe.db.whois.nrtm.dao.jdbc;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.dao.NrtmClientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.sql.DataSource;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.copyToHistoryAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.deleteFromLastAndSetSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.deleteFromTables;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoLastAndSetSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoTables;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.updateLastAndSetSerials;

@Repository
@Transactional
public class JdbcNrtmClientDao implements NrtmClientDao {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public JdbcNrtmClientDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public RpslObjectUpdateInfo createObject(final RpslObject object, final int serialId) {
        final RpslObjectUpdateInfo rpslObjectInfo = insertIntoLastAndSetSerials(dateTimeProvider, jdbcTemplate, object, serialId);

        insertIntoTables(jdbcTemplate, rpslObjectInfo, object);
        return rpslObjectInfo;
    }

    @Override
    public RpslObjectUpdateInfo updateObject(final RpslObject object, final RpslObjectUpdateInfo rpslObjectInfo, final int serialId) {
        deleteFromTables(jdbcTemplate, rpslObjectInfo);
        insertIntoTables(jdbcTemplate, rpslObjectInfo, object);
        copyToHistoryAndUpdateSerials(jdbcTemplate, rpslObjectInfo);
        final int newSequenceId = updateLastAndSetSerials(dateTimeProvider, jdbcTemplate, rpslObjectInfo, object, serialId);
        return new RpslObjectUpdateInfo(rpslObjectInfo.getObjectId(), newSequenceId, rpslObjectInfo.getObjectType(), rpslObjectInfo.getKey());
    }

    @Override
    public void deleteObject(final RpslObjectUpdateInfo rpslObjectInfo, final int serialId) {
        deleteFromTables(jdbcTemplate, rpslObjectInfo);
        copyToHistoryAndUpdateSerials(jdbcTemplate, rpslObjectInfo);
        deleteFromLastAndSetSerials(dateTimeProvider, jdbcTemplate, rpslObjectInfo, serialId);
    }

    @Override
    public boolean objectExistsWithSerial(final int serialId, final int objectId) {
        final int found = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM serials WHERE serial_id = ? AND object_id = ?",
                Integer.class,
                serialId, objectId);
        return found > 0;
    }
}
