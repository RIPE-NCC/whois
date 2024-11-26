package net.ripe.db.nrtm4.client.processor;

import net.ripe.db.nrtm4.client.AbstractNrtmClientIntegrationTest;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;


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
    public void apply_deltas_then_last_mirror_updated() {
        updateNotificationFileProcessor.processFile();
        final RpslObject route = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final Integer route6 = nrtm4ClientRepository.getMirroredObjectId("2001:490:c000::/35AS18666");
        final Integer mntner = nrtm4ClientRepository.getMirroredObjectId("MHM-MNT");

        assertThat(route6, is(not(nullValue())));
        assertThat(route.findAttribute(AttributeType.DESCR).getCleanValue(), is("Dummified"));
        assertThat(mntner, is(nullValue()));

        nrtmServerDummy.setSecondUNFMocks();
        updateNotificationFileProcessor.processFile();
        final RpslObject updatedroute = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final Integer deletedRoute6 = nrtm4ClientRepository.getMirroredObjectId("2001:490:c000::/35AS18666");
        final Integer createdMntner = nrtm4ClientRepository.getMirroredObjectId("MHM-MNT");

        assertThat(createdMntner, is(not(nullValue())));
        assertThat(deletedRoute6, is(nullValue()));
        assertThat(route, is(not(updatedroute)));
        assertThat(updatedroute.findAttribute(AttributeType.DESCR).getCleanValue(), is("SECOND DELTA DUMMY"));
    }


    @Test
    public void wrongly_signed_UNF_then_no_UNF_added(){
        nrtmServerDummy.setWrongSignedUNF();
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource, is(empty()));
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
