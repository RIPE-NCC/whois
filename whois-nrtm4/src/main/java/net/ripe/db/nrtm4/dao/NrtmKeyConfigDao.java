package net.ripe.db.nrtm4.dao;

import jakarta.ws.rs.InternalServerErrorException;
import net.ripe.db.nrtm4.domain.NrtmKeyRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class NrtmKeyConfigDao {

    private final JdbcTemplate readTemplate;
    private final JdbcTemplate writeTemplate;

    NrtmKeyConfigDao(@Qualifier("nrtmSlaveDataSource") final DataSource readOnlyDataSource, @Qualifier("nrtmMasterDataSource") final DataSource writeDataSource) {
        this.readTemplate = new JdbcTemplate(readOnlyDataSource);
        this.writeTemplate = new JdbcTemplate(writeDataSource);
    }

    public byte[] getActivePrivateKey() {
        return readTemplate.queryForObject("SELECT private_key FROM key_pair where is_active= true", (rs, rowNum) -> rs.getBytes(1));
    }

    public void saveKeyPair(final NrtmKeyRecord nrtmKeyRecord) {
        final String sql = """
        INSERT INTO key_pair (private_key, pem_format, created, expires, is_active)
        VALUES (?, ?, ?, ?, ?)
        """;

        writeTemplate.update(sql,nrtmKeyRecord.privateKey(), nrtmKeyRecord.pemFormat(), nrtmKeyRecord.createdTimestamp(), nrtmKeyRecord.expires(), nrtmKeyRecord.isActive());
    }

    public void deleteKeyPair(final NrtmKeyRecord nrtmKeyRecord) {
        writeTemplate.update("DELETE FROM key_pair WHERE id = ?", nrtmKeyRecord.id());
    }

    public void makeCurrentActiveKeyAsInActive() {
        writeTemplate.update("UPDATE key_pair SET is_active = false WHERE is_active = true");
    }

    public NrtmKeyRecord getActiveKeyPair() {
        return readTemplate.queryForObject(
                    "SELECT id, private_key, pem_format, is_active, created, expires FROM key_pair WHERE is_active = true",
                    (rs, rn) -> new NrtmKeyRecord(rs.getLong(1),
                                                  rs.getBytes(2),
                                                  rs.getString(3),
                                                  rs.getBoolean(4),
                                                  rs.getLong(5),
                                                  rs.getLong(6))
                    );
    }

    public List<NrtmKeyRecord> getAllKeyPair() {
        return readTemplate.query(
                "SELECT id, private_key, pem_format, is_active, created, expires FROM key_pair",
                (rs, rn) -> new NrtmKeyRecord(rs.getLong(1),
                        rs.getBytes(2),
                        rs.getString(3),
                        rs.getBoolean(4),
                        rs.getLong(5),
                        rs.getLong(6))
        );
    }

    public boolean isActiveKeyPairExists() {
        final int count = writeTemplate.queryForObject("SELECT count(*) FROM key_pair WHERE is_active=true", Integer.class);
        if(count > 1) {
            throw new InternalServerErrorException("More than one active key pair exists");
        }

        return count == 1;
    }
}
