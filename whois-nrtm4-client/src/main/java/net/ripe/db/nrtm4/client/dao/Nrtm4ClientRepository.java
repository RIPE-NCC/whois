package net.ripe.db.nrtm4.client.dao;

import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
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
        jdbcMasterTemplate.update("TRUNCATE last");
    }

    public void persistRpslObject(final RpslObject rpslObject){
        try {
            final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
            jdbcMasterTemplate.update("INSERT INTO last (object, object_type, pkey, timestamp) VALUES (?, ?, ?, ?)",
                    getRpslObjectBytes(rpslObject),
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    now);
        } catch (IOException e) {
            LOGGER.error("unable to get the bytes of the object {}", rpslObject.getKey(), e);
        }
    }

    private static byte[] getRpslObjectBytes(final RpslObject rpslObject) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        rpslObject.writeTo(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
