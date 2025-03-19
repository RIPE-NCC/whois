package net.ripe.db.nrtm4.client.processor;

import net.ripe.db.nrtm4.client.AbstractNrtmClientIntegrationTest;
import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
    public void process_UNF_then_version_added() {
        updateNotificationFileProcessor.processFile();
        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource.size(), is(2));
    }

    @Test
    public void process_UNF_when_already_created_same_version_then_version_not_added(){
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE-NONAUTH", 1, "6328095e-7d46-415b-9333-8f2ae274b7c8", "localhost");
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE", 1, "4521174b-548f-4e51-98fc-dfd720011a0c", "localhost");

        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource.getFirst().version(), is(1L));
    }

    @Test
    public void process_UNF_but_DB_ahead_then_reInitialize(){
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE-NONAUTH", 2, "6328095e-7d46-415b-9333-8f2ae274b7c8", "localhost");
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE", 2, "4521174b-548f-4e51-98fc-dfd720011a0c", "localhost");

        final List<NrtmClientVersionInfo> versionBeforeCleanUp = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionBeforeCleanUp.getFirst().version(), is(2L));

        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource.isEmpty(), is(true));
    }

    @Test
    public void process_UNF_but_different_session_id_then_reInitialize(){
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE-NONAUTH", 1, "wrong", "localhost");
        nrtm4ClientInfoRepository.saveUpdateNotificationFileVersion("RIPE", 1, "wrong", "localhost");

        final List<NrtmClientVersionInfo> versionBeforeCleanUp = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionBeforeCleanUp.getFirst().version(), is(1L));

        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource.isEmpty(), is(true));
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
    public void apply_deltas_then_updated() {
        updateNotificationFileProcessor.processFile();
        final RpslObject route = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final RpslObject route6 = getMirrorRpslObjectByPkey("2001:490:c000::/35AS18666");
        final RpslObject mntner = getMirrorRpslObjectByPkey("MHM-MNT");

        assertThat(route6, is(not(nullValue())));
        assertThat(route, is(not(nullValue())));
        assertThat(route.findAttribute(AttributeType.DESCR).getCleanValue(), is("Dummified"));
        assertThat(mntner, is(nullValue()));

        nrtmServerDummy.setSecondDeltasMocks();
        updateNotificationFileProcessor.processFile();
        final RpslObject updatedroute = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final RpslObject deletedRoute6 = getMirrorRpslObjectByPkey("2001:490:c000::/35AS18666");
        final RpslObject createdMntner = getMirrorRpslObjectByPkey("MHM-MNT");

        assertThat(createdMntner, is(not(nullValue())));
        assertThat(deletedRoute6, is(nullValue()));
        assertThat(updatedroute, is(not(nullValue())));
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

    // Helper Methods
    private static void assertSnapshotFirstVersion(final NrtmClientVersionInfo snapshotVersionPerSource, final String source) {
        assertThat(snapshotVersionPerSource.source(), is(source));
        assertThat(snapshotVersionPerSource.version(), is(1L));
    }
}
