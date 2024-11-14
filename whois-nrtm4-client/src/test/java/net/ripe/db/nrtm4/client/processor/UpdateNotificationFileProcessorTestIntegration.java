package net.ripe.db.nrtm4.client.processor;

import net.ripe.db.nrtm4.client.AbstractNrtmClientIntegrationTest;
import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class UpdateNotificationFileProcessorTestIntegration extends AbstractNrtmClientIntegrationTest {

    @Test
    public void process_UNF_Then_Version_Added() {
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource.size(), is(2));
    }

    @Test
    public void process_UNF_when_already_Created_Same_Version_Then_Version_Not_Added(){
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE-NONAUTH", 1, "6328095e-7d46-415b-9333-8f2ae274b7c8", "localhost");
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE", 1, "6328095e-7d46-415b-9333-8f2ae274b7c8", "localhost");

        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource.getFirst().version(), is(1L));
    }

    @Test
    public void first_UNF_then_snapshot_persisted(){
        updateNotificationFileProcessor.processFile();
        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource.size(), is(2));

        final List<MirrorRpslObject> mirroredRpslObjects = getMirrorRpslObject();
        final List<NrtmClientVersionInfo> snapshotVersionPerSource = getNrtmLastSnapshotVersion();
        assertThat(mirroredRpslObjects.isEmpty(), is(false));
        assertThat(snapshotVersionPerSource.size(), is(2));

        assertSnapshotFirstVersion(snapshotVersionPerSource.getFirst(), "RIPE");
        assertSnapshotFirstVersion(snapshotVersionPerSource.get(1), "RIPE-NONAUTH");
    }

    @Test
    public void second_UNF_then_no_new_snapshot(){
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> snapshotVersionPerSource = getNrtmLastSnapshotVersion();
        assertSnapshotFirstVersion(snapshotVersionPerSource.getFirst(), "RIPE");
        assertSnapshotFirstVersion(snapshotVersionPerSource.get(1), "RIPE-NONAUTH");

        updateNotificationFileProcessor.processFile();
        assertSnapshotFirstVersion(snapshotVersionPerSource.getFirst(), "RIPE");
        assertSnapshotFirstVersion(snapshotVersionPerSource.get(1), "RIPE-NONAUTH");
    }

    @Test
    public void UNF_with_different_hash_then_no_snapshot(){
        nrtmServerDummy.setFakeHashMocks();
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> snapshotVersionPerSource = getNrtmLastSnapshotVersion();
        assertThat(snapshotVersionPerSource, is(empty()));
    }

    @Test
    public void process_UNF_but_DB_Ahead_Then_ReInitialize(){
        // TODO: [MH] Re-initialize
    }

    // Helper Methods
    private static void assertSnapshotFirstVersion(final NrtmClientVersionInfo snapshotVersionPerSource, final String source) {
        assertThat(snapshotVersionPerSource.source(), is(source));
        assertThat(snapshotVersionPerSource.version(), is(1L));
    }
}
