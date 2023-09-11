package net.ripe.db.nrtm4.generator;

import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.dao.NrtmSourceDao;
import net.ripe.db.nrtm4.util.Ed25519Util;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.groupingBy;


@Service
public class SnapshotFileInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileInitializer.class);
    private final SnapshotFileGenerator snapshotFileGenerator;
    private final NrtmSourceDao nrtmSourceDao;
    private final NrtmKeyConfigDao nrtmKeyConfigDao;

    public SnapshotFileInitializer(
        final SnapshotFileGenerator snapshotFileGenerator,
        final NrtmKeyConfigDao nrtmKeyConfigDao,
        final NrtmSourceDao nrtmSourceDao
    ) {
        this.snapshotFileGenerator = snapshotFileGenerator;
        this.nrtmSourceDao = nrtmSourceDao;

        this.nrtmKeyConfigDao = nrtmKeyConfigDao;
    }

    public void initialize()  {
        if(!canProceed()) {
            return;
        }

        LOGGER.info("Initializing Nrtm Database...");
        createKeyPair();
        nrtmSourceDao.createSources();

        snapshotFileGenerator.createSnapshot();
    }

    private void createKeyPair() {
        if(!nrtmKeyConfigDao.isKeyPairExists()) {
            final AsymmetricCipherKeyPair asymmetricCipherKeyPair = Ed25519Util.generateEd25519KeyPair();
            final byte[] privateKey =((Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getEncoded();
            final byte[] publicKey = ((Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getEncoded();

            nrtmKeyConfigDao.saveKeyPair(privateKey, publicKey);
        }
    }

    private boolean canProceed() {
        return nrtmSourceDao.getSources().isEmpty();
    }
}
