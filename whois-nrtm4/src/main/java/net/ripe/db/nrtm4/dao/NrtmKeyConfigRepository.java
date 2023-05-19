package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.util.Ed25519Util;
import net.ripe.db.whois.common.DateTimeProvider;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.time.ZoneOffset;

@Repository
public class NrtmKeyConfigRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    NrtmKeyConfigRepository(@Qualifier("nrtmDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    @PostConstruct
    public void initializeEd25519KeyPair() {
        try {
            getPrivateKey();
        } catch (EmptyResultDataAccessException ex) {
            generateKeyPair();
        }
    }

    private void generateKeyPair() {
        final String sql = """
        INSERT INTO key_pair (private_key, public_key, created)
        VALUES (?, ?, ?, ?)
        """;

        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);

        final AsymmetricCipherKeyPair asymmetricCipherKeyPair = Ed25519Util.generateEd25519KeyPair();
        final byte[] privateKey =((Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getEncoded();
        final byte[] publicKey = ((Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getEncoded();

        jdbcTemplate.update(sql,privateKey, publicKey, createdTimestamp);
    }

    public byte[] getPrivateKey() {
        return jdbcTemplate.queryForObject(" SELECT private_key FROM delta_file", (rs, rowNum) -> rs.getBytes(1));
    }

    public byte[] getPublicKey() {
        return jdbcTemplate.queryForObject(" SELECT public_key FROM delta_file", (rs, rowNum) -> rs.getBytes(1));
    }

}
