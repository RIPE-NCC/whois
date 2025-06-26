package net.ripe.db.whois.nrtm.integration;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.nrtm.client.NrtmImporter;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientTestIntegration extends AbstractNrtmIntegrationBase {

    private static final RpslObject MNTNER = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "source: TEST");

    @Autowired protected NrtmImporter nrtmImporter;
    @Autowired protected SourceContext sourceContext;

    @BeforeAll
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS");
        System.setProperty("nrtm.update.interval", "1");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS");
        System.setProperty("nrtm.import.enabled", "true");
    }

    @BeforeEach
    public void before() throws InterruptedException {
        databaseHelper.addObject(MNTNER);
        databaseHelper.addObjectToSource("1-GRS", MNTNER);

        nrtmServer.start();

        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(nrtmServer.getPort()));
        nrtmImporter.start();
    }

    @AfterEach
    public void after() throws InterruptedException {
        nrtmImporter.stop(true);
        nrtmServer.stop(true);
    }


    @Test
    public void check_mntner_exists() {
        objectExists(ObjectType.MNTNER, "OWNER-MNT", true);
    }


    @Test
    public void add_person_from_nrtm() {
        final RpslObject person = RpslObject.parse("" +
                "person: One Person\n" +
                "nic-hdl: OP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(person);

        // should not exist, is filtered
        objectExists(ObjectType.PERSON, "OP1-TEST", false);
    }

    @Test
    public void add_mntner_from_nrtm_gap_in_serials() throws InterruptedException {
        final RpslObject mntner = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "source: TEST");

        nrtmImporter.stop(true);
        nrtmServer.stop(true);

        final RpslObject rpslObject = databaseHelper.addObject("mntner: TEST2-MNT\nsource: TEST");
        databaseHelper.getWhoisTemplate().update("delete from serials where object_id = ?", rpslObject.getObjectId());

        databaseHelper.addObject(mntner);

        nrtmServer.start();

        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(nrtmServer.getPort()));
        nrtmImporter.start();

        objectExists(ObjectType.MNTNER, "TEST-MNT", true);
    }

    @Test
    public void delete_maintainer_from_nrtm() {
        databaseHelper.deleteObject(MNTNER);

        objectExists(ObjectType.MNTNER, "OWNER-MNT", false);
    }

    @Test // and also check dummification remarks are set correctly
    public void create_and_update_mntner_from_nrtm() {

        final RpslObject mntner = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        final RpslObject mntner2 = RpslObject.parse("" +
                "mntner: TEST2-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        final RpslObject update = RpslObject.parse("" +
                "mntner:         TEST-MNT\n" +
                "mnt-by:         TEST2-MNT\n" +
                "source:         TEST");

        databaseHelper.addObject(mntner);
        databaseHelper.addObject(mntner2);
        databaseHelper.updateObject(update);
        RpslObject updateAppended = appendDummificationRemarks(update);
        objectMatches(updateAppended);
    }

    @Test
    public void network_error() throws InterruptedException {
        final RpslObject mntner1 = RpslObject.parse("" +
                "mntner: TEST1-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(mntner1);
        objectExists(ObjectType.MNTNER, "TEST1-MNT", true);

        nrtmImporter.stop(true);
        nrtmServer.stop(true);

        final RpslObject mntner2 = RpslObject.parse("" +
                "mntner: TEST2-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(mntner2);
        nrtmServer.start();
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(nrtmServer.getPort()));
        nrtmImporter.start();

        objectExists(ObjectType.MNTNER, "TEST2-MNT", true);
    }

    @Test
    public void ensure_all_changes_of_object_are_imported_with_no_missing_references() throws InterruptedException {
        final RpslObject test1mntA = RpslObject.parse("" +
                "mntner: TEST1-MNT\n" +
                "mnt-ref: OWNER-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        final RpslObject test2mnt = RpslObject.parse("" +
                "mntner: TEST2-MNT\n" +
                "mnt-ref: OWNER-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        final RpslObject test1mntB = RpslObject.parse("" +
                "mntner: TEST1-MNT\n" +
                "mnt-ref: TEST2-MNT\n" +
                "mnt-by: TEST2-MNT\n" +
                "source: TEST");

        nrtmImporter.stop(true);
        nrtmServer.stop(true);

        databaseHelper.addObject(test1mntA);
        databaseHelper.addObject(test2mnt);
        databaseHelper.updateObject(test1mntB);

        nrtmServer.start();

        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(nrtmServer.getPort()));
        nrtmImporter.start();

        objectExists(ObjectType.MNTNER, "TEST2-MNT", true);
        objectExists(ObjectType.MNTNER, "TEST1-MNT", true);
    }

    // helper methods

    private RpslObject appendDummificationRemarks(final RpslObject rpslObject) {
        final List<RpslAttribute> attributes = Lists.newArrayList(rpslObject.getAttributes());

        attributes.addAll(getRipeDummificationRemarks());
        return new RpslObject(rpslObject, attributes);
    }

    private List<RpslAttribute> getRipeDummificationRemarks() {
        return Lists.newArrayList(
                new RpslAttribute("remarks", "        ****************************"),
                new RpslAttribute("remarks", "        * THIS OBJECT IS MODIFIED"),
                new RpslAttribute("remarks", "        * Please note that all data that is generally regarded as personal"),
                new RpslAttribute("remarks", "        * data has been removed from this object."),
                new RpslAttribute("remarks", "        * To view the original object, please query the RIPE Database at:"),
                new RpslAttribute("remarks", "        * http://www.ripe.net/whois"),
                new RpslAttribute("remarks", "        ****************************"));
    }

    private void objectMatches(final RpslObject rpslObject) {
        Awaitility.waitAtMost(5L, TimeUnit.SECONDS).until(new Callable<RpslObject>() {
            @Override
            public RpslObject call() {
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
