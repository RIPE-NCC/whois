package net.ripe.db.whois.nrtm.integration;


import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.client.NrtmImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmClientInvalidHostTestIntegration extends AbstractNrtmIntegrationBase {

    @Autowired protected NrtmImporter nrtmImporter;
    private static final RpslObject mntner = RpslObject.parse("" +
            "mntner: TEST-MNT\n" +
            "source: TEST");

    @BeforeAll
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS");
        System.setProperty("nrtm.import.enabled", "true");
        System.setProperty("nrtm.import.1-GRS.source", "TEST");
        System.setProperty("nrtm.import.1-GRS.host", "invalid.localhost");
        System.setProperty("nrtm.import.1-GRS.port", "4444");
    }

    // provoke an UnresolvedAddressException in the NrtmClient with an invalid host property,
    // and ensure the client stops running
    @Test
    public void invalid_host() {
        try {
            databaseHelper.addObject(mntner);
            nrtmServer.start();

            nrtmImporter.start();
            objectExists(ObjectType.MNTNER, "TEST-MNT", false);
        } finally {
            nrtmImporter.stop(false);
            nrtmServer.stop(false);
        }
    }
}
