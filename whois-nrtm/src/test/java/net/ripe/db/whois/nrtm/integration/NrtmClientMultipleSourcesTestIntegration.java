package net.ripe.db.whois.nrtm.integration;


import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.ObjectType;
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

import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class NrtmClientMultipleSourcesTestIntegration extends AbstractNrtmIntegrationBase {

    private static final RpslObject MNTNER = RpslObject.parse("" +
            "mntner: OWNER-MNT\n" +
            "source: TEST");

    @Autowired protected NrtmImporter nrtmImporter;
    @Autowired protected SourceContext sourceContext;

    @BeforeAll
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS", "2-GRS", "3-GRS");
        System.setProperty("nrtm.update.interval", "1");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS,2-GRS,3-GRS");
        System.setProperty("nrtm.import.enabled", "true");
    }

    @BeforeEach
    public void before() {
        databaseHelper.addObject(MNTNER);
        databaseHelper.addObjectToSource("1-GRS", MNTNER);
        databaseHelper.addObjectToSource("2-GRS", MNTNER);
        databaseHelper.addObjectToSource("3-GRS", MNTNER);
        nrtmServer.start();
        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "localhost");
        System.setProperty("nrtm.import.1-GRS.port", Integer.toString(nrtmServer.getPort()));
        System.setProperty("nrtm.import.2-GRS.source", "TEST");
        System.setProperty("nrtm.import.2-GRS.host", "localhost");
        System.setProperty("nrtm.import.2-GRS.port", Integer.toString(nrtmServer.getPort()));
        System.setProperty("nrtm.import.3-GRS.source", "TEST");
        System.setProperty("nrtm.import.3-GRS.host", "localhost");
        System.setProperty("nrtm.import.3-GRS.port", Integer.toString(nrtmServer.getPort()));
        nrtmImporter.start();
    }

    @AfterEach
    public void after() {
        nrtmImporter.stop(true);
        nrtmServer.stop(true);
    }

    @Test
    public void add_mntner_from_nrtm() {
        final RpslObject mntner = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");

        databaseHelper.addObject(mntner);

        objectExists(ObjectType.MNTNER, "TEST-MNT", "1-GRS", true);
        objectExists(ObjectType.MNTNER, "TEST-MNT", "2-GRS", true);
        objectExists(ObjectType.MNTNER, "TEST-MNT", "3-GRS", true);
    }

    private void objectExists(final ObjectType type, final String key, final String source, final boolean exists) {
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
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
