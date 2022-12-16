package net.ripe.db.nrtm4.util;

import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.publish.PublishableNrtmDocument;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

import static net.ripe.db.nrtm4.NrtmConstants.DELTA_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.NOTIFICATION_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.SNAPSHOT_PREFIX;


@Component
public class NrtmFileUtil {

    private static final String CHARS = "0123456789abcdef";
    private static final int RAND_STRING_LENGTH = 40;

    public String fileName(final PublishableNrtmDocument file) {
        final String prefix = file.getType() == NrtmDocumentType.DELTA ? DELTA_PREFIX
            : file.getType() == NrtmDocumentType.SNAPSHOT ? SNAPSHOT_PREFIX
            : file.getType() == NrtmDocumentType.NOTIFICATION ? NOTIFICATION_PREFIX : "";
        return String.format("%s.%d.%s.json", prefix, file.getVersion(), randomHexString());
    }

    public String hashString(final String payload) {
        return DigestUtils.sha256Hex(payload);
    }

    public String sessionId() {
        return UUID.randomUUID().toString();
    }

    private static String randomHexString() {
        final Random random = new Random();
        return Long.toHexString(random.nextLong()) + Long.toHexString(random.nextLong());
    }

}
