package net.ripe.db.nrtm4.client.processor;

import net.ripe.db.nrtm4.client.AbstractNrtmClientIntegrationTest;
import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import net.ripe.db.nrtm4.client.dao.NrtmClientDocumentType;
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
        final List<NrtmClientVersionInfo> snapshotVersionPerSource = getNrtmLastFileVersion(NrtmClientDocumentType.SNAPSHOT);
        assertThat(mirroredRpslObjects.isEmpty(), is(false));
        assertThat(snapshotVersionPerSource.size(), is(2));

        assertSnapshotFirstVersion(snapshotVersionPerSource.getFirst(), "RIPE");
        assertSnapshotFirstVersion(snapshotVersionPerSource.get(1), "RIPE-NONAUTH");
    }

    @Test
    public void second_UNF_then_no_new_snapshot(){
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> snapshotVersionPerSource = getNrtmLastFileVersion(NrtmClientDocumentType.SNAPSHOT);
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

        final List<NrtmClientVersionInfo> snapshotVersionPerSource = getNrtmLastFileVersion(NrtmClientDocumentType.SNAPSHOT);
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
        final RpslObject updatedRoute = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final RpslObject deletedRoute6 = getMirrorRpslObjectByPkey("2001:490:c000::/35AS18666");
        final RpslObject createdMntner = getMirrorRpslObjectByPkey("MHM-MNT");

        assertThat(createdMntner, is(not(nullValue())));
        assertThat(deletedRoute6, is(nullValue()));
        assertThat(updatedRoute, is(not(nullValue())));
        assertThat(route, is(not(updatedRoute)));
        assertThat(updatedRoute.findAttribute(AttributeType.DESCR).getCleanValue(), is("SECOND DELTA DUMMY"));
    }

    @Test
    public void apply_unf_with_multiple_deltas_then_updated() {
        updateNotificationFileProcessor.processFile();
        final RpslObject route = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final RpslObject route6 = getMirrorRpslObjectByPkey("2001:490:c000::/35AS18666");
        final RpslObject mntner = getMirrorRpslObjectByPkey("MHM-MNT");

        assertThat(route6, is(not(nullValue())));
        assertThat(route, is(not(nullValue())));
        assertThat(route.findAttribute(AttributeType.DESCR).getCleanValue(), is("Dummified"));
        assertThat(mntner, is(nullValue()));

        nrtmServerDummy.setTwoAndThreeVersionDeltasMocks();
        updateNotificationFileProcessor.processFile();
        final RpslObject autnum = getMirrorRpslObjectByPkey("AS211871");
        final RpslObject deletedRoute6 = getMirrorRpslObjectByPkey("2001:490:f000::/36AS1248");
        final RpslObject createdMntner = getMirrorRpslObjectByPkey("MHM-MNT");

        assertThat(createdMntner, is(nullValue()));
        assertThat(deletedRoute6, is(nullValue()));
        assertThat(autnum, is(not(nullValue())));

        final List<NrtmClientVersionInfo> versions = getNrtmLastFileVersion(NrtmClientDocumentType.DELTA);
        assertThat(versions.getFirst().version(), is(3L));
    }

    @Test
    public void apply_unf_with_discontinuous_deltas_then_discarded() {
        updateNotificationFileProcessor.processFile();
        final RpslObject route = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final RpslObject route6 = getMirrorRpslObjectByPkey("2001:490:c000::/35AS18666");
        final RpslObject mntner = getMirrorRpslObjectByPkey("MHM-MNT");

        assertThat(route6, is(not(nullValue())));
        assertThat(route, is(not(nullValue())));
        assertThat(route.findAttribute(AttributeType.DESCR).getCleanValue(), is("Dummified"));
        assertThat(mntner, is(nullValue()));

        nrtmServerDummy.setTwoAndFourVersionDeltasMocks();
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versions = getNrtmLastFileVersion(NrtmClientDocumentType.DELTA);

        assertThat(versions.getFirst().version(), is(1L));
    }

    @Test
    public void apply_unf_with_ahead_deltas_then_reinitialise_from_snap_and_reapply() {
        updateNotificationFileProcessor.processFile();
        final RpslObject route = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final RpslObject route6 = getMirrorRpslObjectByPkey("2001:490:c000::/35AS18666");
        final RpslObject mntner = getMirrorRpslObjectByPkey("MHM-MNT");

        assertThat(route6, is(not(nullValue())));
        assertThat(route, is(not(nullValue())));
        assertThat(route.findAttribute(AttributeType.DESCR).getCleanValue(), is("Dummified"));
        assertThat(mntner, is(nullValue()));

        //Put the database deltas ahead
        nrtm4ClientInfoRepository.saveDeltaFileVersion("RIPE-NONAUTH", 5, "6328095e-7d46-415b-9333-8f2ae274b7c8");
        nrtm4ClientInfoRepository.saveDeltaFileVersion("RIPE", 5, "4521174b-548f-4e51-98fc-dfd720011a0c");

        nrtmServerDummy.setAllDeltas();
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versions = getNrtmLastFileVersion(NrtmClientDocumentType.DELTA);

        assertThat(versions.getFirst().version(), is(4L));

        final RpslObject deleteDomain = getMirrorRpslObjectByPkey("7.4.1.8.0.1.a.2.ip6.arpa");
        final RpslObject createdMntner = getMirrorRpslObjectByPkey("MHM3-MNT");
        final RpslObject createdInet6num = getMirrorRpslObjectByPkey("2a00:2381:c3be::/48");
        final RpslObject updatedRoute = getMirrorRpslObjectByPkey("176.240.50.0/24AS47524");
        final RpslObject createdAutnum = getMirrorRpslObjectByPkey("AS211871");
        final RpslObject createdMntnerMHM = getMirrorRpslObjectByPkey("MHM-MNT");

        final RpslObject deleteRoute = getMirrorRpslObjectByPkey("205.203.119.0/24AS15951");
        final RpslObject createdNokiaMntner = getMirrorRpslObjectByPkey("NOKIA-NOC");
        final RpslObject deletedRoute6= getMirrorRpslObjectByPkey("2001:490:c000::/35AS18666");
        final RpslObject createdRoute6= getMirrorRpslObjectByPkey("2001:490:f000::/36AS1248");


        assertThat(deleteDomain, is(nullValue()));
        assertThat(createdMntner, is(not(nullValue())));
        assertThat(createdInet6num, is(not(nullValue())));
        assertThat(updatedRoute, is(not(nullValue())));
        assertThat(updatedRoute.findAttribute(AttributeType.DESCR).getCleanValue(), is("SECOND DELTA DUMMY"));
        assertThat(createdAutnum, is(not(nullValue())));
        assertThat(createdMntnerMHM, is(not(nullValue())));

        assertThat(deleteRoute, is(nullValue()));
        assertThat(createdNokiaMntner, is(not(nullValue())));
        assertThat(deletedRoute6, is(nullValue()));
        assertThat(createdRoute6, is(not(nullValue())));
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
