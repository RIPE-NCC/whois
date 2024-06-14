package net.ripe.db.nrtm4.generator;

import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.domain.NrtmKeyRecord;
import net.ripe.db.nrtm4.util.Ed25519Util;
import net.ripe.db.whois.common.DateTimeProvider;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NrtmKeyPairService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmKeyPairService.class);

    private final NrtmKeyConfigDao nrtmKeyConfigDao;
    private final DateTimeProvider dateTimeProvider;


    @Autowired
    public NrtmKeyPairService(final NrtmKeyConfigDao nrtmKeyConfigDao, final DateTimeProvider dateTimeProvider) {
        this.nrtmKeyConfigDao = nrtmKeyConfigDao;
        this.dateTimeProvider = dateTimeProvider;
    }

    public void generateActiveKeyPair() {
        if(!nrtmKeyConfigDao.isActiveKeyPairExists()) {
            generateKeyRecord(true);
        } else {
            LOGGER.info("One active key already exists");
        }
    }

    public void generateKeyRecord(final boolean isActive) {
        final AsymmetricCipherKeyPair asymmetricCipherKeyPair = Ed25519Util.generateEd25519KeyPair();
        final byte[] privateKey =((Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getEncoded();
        final byte[] publicKey = ((Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getEncoded();

        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
        final long expires = dateTimeProvider.getCurrentDateTime().plusYears(1).toEpochSecond(ZoneOffset.UTC);

        nrtmKeyConfigDao.saveKeyPair(NrtmKeyRecord.of(privateKey, publicKey, isActive, createdTimestamp, expires ));
    }

    public NrtmKeyRecord getNextkeyPairRecord() {
        final NrtmKeyRecord currentActiveKey = nrtmKeyConfigDao.getActiveKeyPair();
        final List<NrtmKeyRecord> nextKey = nrtmKeyConfigDao.getAllKeyPair().stream().filter(keyRecord -> keyRecord.expires() > currentActiveKey.expires()).collect(Collectors.toList());
        if(nextKey.isEmpty()) {
            return null;
        }

        if(nextKey.size() != 1) {
            throw new RuntimeException("More than two next key pair record found , this cannot happen");
        }

        return nextKey.get(0);
    }

}
