package net.ripe.db.whois.api.rest;

import net.ripe.db.nrtm4.util.Ed25519Util;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.transaction.TransactionConfiguration;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.ZoneOffset;

@Component
public class TestNrtmTransactionDao {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public TestNrtmTransactionDao(@Qualifier("nrtmMasterDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }


    @Transactional(transactionManager = TransactionConfiguration.NRTM_UPDATE_TRANSACTION)
    void testTransaction(final int maxId) {
        saveKeys(maxId);
    }

    private void saveKeys(int maxId) {
        saveKeyPair(maxId + 1);
        saveKeyPair(maxId + 2);

        //this will throw exception
        saveKeyPair(maxId + 1);
    }

    public void saveKeyPair(final int id) {
        final AsymmetricCipherKeyPair asymmetricCipherKeyPair = Ed25519Util.generateEd25519KeyPair();
        final byte[] privateKey =((Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getEncoded();
        final byte[] publicKey = ((Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getEncoded();

        final String sql = """
        INSERT INTO key_pair (id, private_key, public_key, created, expires)
        VALUES (?, ?, ?, ?, ?)
        """;

        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
        final long expires = dateTimeProvider.getCurrentDateTime().plusYears(1).toEpochSecond(ZoneOffset.UTC);
        jdbcTemplate.update(sql, id, privateKey, publicKey, createdTimestamp, expires);
    }

    public Integer getKeyForId(int id) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM key_pair where id=?", Integer.class, id);
    }

    public Integer getMaxId() {
        final Integer maxId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM key_pair", Integer.class);
        return maxId == null ? 0 : maxId;
    }
}
