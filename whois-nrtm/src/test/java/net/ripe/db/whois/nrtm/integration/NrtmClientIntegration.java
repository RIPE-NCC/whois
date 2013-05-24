package net.ripe.db.whois.nrtm.integration;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.NrtmClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-nrtm-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class NrtmClientIntegration extends AbstractNrtmIntegrationBase {

    @Autowired NrtmClient nrtmClient;
    private DummyNrtmServer dummyNrtmServer;

    @Before
    public void before() throws Exception {
        dummyNrtmServer = new DummyNrtmServer();
        dummyNrtmServer.start();
    }

    @After
    public void after() {
        dummyNrtmServer.stop();
        nrtmClient.stop();
    }

    @Test
    public void safd() {

    }
    // %ERROR:402: not authorised to mirror the database from IP address 193.0.20.232

    // %ERROR:403: unknown source INVALID

    // %ERROR:401: (Requesting serials older than 14 days will be rejected)

    @Test
    public void add_person_object() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: TEST");
        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");
        databaseHelper.addObject(mntner);

        dummyNrtmServer.when("-g TEST:3:1-LAST -k",
                "%START Version: 3 TEST 1-2\n\n" +
                "ADD 1\n\n" +
                mntner.toString() + "\n" +
                "ADD 2\n\n" +
                person.toString() + "\n");

        nrtmClient.start("localhost", dummyNrtmServer.getPort());

        lookupObject(ObjectType.PERSON, "OP1-TEST");
    }

    private void lookupObject(final ObjectType type, final String key) {
        Awaitility.waitAtMost(Duration.ONE_SECOND).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    databaseHelper.lookupObject(type, key);
                    return Boolean.TRUE;
                } catch (EmptyResultDataAccessException e) {
                    return Boolean.FALSE;
                }
            }
        }, is(Boolean.TRUE));
    }

}
