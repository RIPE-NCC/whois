package net.ripe.db.nrtm4.client.reader;

import net.ripe.db.nrtm4.client.AbstractNrtmClientIntegrationTest;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class UpdateNotificationFileReaderTestIntegration extends AbstractNrtmClientIntegrationTest {

    @Test
    public void readUNFThenVersionAdded() {
        updateNotificationFileReader.readFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientMirrorRepository.getNrtmLastVersionInfo();
        assertThat(versionInfosPerSource.size(), is(2));
    }

    @Test
    public void readUNFWhenAlreadyCreatedSameVersionThenVersionNotAdded(){
        nrtm4ClientMirrorRepository.saveUpdateNotificationFileVersion("RIPE-NONAUTH", 1, "6328095e-7d46-415b-9333-8f2ae274b7c8");
        nrtm4ClientMirrorRepository.saveUpdateNotificationFileVersion("RIPE", 1, "6328095e-7d46-415b-9333-8f2ae274b7c8");

        updateNotificationFileReader.readFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientMirrorRepository.getNrtmLastVersionInfo();
        assertThat(versionInfosPerSource.getFirst().version(), is(1L));
    }

    @Test
    public void readUNFButDBInfoAheadThenReInitialize(){
        // TODO: [MH] Re-initialize
    }
}
