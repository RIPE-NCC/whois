package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;


public class NrtmFileUtilTest {

    private final NrtmVersionInfo testSnapshotVersion = new NrtmVersionInfo(
        21L,
        new NrtmSource("TEST"),
        22L,
        "1234567890abcdef",
        NrtmDocumentType.SNAPSHOT,
        123123,
        0
    );

    @Test
    void snapshot_file_name_looks_legit() {
        final var file = new PublishableSnapshotFile(testSnapshotVersion);
        final var name = NrtmFileUtil.newFileName(file);
        assertThat(name, startsWith("nrtm-snapshot.22."));
    }

}
