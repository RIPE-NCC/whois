package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmDocumentType;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


public class NrtmFileUtilTest {

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

    @Test
    void snapshot_file_name_looks_legit() {
        final var file = new PublishableSnapshotFile(testSnapshotVersion);
        final var name = NrtmFileUtil.newFileName(file);
        assertThat(name, startsWith("nrtm-snapshot.22."));
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
        final var result = DigestUtils.sha256Hex(str);
        assertThat(result, is("fc896e4ab60680371e1eec18d544b57339bac463860d8a26c4d075813e61f6fe"));
    }

}
