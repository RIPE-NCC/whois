package net.ripe.db.nrtm4.util;

import net.ripe.db.nrtm4.domain.NrtmVersionInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static net.ripe.db.nrtm4.util.ByteArrayUtil.byteArrayToHexString;


public class NrtmFileUtil {

    private static final Random random = new Random();

    public static String newFileName(final NrtmVersionInfo file) {
        final String prefix = file.type().getFileNamePrefix();
        return String.format("%s.%d.%s.%s.json.gz", prefix, file.version(), file.source().getName(), randomHexString());
    }

    public static String newGzFileName(final NrtmVersionInfo file) {
        final String fileName = newFileName(file);
        return String.format("%s.gz", fileName);
    }

    public static boolean checkIfFileExists(final String path, final String sessionId, final String name) {
        final File dir = new File(path, sessionId);
        return dir.exists() && new File(dir, name).exists();
    }

    public static FileInputStream getFileInputStream(final String path, final String sessionId, final String name) throws FileNotFoundException {
        final File dir = new File(path, sessionId);
        return new FileInputStream(new File(dir, name));
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
