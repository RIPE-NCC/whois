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
public class NrtmClientMultipleSourcesTestIntegration extends AbstractNrtmIntegrationBase {

    private static final RpslObject MNTNER = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "source: TEST");

    @Autowired protected NrtmImporter nrtmImporter;

    @BeforeClass
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS");
        System.setProperty("nrtm.update.interval", "1");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS,2-GRS,3-GRS");
        System.setProperty("nrtm.import.enabled", "true");
    }

    @Before
    public void before() throws Exception {
        databaseHelper.addObject(MNTNER);
        databaseHelper.addObjectToSource("1-GRS", MNTNER);
        databaseHelper.addObjectToSource("2-GRS", MNTNER);
        databaseHelper.addObjectToSource("3-GRS", MNTNER);
        nrtmServer.start();
        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.port));
        System.setProperty("nrtm.import.2-GRS.source", "TEST");
        System.setProperty("nrtm.import.2-GRS.host", "localhost");
        System.setProperty("nrtm.import.2-GRS.port", Integer.toString(NrtmServer.port));
        System.setProperty("nrtm.import.3-GRS.source", "TEST");
        System.setProperty("nrtm.import.3-GRS.host", "localhost");
        System.setProperty("nrtm.import.3-GRS.port", Integer.toString(NrtmServer.port));
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

        objectExists(ObjectType.PERSON, "OP1-TEST", "1-GRS", true);
        objectExists(ObjectType.PERSON, "OP1-TEST", "2-GRS", true);
        objectExists(ObjectType.PERSON, "OP1-TEST", "3-GRS", true);
    }

    private void objectExists(final ObjectType type, final String key, final String source, final boolean exists) {
        Awaitility.waitAtMost(Duration.FOREVER).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    sourceContext.setCurrent(Source.master(source));
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
}
