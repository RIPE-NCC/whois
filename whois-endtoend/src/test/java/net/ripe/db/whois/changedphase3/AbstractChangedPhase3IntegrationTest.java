package net.ripe.db.whois.changedphase3;

import net.ripe.db.whois.WhoisEndToEndTestConfiguration;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.changedphase3.util.Context;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.scheduler.task.export.DatabaseTextExport;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Tag("IntegrationTest")
@ContextConfiguration(classes = {WhoisEndToEndTestConfiguration.class})
public abstract class AbstractChangedPhase3IntegrationTest extends AbstractIntegrationTest {

    protected static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");
    protected static final RpslObject OWNER_MNTNER = RpslObject.parse("" +
            "mntner:        OWNER-MNT\n" +
            "descr:         Test maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        upd-to@ripe.net\n" +
            "mnt-nfy:       mnt-nfy@ripe.net\n" +
            "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    protected Context context;
    @Autowired private MaintenanceMode maintenanceMode;
    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired private MailSenderStub mailSenderStub;
    @Autowired private NrtmServer nrtmServer;
    @Autowired private QueryServer queryServer;

    @Autowired private DatabaseTextExport databaseTextExport;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("nrtm.enabled", "true");
    }

    @AfterAll
    public static void afterClass() {
        System.clearProperty("nrtm.enabled");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNTNER);
        databaseHelper.updateObject(TEST_PERSON);
        maintenanceMode.set("FULL,FULL");
        context = new Context(getPort(), getPort(), whoisObjectMapper, mailUpdatesTestSupport, mailSenderStub,
               queryServer, nrtmServer, databaseHelper, databaseTextExport);
    }

    @AfterEach
    public void teardown() {
    }
}
