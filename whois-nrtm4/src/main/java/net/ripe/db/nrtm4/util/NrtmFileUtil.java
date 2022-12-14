package net.ripe.db.nrtm4.util;

import com.google.common.hash.Hashing;
import net.ripe.db.nrtm4.publish.PublishableDeltaFile;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

import static net.ripe.db.nrtm4.NrtmConstants.DELTA_PREFIX;
import static net.ripe.db.nrtm4.NrtmConstants.SNAPSHOT_PREFIX;


@Service
public class NrtmFileUtil {

    private static final String chars = "0123456789abcdef";

    public String fileName(final PublishableDeltaFile file) {
        return String.format("%s.%d.%s.json", DELTA_PREFIX, file.getVersion(), randomHexString());
    }

    public String fileName(final PublishableSnapshotFile file) {
        return String.format("%s.%d.%s.json", SNAPSHOT_PREFIX, file.getVersion(), randomHexString());
    }

    public String hashString(final String payload) {
        return Hashing.sha256()
            .hashString(payload, StandardCharsets.UTF_8)
            .toString();
    }

    public String sessionId() {
        return UUID.randomUUID().toString();
    }

    private static String randomHexString() {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < 40; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
