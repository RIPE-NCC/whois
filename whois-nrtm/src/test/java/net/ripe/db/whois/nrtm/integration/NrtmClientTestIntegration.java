package net.ripe.db.whois.nrtm.integration;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.nrtm.client.NrtmImporter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

@Category(IntegrationTest.class)
public class NrtmClientTestIntegration extends AbstractNrtmIntegrationBase {
    private static final RpslObject MNTNER = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "source: TEST");

    @Autowired protected NrtmImporter nrtmImporter;

    @BeforeClass
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS");
        System.setProperty("nrtm.update.interval", "1");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS");
        System.setProperty("nrtm.import.enabled", "true");
    }

    @Before
    public void before() throws Exception {
        databaseHelper.addObject(MNTNER);
        databaseHelper.addObjectToSource("1-GRS", MNTNER);

        nrtmServer.start();

        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.port));
        nrtmImporter.start();
    }

    @After
    public void after() throws Exception {
        nrtmImporter.stop();
        nrtmServer.stop();
    }

    @Test
    public void add_person_from_nrtm() throws Exception {
        final RpslObject person = RpslObject.parse("" +
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");


        databaseHelper.addObject(person);
        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    @Test
    public void add_person_from_nrtm_gap_in_serials() throws Exception {
        final RpslObject person = RpslObject.parse("" +
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "source: TEST");

        nrtmImporter.stop();
        nrtmServer.stop();

        final RpslObject rpslObject = databaseHelper.addObject("mntner: MNT1");
        databaseHelper.getWhoisTemplate().update("delete from serials where object_id = ?", rpslObject.getObjectId());

        databaseHelper.addObject(person);

        nrtmServer.start();
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.port));
        nrtmImporter.start();

        objectExists(ObjectType.PERSON, "OP1-TEST", true);
    }

    @Test
    public void delete_maintainer_from_nrtm() throws Exception {
        databaseHelper.removeObject(MNTNER);

        objectExists(ObjectType.MNTNER, "OWNER-MNT", false);
    }

    @Test
    public void create_and_update_person_from_nrtm() throws Exception {
        final RpslObject person = RpslObject.parse("" +
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "source: TEST");

        final RpslObject update = RpslObject.parse("" +
                "person:         Name Removed\n" +
                "nic-hdl:        OP1-TEST\n" +
                "remarks:        updated\n" +
                "source:         TEST");

        databaseHelper.addObject(person);
        databaseHelper.updateObject(update);

        objectMatches(update);
    }

    @Test
    public void update_existing_person_from_nrtm() throws Exception {
        final RpslObject person = RpslObject.parse("" +
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "source: TEST");

        final RpslObject update = RpslObject.parse("" +
                "person:         Name Removed\n" +
                "nic-hdl:        OP1-TEST\n" +
                "remarks:        updated\n" +
                "source:         TEST");

        databaseHelper.addObject(person);
        databaseHelper.updateObject(update);

        objectMatches(update);
    }

    @Test
    public void network_error() throws Exception {
        final RpslObject person = RpslObject.parse("" +
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(person);
        objectExists(ObjectType.PERSON, "OP1-TEST", true);

        nrtmImporter.stop();
        nrtmServer.stop();

        final RpslObject person2 = RpslObject.parse("" +
                "person: Two Person\n" +
                "nic-hdl: OP2-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(person2);
        nrtmServer.start();
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.port));
        nrtmImporter.start();

        objectExists(ObjectType.PERSON, "OP2-TEST", true);
    }

    private void objectExists(final ObjectType type, final String key, final boolean exists) {
        Awaitility.waitAtMost(Duration.FOREVER).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    sourceContext.setCurrent(Source.master("1-GRS"));
                    databaseHelper.lookupObject(type, key);
                    return Boolean.TRUE;
                } catch (EmptyResultDataAccessException e) {
                    return Boolean.FALSE;
                } finally {
                    sourceContext.removeCurrentSource();
                }
            }
        }, is(exists));
    }

    private void objectMatches(final RpslObject rpslObject) {
        Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(new Callable<RpslObject>() {
            @Override
            public RpslObject call() throws Exception {
                try {
                    sourceContext.setCurrent(Source.master("1-GRS"));
                    return databaseHelper.lookupObject(rpslObject.getType(), rpslObject.getKey().toString());
                } catch (EmptyResultDataAccessException e) {
                    return null;
                } finally {
                    sourceContext.removeCurrentSource();
                }
            }
        }, is(rpslObject));
    }
}
