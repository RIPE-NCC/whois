package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class GrsDaoTest extends AbstractSchedulerIntegrationTest {
    @Autowired SourceContext sourceContext;
    @Autowired DateTimeProvider dateTimeProvider;

    Logger logger = LoggerFactory.getLogger(GrsDao.class);
    GrsDao subject;

    @BeforeClass
    public static void setupGrsDatabase() {
        System.setProperty("grs.sources", "TEST-GRS");
    }

    @Before
    public void setUp() throws Exception {
        subject = new GrsDao(logger, dateTimeProvider, ciString("TEST-GRS"), sourceContext);
        subject.cleanDatabase();
    }

    @Test(expected = IllegalArgumentException.class)
    public void no_grs_datasource() {
        subject = new GrsDao(logger, dateTimeProvider, ciString("UNKNOWN"), sourceContext);
        subject.cleanDatabase();
    }

    @Test
    public void create_object_and_rebuild() {
        final RpslObject maintainer = RpslObject.parse("mntner: DEV-MNT");
        subject.createObject(maintainer);
        assertThat(subject.getCurrentObjectIds(), hasSize(1));

        subject.cleanDatabase();
        assertThat(subject.getCurrentObjectIds(), hasSize(0));

        subject.createObject(maintainer);
        assertThat(subject.getCurrentObjectIds(), hasSize(1));

        final GrsObjectInfo grsObjectInfo = subject.find("DEV-MNT", ObjectType.MNTNER);
        assertThat(grsObjectInfo.getType(), is(ObjectType.MNTNER));
        assertThat(grsObjectInfo.getKey(), is("DEV-MNT"));
        assertThat(grsObjectInfo.getRpslObject(), is(maintainer));
    }

    @Test
    public void find_not_existing_object() {
        assertNull(subject.find("DEV-MNT", ObjectType.MNTNER));
    }

    @Test
    public void create_update_and_delete_object() {
        subject.createObject(RpslObject.parse("mntner: DEV-MNT"));
        assertThat(subject.getCurrentObjectIds(), hasSize(1));

        final GrsObjectInfo grsObjectInfo = subject.find("DEV-MNT", ObjectType.MNTNER);
        subject.updateObject(grsObjectInfo, RpslObject.parse("mntner: DEV-MNT"));
        assertThat(subject.getCurrentObjectIds(), hasSize(1));

        subject.deleteObject(grsObjectInfo.getObjectId());
        assertThat(subject.getCurrentObjectIds(), hasSize(0));
    }

    @Test
    public void create_object_with_invalid_references() {
        final GrsDao.UpdateResult updateResult = subject.createObject(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: UNKNOWN-MNT"));

        assertThat(updateResult.hasMissingReferences(), is(true));
        assertThat(subject.getCurrentObjectIds(), hasSize(1));

        final Set<CIString> missingReferences = subject.updateIndexes(updateResult.getObjectId());
        assertThat(missingReferences, contains(ciString("UNKNOWN-MNT")));
    }

    @Test
    public void create_object_with_invalid_references_resolved() {
        final GrsDao.UpdateResult updateResult1 = subject.createObject(RpslObject.parse("" +
                "mntner: DEV1-MNT\n" +
                "mnt-by: DEV2-MNT"));

        assertThat(updateResult1.hasMissingReferences(), is(true));

        final GrsDao.UpdateResult updateResult2 = subject.createObject(RpslObject.parse("" +
                "mntner: DEV2-MNT\n" +
                "mnt-by: DEV1-MNT"));

        assertThat(updateResult2.hasMissingReferences(), is(false));

        assertThat(subject.getCurrentObjectIds(), hasSize(2));

        final Set<CIString> missingAfterUpdate = subject.updateIndexes(updateResult1.getObjectId());
        assertThat(missingAfterUpdate, hasSize(0));
    }

    @Test
    public void updateIndexes_unknown_object_does_not_throw_exception() {
        final Set<CIString> missingReferences = subject.updateIndexes(1);
        assertThat(missingReferences, hasSize(0));
    }
}
