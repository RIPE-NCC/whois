package net.ripe.db.nrtm4.util;

import net.ripe.db.nrtm4.dao.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.PublishableNrtmDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.UUID;

import static net.ripe.db.nrtm4.NrtmConstants.DELTA_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.NOTIFICATION_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.SNAPSHOT_PREFIX;


public class NrtmFileUtil {

    public static String newFileName(final PublishableNrtmDocument file) {
        final String prefix = file.getType() == NrtmDocumentType.DELTA ? DELTA_PREFIX
            : file.getType() == NrtmDocumentType.SNAPSHOT ? SNAPSHOT_PREFIX
            : file.getType() == NrtmDocumentType.NOTIFICATION ? NOTIFICATION_PREFIX : "";
        return String.format("%s.%d.%s.json", prefix, file.getVersion(), randomHexString());
    }

    public static String sessionId() {
        return UUID.randomUUID().toString();
    }

    public static boolean checkIfFileExists(final String path, final String sessionId, final String name) {
        final File dir = new File(path, sessionId);
        return dir.exists() && new File(dir, name).exists();
    }

    public static FileInputStream getFileInputStream(final String path, final String sessionId, final String name) throws FileNotFoundException {
        final File dir = new File(path, sessionId);
        return new FileInputStream(new File(dir, name));
    }

    private static String randomHexString() {
        final Random random = new Random();
        return Long.toHexString(random.nextLong()) + Long.toHexString(random.nextLong());
    }

}
