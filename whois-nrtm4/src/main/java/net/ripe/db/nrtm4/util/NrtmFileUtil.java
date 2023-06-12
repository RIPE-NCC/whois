package net.ripe.db.nrtm4.util;

import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import org.apache.commons.lang.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static net.ripe.db.nrtm4.util.ByteArrayUtil.byteArrayToHexString;


public class NrtmFileUtil {

    private static final Random random = new Random();

    public static String newFileName(final NrtmVersionInfo file) {
        final String prefix = file.type().getFileNamePrefix();
        return String.format("%s.%d.%s.%s.%s.json", prefix, file.version(), file.source().getName(), file.sessionID(), randomHexString());
    }

    public static String newGzFileName(final NrtmVersionInfo file) {
        final String fileName = newFileName(file);
        return String.format("%s.gz", fileName);
    }

    public static String getSource(final String filename) {
        final String[] splitFilename = StringUtils.split(filename, ".");
        return splitFilename.length < 3 ? "" :  splitFilename[2];
    }

    public static String calculateSha256(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedSha256hex = digest.digest(bytes);
            return byteArrayToHexString(encodedSha256hex);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String randomHexString() {
        return Long.toHexString(random.nextLong()) + Long.toHexString(random.nextLong());
    }

}
