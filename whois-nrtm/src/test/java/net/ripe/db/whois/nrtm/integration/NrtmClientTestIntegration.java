package net.ripe.db.whois.nrtm.integration;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.ServerHelper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.nrtm.client.NrtmClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
public class NrtmClientTestIntegration extends AbstractNrtmIntegrationBase {

    @Autowired
    NrtmClient nrtmClient;
    private DummyNrtmServer dummyNrtmServer;

    private static int port = ServerHelper.getAvailablePort();

    @BeforeClass
    public static void beforeClass() {
        // TODO: test with multiple sources
        System.setProperty("nrtm.import.sources", "TEST-GRS");
        System.setProperty("nrtm.import.enabled", "true");
        System.setProperty("nrtm.import.TEST-GRS.host", "localhost");
        System.setProperty("nrtm.import.TEST-GRS.port", Integer.toString(port));
    }

    @Before
    public void before() throws Exception {
        databaseHelper.setCurrentSource(Source.master("TEST-GRS"));

        dummyNrtmServer = new DummyNrtmServer(port);
        dummyNrtmServer.start();
    }

    @After
    public void after() throws Exception {
        dummyNrtmServer.stop();
    }

    @Test
    public void add_person_from_nrtm() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: TEST");
        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(mntner);
        dummyNrtmServer.addObject(1, mntner);
        dummyNrtmServer.addObject(2, person);

        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    @Test
    public void add_person_from_nrtm_gap_in_serials() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: TEST");
        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "source: TEST");

        databaseHelper.addObject(mntner);
        dummyNrtmServer.addObject(1, mntner);
        dummyNrtmServer.addObject(5, person);

        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    @Test
    public void delete_person_from_nrtm() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(mntner);
        dummyNrtmServer.addObject(1, mntner);
        dummyNrtmServer.deleteObject(2, mntner);

        objectExists(ObjectType.PERSON, "OP1-TEST", false);
    }

    @Test
    public void create_and_update_person_from_nrtm() throws Exception {
        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "remarks: updated\n" +
                "source: TEST");

        dummyNrtmServer.addObject(1, person);
        dummyNrtmServer.addObject(2, update);

        objectMatches(update);
    }

    @Test
    public void update_existing_person_from_nrtm() throws Exception {

        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "source: TEST");
        final RpslObject update = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "remarks: updated\n" +
                "source: TEST");

        databaseHelper.addObject(person);
        dummyNrtmServer.addObject(1, person);
        dummyNrtmServer.addObject(2, update);

        objectMatches(update);
    }

    @Ignore("TODO: nrtm client isn't detecting socket close on (dummy) server side")  // TODO: [ES]
    @Test
    public void create_mntner_network_error_then_create_person() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: TEST");
        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        dummyNrtmServer.addObject(1, mntner);
        objectExists(ObjectType.MNTNER, "OWNER-MNT", true);

        dummyNrtmServer.stop();
        dummyNrtmServer.addObject(2, person);
        dummyNrtmServer.start();

        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    private void objectExists(final ObjectType type, final String key, final boolean exists) {
        Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    databaseHelper.lookupObject(type, key);
                    return Boolean.TRUE;
                } catch (EmptyResultDataAccessException e) {
                    return Boolean.FALSE;
                }
            }
        }, is(exists));
    }

    private void objectMatches(final RpslObject rpslObject) {
        Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(new Callable<RpslObject>() {
            @Override
            public RpslObject call() throws Exception {
                try {
                    return databaseHelper.lookupObject(rpslObject.getType(), rpslObject.getKey().toString());
                } catch (EmptyResultDataAccessException e) {
                    return null;
                }
            }
        }, is(rpslObject));
    }
}
