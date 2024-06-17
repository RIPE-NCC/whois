package net.ripe.db.nrtm4.generator;

import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.dao.UpdateNrtmFileRepository;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NrtmKeyPairService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmKeyPairService.class);

    private final NrtmKeyConfigDao nrtmKeyConfigDao;
    private final DateTimeProvider dateTimeProvider;
    private final UpdateNrtmFileRepository updateNrtmFileRepository;



    @Autowired
    public NrtmKeyPairService(final NrtmKeyConfigDao nrtmKeyConfigDao, final DateTimeProvider dateTimeProvider, final UpdateNrtmFileRepository updateNrtmFileRepository) {
        this.nrtmKeyConfigDao = nrtmKeyConfigDao;
        this.dateTimeProvider = dateTimeProvider;
        this.updateNrtmFileRepository = updateNrtmFileRepository;
    }

    public void generateActiveKeyPair() {
        if(!nrtmKeyConfigDao.isActiveKeyPairExists()) {
            generateKeyRecord(true);
        } else {
            LOGGER.info("One active key already exists");
        }
    }

    public NrtmKeyRecord generateKeyRecord(final boolean isActive) {
        final AsymmetricCipherKeyPair asymmetricCipherKeyPair = Ed25519Util.generateEd25519KeyPair();
        final byte[] privateKey =((Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getEncoded();
        final byte[] publicKey = ((Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getEncoded();

        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
        final long expires = dateTimeProvider.getCurrentDateTime().plusYears(1).toEpochSecond(ZoneOffset.UTC);

        final NrtmKeyRecord keyRecord = NrtmKeyRecord.of(privateKey, publicKey, isActive, createdTimestamp, expires );
        nrtmKeyConfigDao.saveKeyPair(keyRecord);

        return keyRecord;
    }

    private NrtmKeyRecord getNextkeyPairRecord() {
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

    public NrtmKeyRecord generateOrRotateNextKey() {
        try {
            final NrtmKeyRecord currentActiveKey = nrtmKeyConfigDao.getActiveKeyPair();
            final LocalDateTime currentDateTime = dateTimeProvider.getCurrentDateTime();

            if(currentActiveKey.expires() > currentDateTime.plusDays(7).toEpochSecond(ZoneOffset.UTC)) {
                return null;
            }

            final NrtmKeyRecord nextKey = getNextkeyPairRecord();
            if(nextKey == null) {
              return generateKeyRecord(false);
            }

            if(currentActiveKey.expires() <=  currentDateTime.toEpochSecond(ZoneOffset.UTC)) {
                //Needs to happen in a transaction
                updateNrtmFileRepository.rotateKey(nextKey, currentActiveKey);
                return null;
            }

            return nextKey;

        } catch (final Exception e) {
            LOGGER.error("NRTMv4 key rotation failed", e);
            return null;
        }
    }

}
