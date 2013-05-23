package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.NrtmClient;
import net.ripe.db.whois.nrtm.NrtmServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-nrtm-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class NrtmClientIntegration extends AbstractNrtmIntegrationBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmClientIntegration.class);

    @Autowired NrtmClient subject;

    @Before
    public void before() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: OWNER-MNT\n" +
                "source: TEST"));
        nrtmServer.start();
        subject.start("localhost", NrtmServer.port);
    }

    @After
    public void after() {
        nrtmServer.stop();
        subject.cleanup();
    }

    @Test
    public void safd() {

    }
    // %ERROR:402: not authorised to mirror the database from IP address 193.0.20.232

    // %ERROR:403: unknown source INVALID

    // %ERROR:401: (Requesting serials older than 14 days will be rejected)
}
