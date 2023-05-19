package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.DateTimeProvider;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.util.Base64;

@Repository
public class NrtmKeyConfigRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    NrtmKeyConfigRepository(@Qualifier("nrtmDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    public void createKeyPair() {
        final String sql = """
            INSERT INTO key_pair (private_key, public_key, timestamp)
            VALUES (?, ?, ?, ?)
            """;

        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);

        final AsymmetricCipherKeyPair ssymmetricCipherKeyPair = generateEd25519KeyPair();
        final byte[] privateKey =((Ed25519PrivateKeyParameters) ssymmetricCipherKeyPair.getPrivate()).getEncoded();
        final byte[] publicKey = ((Ed25519PublicKeyParameters) ssymmetricCipherKeyPair.getPublic()).getEncoded();

        jdbcTemplate.update(sql,privateKey, publicKey, createdTimestamp);
    }

    public byte[] getPrivateKey() {
        return jdbcTemplate.queryForObject(" SELECT private_key FROM delta_file", (rs, rowNum) -> rs.getBytes(1));
    }

    public byte[] getPublicKey() {
        return jdbcTemplate.queryForObject(" SELECT public_key FROM delta_file", (rs, rowNum) -> rs.getBytes(1));
    }

    private AsymmetricCipherKeyPair generateEd25519KeyPair() {
        Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
        keyPairGenerator.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        return keyPairGenerator.generateKeyPair();
    }

}
