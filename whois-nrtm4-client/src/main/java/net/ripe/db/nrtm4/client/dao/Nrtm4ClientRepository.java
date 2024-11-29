package net.ripe.db.nrtm4.client.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ripe.db.nrtm4.client.client.MirrorSnapshotInfo;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.copyToHistoryAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.deleteFromLastAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.deleteFromTables;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoLastAndUpdateSerials;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoTables;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoTablesIgnoreMissing;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.lookupRpslObjectUpdateInfo;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.updateLastAndUpdateSerials;

@Repository
@Conditional(Nrtm4ClientCondition.class)
public class Nrtm4ClientRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(Nrtm4ClientInfoRepository.class);

    private final JdbcTemplate jdbcMasterTemplate;
    private final JdbcTemplate jdbcSlaveTemplate;
    private final DateTimeProvider dateTimeProvider;

    public Nrtm4ClientRepository(@Qualifier("nrtmClientMasterDataSource") final DataSource masterDataSource,
                                     @Qualifier("nrtmClientSlaveDataSource") final DataSource slaveDataSource,
                                     final DateTimeProvider dateTimeProvider) {
        this.jdbcMasterTemplate = new JdbcTemplate(masterDataSource);
        this.jdbcSlaveTemplate = new JdbcTemplate(slaveDataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    public void truncateTables(){
        jdbcMasterTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        final String databaseName = jdbcSlaveTemplate.queryForObject("SELECT DATABASE()", String.class);

        final List<String> tables = jdbcSlaveTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = ?",
                String.class,
                databaseName
        );

        tables.forEach(table -> jdbcMasterTemplate.execute("DELETE FROM " + table));

        jdbcMasterTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

    }

    @Nullable
    public RpslObjectUpdateInfo getMirroredObjectId(final ObjectType type, final String primaryKey){
        try {
            return lookupRpslObjectUpdateInfo(jdbcSlaveTemplate, type, primaryKey);
        } catch (EmptyResultDataAccessException ex){
            return null;
        }
    }

    @Nullable
    public Integer getSerialByObjectId(final int objectId, final int sequenceId) {
        final String query = "SELECT serial_id FROM serials WHERE object_id = ? AND sequence_id = ?";
        try {
            return jdbcSlaveTemplate.queryForObject(query, Integer.class, objectId, sequenceId);
        } catch (EmptyResultDataAccessException ex){
            return null;
        }
    }

    public RpslObjectUpdateInfo persistRpslObject(final RpslObject rpslObject){
        return insertIntoLastAndUpdateSerials(dateTimeProvider, jdbcMasterTemplate, rpslObject);
    }

    public void createIndexes(final RpslObject rpslObject, final RpslObjectUpdateInfo rpslObjectUpdateInfo){
        //Using IgnoreMissing because of the order, RpslObjects are not coming in order from NRTMv4
        insertIntoTablesIgnoreMissing(jdbcMasterTemplate, rpslObjectUpdateInfo, rpslObject);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map.Entry<RpslObject, RpslObjectUpdateInfo> processSnapshotRecord(final MirrorSnapshotInfo mirrorSnapshotInfo) throws JsonProcessingException {
        return Map.entry(mirrorSnapshotInfo.getRpslObject(), persistRpslObject(mirrorSnapshotInfo.getRpslObject()));
    }

    public void removeMirroredObjectAndUpdateSerials(final RpslObjectUpdateInfo rpslObjectInfo){
        deleteFromTables(jdbcMasterTemplate, rpslObjectInfo);
        copyToHistoryAndUpdateSerials(jdbcMasterTemplate, rpslObjectInfo);
        deleteFromLastAndUpdateSerials(dateTimeProvider, jdbcMasterTemplate, rpslObjectInfo);
    }

    public void updateMirroredObject(final RpslObject rpslObject, final RpslObjectUpdateInfo rpslObjectUpdateInfo){
        deleteFromTables(jdbcMasterTemplate, rpslObjectUpdateInfo);
        insertIntoTables(jdbcMasterTemplate, rpslObjectUpdateInfo, rpslObject);
        copyToHistoryAndUpdateSerials(jdbcMasterTemplate, rpslObjectUpdateInfo);
        updateLastAndUpdateSerials(dateTimeProvider, jdbcMasterTemplate, rpslObjectUpdateInfo, rpslObject);
    }
}
