package net.ripe.db.nrtm4.client.processor;

import net.ripe.db.nrtm4.client.AbstractNrtmClientIntegrationTest;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class UNFProcessorWrongPublicKeyTestIntegration extends AbstractNrtmClientIntegrationTest {

    @BeforeAll
    public static void setUp(){
        System.setProperty("nrtm.key", "{\"kty\":\"OKP\",\"crv\":\"Ed25519\",\"kid\":\"35e18000-ce6a-4b72-b92c-d09fc6ec279d\",\"x\":\"ZqHS2UmPulPoWlPIET-nSuXyEkb4N-LKiWZMSj8S2eZ\"}");
    }

    @AfterAll
    public static void tearDown(){
        System.clearProperty("nrtm.key");
    }

    @Test
    public void process_UNF_signature_Then_No_version_added() {
        updateNotificationFileProcessor.processFile();

        final List<NrtmClientVersionInfo> versionInfosPerSource = nrtm4ClientMirrorRepository.getNrtmLastVersionInfoForUpdateNotificationFile();
        assertThat(versionInfosPerSource, is(empty()));
    }
}
