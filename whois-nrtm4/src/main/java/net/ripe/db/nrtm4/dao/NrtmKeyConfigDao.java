package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.DateTimeProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class NrtmKeyConfigDao {

    private final JdbcTemplate readTemplate;

    NrtmKeyConfigDao(@Qualifier("nrtmSlaveDataSource") final DataSource readOnlyDataSource) {
        this.readTemplate = new JdbcTemplate(readOnlyDataSource);
    }

    public byte[] getPrivateKey() {
        return readTemplate.queryForObject("SELECT private_key FROM key_pair", (rs, rowNum) -> rs.getBytes(1));
    }

    public byte[] getPublicKey() {
        return readTemplate.queryForObject("SELECT public_key FROM key_pair", (rs, rowNum) -> rs.getBytes(1));
    }
}
