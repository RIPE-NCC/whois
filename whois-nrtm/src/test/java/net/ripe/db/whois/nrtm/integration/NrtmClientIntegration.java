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
    public void add_person() throws Exception {
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
        nrtmClient.start("localhost", dummyNrtmServer.getPort());

        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    @Test
    public void add_person_gap_in_serials() throws Exception {
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
        nrtmClient.start("localhost", dummyNrtmServer.getPort());

        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    @Test
    public void delete_person() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: TEST");
        databaseHelper.addObject(mntner);
        dummyNrtmServer.addObject(1, mntner);
        dummyNrtmServer.deleteObject(2, mntner);
        nrtmClient.start("localhost", dummyNrtmServer.getPort());

        objectExists(ObjectType.PERSON, "OP1-TEST", false);
    }

    @Test
    public void update_person() throws Exception {
        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "source: TEST");
        databaseHelper.addObject(person);
        dummyNrtmServer.addObject(1, person);

        nrtmClient.start("localhost", dummyNrtmServer.getPort());

        final RpslObject update = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "remarks: updated\n" +
                "source: TEST");
        dummyNrtmServer.addObject(2, update);

        objectMatches(update);
    }

    @Test
    public void unexpected_server_disconnect() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: TEST");
        databaseHelper.addObject(mntner);
        dummyNrtmServer.addObject(1, mntner);

        nrtmClient.start("localhost", dummyNrtmServer.getPort());

        Thread.sleep(1000);
        dummyNrtmServer.stop();
        Thread.sleep(1000);

        final RpslObject person = RpslObject.parse(
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");
        dummyNrtmServer.addObject(2, person);
        dummyNrtmServer.start();

        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    private void objectExists(final ObjectType type, final String key, final boolean exists) {
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
        }, is(exists));
    }

    private void objectMatches(final RpslObject rpslObject) {
        Awaitility.waitAtMost(Duration.ONE_SECOND).until(new Callable<RpslObject>() {
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
