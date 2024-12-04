package net.ripe.db.nrtm4.client.importer;

import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

public abstract class AbstractMirrorImporter implements MirrorImporter {

    final Nrtm4ClientInfoRepository nrtm4ClientInfoRepository;

    final Nrtm4ClientRepository nrtm4ClientRepository;

    public AbstractMirrorImporter(final Nrtm4ClientInfoRepository nrtm4ClientInfoRepository,
                                  final Nrtm4ClientRepository nrtm4ClientRepository){
        this.nrtm4ClientInfoRepository = nrtm4ClientInfoRepository;
        this.nrtm4ClientRepository = nrtm4ClientRepository;
    }

    String calculateSha256(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedSha256hex = digest.digest(bytes);
            return encodeHexString(encodedSha256hex);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public void truncateTables(){
        nrtm4ClientInfoRepository.truncateTables();
        nrtm4ClientRepository.truncateTables();
    }
}
