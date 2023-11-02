package net.ripe.db.nrtm4.dao;

import jakarta.ws.rs.InternalServerErrorException;
import net.ripe.db.whois.common.DateTimeProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.ZoneOffset;

@Repository
public class NrtmKeyConfigDao {

    private final JdbcTemplate readTemplate;
    private final JdbcTemplate writeTemplate;
    private final DateTimeProvider dateTimeProvider;


    NrtmKeyConfigDao(@Qualifier("nrtmSlaveDataSource") final DataSource readOnlyDataSource, @Qualifier("nrtmMasterDataSource") final DataSource writeDataSource, final DateTimeProvider dateTimeProvider ) {
        this.readTemplate = new JdbcTemplate(readOnlyDataSource);
        this.dateTimeProvider = dateTimeProvider;
        this.writeTemplate = new JdbcTemplate(writeDataSource);
    }

    public byte[] getPrivateKey() {
        return readTemplate.queryForObject("SELECT private_key FROM key_pair", (rs, rowNum) -> rs.getBytes(1));
    }

    public byte[] getPublicKey() {
        return readTemplate.queryForObject("SELECT public_key FROM key_pair", (rs, rowNum) -> rs.getBytes(1));
    }

    public void saveKeyPair( final byte[] privateKey,  final byte[] publicKey) {
        final String sql = """
        INSERT INTO key_pair (private_key, public_key, created, expires)
        VALUES (?, ?, ?, ?)
        """;

        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
        final long expires = dateTimeProvider.getCurrentDateTime().plusYears(1).toEpochSecond(ZoneOffset.UTC);
        writeTemplate.update(sql,privateKey, publicKey, createdTimestamp, expires);
    }

    public boolean isKeyPairExists() {
        final int count = writeTemplate.queryForObject("SELECT count(*) FROM key_pair", Integer.class);
        if(count > 1) {
            throw new InternalServerErrorException("More than one key pair exists");
        }

        return count == 1;
    }
}
