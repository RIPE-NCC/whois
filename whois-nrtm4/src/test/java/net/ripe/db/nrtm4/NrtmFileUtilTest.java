package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.publish.PublishableDeltaFile;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


public class NrtmFileUtilTest {

    NrtmFileUtil nrtmFileUtil;
    private final NrtmVersionInfo testSnapshotVersion = new NrtmVersionInfo(
        21L,
        new NrtmSource("TEST"),
        22L,
        "1234567890abcdef",
        NrtmDocumentType.SNAPSHOT,
        123123
    );

    private final NrtmVersionInfo testDeltaVersion = new NrtmVersionInfo(
        21L,
        new NrtmSource("TEST"),
        22L,
        "1234567890abcdef",
        NrtmDocumentType.DELTA,
        123123
    );

    @BeforeEach
    void setup() {
        nrtmFileUtil = new NrtmFileUtil();
    }

    @Test
    void snapshot_file_name_looks_legit() {
        final var file = new PublishableSnapshotFile(testSnapshotVersion);
        final var name = nrtmFileUtil.fileName(file);
        assertThat(name, startsWith("nrtm-snapshot.22."));
    }

    @Test
    void delta_file_name_looks_legit() {
        final var file = new PublishableDeltaFile(testDeltaVersion, List.of());
        final var name = nrtmFileUtil.fileName(file);
        assertThat(name, startsWith("nrtm-delta.22."));
    }

    @Test
    void session_ids_are_unique() {
        final var listOfSessionIds = new ArrayList<>();
        for (int i = 0; i < 50_000; i++) {
            listOfSessionIds.add(nrtmFileUtil.sessionId());
        }
        final var setOfSessionIds = Set.copyOf(listOfSessionIds);
        assertThat(setOfSessionIds.size(), is(listOfSessionIds.size()));
    }

    @Test
    void hash256_works_as_expected() {
        final var str = "" +
            "{\"nrtm_version\":4," +
            "\"type\":\"snapshot\"," +
            "\"source\":\"TEST\"," +
            "\"version\":1," +
            "\"objects\":[" +
            "\"inetnum:        195.77.187.144 - 195.77.187.151\\nnetname:        Netname\\ndescr:          Description\\ncountry:        es\\nadmin-c:        DUMY-RIPE\\ntech-c:         DUMY-RIPE\\nstatus:         ASSIGNED PA\\nmnt-by:         MAINT-AS3352\\nsource:         RIPE\\nremarks:        ****************************\\nremarks:        * THIS OBJECT IS MODIFIED\\nremarks:        * Please note that all data that is generally regarded as personal\\nremarks:        * data has been removed from this object.\\nremarks:        * To view the original object, please query the RIPE Database at:\\nremarks:        * http://www.ripe.net/whois\\nremarks:        ****************************\\n\"" +
            "]}";
        final var result = nrtmFileUtil.hashString(str);
        assertThat(result, is("fc896e4ab60680371e1eec18d544b57339bac463860d8a26c4d075813e61f6fe"));
    }

}
