package net.ripe.db.nrtm4.dao;

import jakarta.ws.rs.InternalServerErrorException;
import net.ripe.db.nrtm4.domain.NrtmKeyRecord;
import net.ripe.db.whois.common.DateTimeProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

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

    public byte[] getActivePrivateKey() {
        return readTemplate.queryForObject("SELECT private_key FROM key_pair where isActive=true", (rs, rowNum) -> rs.getBytes(1));
    }

    public byte[] getActivePublicKey() {
        return readTemplate.queryForObject("SELECT public_key FROM key_pair where isActive=true", (rs, rowNum) -> rs.getBytes(1));
    }

    public void saveKeyPair(final NrtmKeyRecord nrtmKeyRecord) {
        final String sql = """
        INSERT INTO key_pair (private_key, public_key, created, expires, isActive)
        VALUES (?, ?, ?, ?, ?)
        """;

        writeTemplate.update(sql,nrtmKeyRecord.privateKey(), nrtmKeyRecord.publicKey(), nrtmKeyRecord.createdTimestamp(), nrtmKeyRecord.expires(), nrtmKeyRecord.isActive());
    }

    public NrtmKeyRecord getActiveKeyPair() {
        return readTemplate.queryForObject(
                    "SELECT id, private_key, public_key, isActive, created, expires FROM key_pair WHERE isActive = true",
                    (rs, rn) -> new NrtmKeyRecord(rs.getLong(1),
                                                  rs.getBytes(2),
                                                  rs.getBytes(3),
                                                  rs.getBoolean(4),
                                                  rs.getLong(5),
                                                  rs.getLong(6))
                    );
    }

    public List<NrtmKeyRecord> getAllKeyPair() {
        return readTemplate.query(
                "SELECT id, private_key, public_key, isActive, created, expires FROM key_pair",
                (rs, rn) -> new NrtmKeyRecord(rs.getLong(1),
                        rs.getBytes(2),
                        rs.getBytes(3),
                        rs.getBoolean(4),
                        rs.getLong(5),
                        rs.getLong(6))
        );
    }

    public boolean isActiveKeyPairExists() {
        final int count = writeTemplate.queryForObject("SELECT count(*) FROM key_pair WHERE isActive=true", Integer.class);
        if(count > 1) {
            throw new InternalServerErrorException("More than one active key pair exists");
        }

        return count == 1;
    }
}
