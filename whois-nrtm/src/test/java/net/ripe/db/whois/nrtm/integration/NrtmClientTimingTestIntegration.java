package net.ripe.db.whois.nrtm.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.nrtm.client.NrtmImporter;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientTimingTestIntegration extends AbstractNrtmIntegrationBase {
    /*
       The tests in this class assume that the last entry in GRS was 30 days ago
       and the last serial was not updated.
     */

    @Autowired private NrtmImporter nrtmImporter;
    @Autowired private SerialDao serialDao;

    private static final RpslObject MNTNER = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "description: creation\n" +
            "source: TEST");

    private static final RpslObject MNTNER_UPDATED = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "description: modification\n" +
            "source: TEST");


    private static final RpslObject TEST1_MNT = RpslObject.parse("" +
            "mntner: TEST1-MNT\n" +
            "description: first\n" +
            "source: TEST");

    private static final RpslObject TEST2_MNT = RpslObject.parse("" +
            "mntner: TEST2-MNT\n" +
            "description: second\n" +
            "source: TEST");

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
        testDateTimeProvider.setTime(LocalDateTime.now().minusDays(30));
        databaseHelper.addObject(MNTNER);
        databaseHelper.updateObject(MNTNER_UPDATED);

        databaseHelper.addObjectToSource("1-GRS", MNTNER_UPDATED);

        testDateTimeProvider.setTime(LocalDateTime.now());

        nrtmServer.start();
    }

    private void startNrtmImporter() {
        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(NrtmServer.getPort()));
        nrtmImporter.start();
    }

    @After
    public void after() throws Exception {
        nrtmImporter.stop(true);
        nrtmServer.stop(true);
    }

    @Test
    public void initial_dump_does_not_does_not_contain_serial_from_source() {
        databaseHelper.getWhoisTemplate().execute("ALTER TABLE serials AUTO_INCREMENT = 100;");
        databaseHelper.addObject(TEST1_MNT);
        databaseHelper.addObject(TEST2_MNT);

        startNrtmImporter();

        objectExists(TEST1_MNT.getType(), TEST1_MNT.getKey().toString(), false);
        objectExists(TEST2_MNT.getType(), TEST2_MNT.getKey().toString(), false);
    }

    @Test
    public void initial_dump_has_updated_serial_but_is_too_old() {
        updateGrsLastSerialIdFromSource();

        startNrtmImporter();

        databaseHelper.addObject(TEST2_MNT);

        objectExists(TEST2_MNT.getType(), TEST2_MNT.getKey().toString(), false);
    }

    @Test
    public void initial_dump_has_updated_the_last_serial_from_source_and_is_recent() {
        databaseHelper.getWhoisTemplate().execute("ALTER TABLE serials AUTO_INCREMENT = 100;");

        //we make a recent entry to both source and grs.
        databaseHelper.addObject(TEST1_MNT);
        databaseHelper.addObjectToSource("1-GRS", TEST1_MNT);

        updateGrsLastSerialIdFromSource();

        databaseHelper.addObject(TEST2_MNT);

        startNrtmImporter();

        objectExists(TEST1_MNT.getType(), TEST1_MNT.getKey().toString(), true);
        objectExists(TEST2_MNT.getType(), TEST2_MNT.getKey().toString(), true);
    }

    private void updateGrsLastSerialIdFromSource() {
        long lastSerialFromSource = serialDao.getSerials().getEnd();

        databaseHelper.setCurrentSource(Source.master("1-GRS"));

        long lastSerialInGrs = serialDao.getSerials().getEnd();

        databaseHelper.getWhoisTemplate().execute(String.format("ALTER TABLE serials AUTO_INCREMENT = %s;", lastSerialFromSource));
        databaseHelper.getWhoisTemplate().update("UPDATE serials SET serial_id = ? WHERE serial_id = ?;", lastSerialFromSource, lastSerialInGrs);
        sourceContext.removeCurrentSource();
    }
}
