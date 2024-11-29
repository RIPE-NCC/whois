package net.ripe.db.nrtm4.client.importer;

import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

public interface Importer {

    void doImport(final String source, final UpdateNotificationFileResponse updateNotificationFile);

    default String calculateSha256(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedSha256hex = digest.digest(bytes);
            return encodeHexString(encodedSha256hex);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
