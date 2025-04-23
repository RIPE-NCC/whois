package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.WhoisObjectDao;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.domain.WhoisObjectData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static java.util.stream.Collectors.groupingBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WhoisObjectRepositoryTestIntegration extends AbstractNrtmIntegrationTest {

    @Autowired
    WhoisObjectRepository whoisObjectRepository;

    @Autowired
    WhoisObjectDao whoisObjectDao;

    final RpslObject ORG_TEST2 = RpslObject.parse("" +
            "organisation:  ORG-TEST2-TEST\n" +
            "org-name:      Test2 organisation\n" +
            "org-type:      OTHER\n" +
            "descr:         Drugs and gambling\n" +
            "remarks:       Nice to deal with generally\n" +
            "address:       1 Fake St. Fauxville\n" +
            "phone:         +01-000-000-000\n" +
            "fax-no:        +01-000-000-000\n" +
            "admin-c:       PP1-TEST\n" +
            "e-mail:        org@test.com\n" +
            "mnt-by:        OWNER-MNT\n" +
            "created:         2022-08-14T11:48:28Z\n" +
            "last-modified:   2022-10-25T12:22:39Z\n" +
            "source:        TEST");

    @Test
    public void should_not_include_new_objects_snapshot_state_since_last_delta() {
        final int lastSerialId = whoisObjectDao.getLastSerialId();
        final RpslObject newObject = databaseHelper.addObject(ORG_TEST2);

        final SnapshotState snapshotState = whoisObjectRepository.getSnapshotState(lastSerialId);
        assertThat(snapshotState.whoisObjectData(), not(hasItem(new WhoisObjectData(newObject.getObjectId(), 1))));
    }

    @Test
    public void should_include_deleted_object_after_last_delta() {
        final RpslObject newObject = databaseHelper.addObject(ORG_TEST2);
        final int lastSerialId = whoisObjectDao.getLastSerialId();
        databaseHelper.deleteObject(newObject);

        final SnapshotState snapshotState = whoisObjectRepository.getSnapshotState(lastSerialId);
        assertThat(snapshotState.whoisObjectData(), hasItem(new WhoisObjectData(newObject.getObjectId(), 1)));
    }

    @Test
    public void check_snapshot_state_object_deleted_created_same_time_after_last_delta() {
        final RpslObject newObject = databaseHelper.addObject(ORG_TEST2);
        final int lastSerialId = whoisObjectDao.getLastSerialId();
        databaseHelper.deleteObject(newObject);
        final RpslObject recreatedObject = databaseHelper.addObject(ORG_TEST2);

        final SnapshotState snapshotState = whoisObjectRepository.getSnapshotState(lastSerialId);
        assertThat(snapshotState.whoisObjectData(), hasItem(new WhoisObjectData(newObject.getObjectId(), 1)));
        assertThat(snapshotState.whoisObjectData(), not(hasItem(new WhoisObjectData(recreatedObject.getObjectId(), 1))));
    }


    @Test
    public void check_snapshot_state_object_updated_after_last_delta() {
        final RpslObject newObject = databaseHelper.addObject(ORG_TEST2);
        final int lastSerialId = whoisObjectDao.getLastSerialId();
        databaseHelper.updateObject(RpslObject.parse("" +
                "organisation:  ORG-TEST2-TEST\n" +
                "org-name:      Test2 organisation\n" +
                "org-type:      OTHER\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "admin-c:       PP1-TEST\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST"));


        final SnapshotState snapshotState = whoisObjectRepository.getSnapshotState(lastSerialId);
        assertThat(snapshotState.whoisObjectData(), hasItem(new WhoisObjectData(newObject.getObjectId(), 1)));
        assertThat(snapshotState.whoisObjectData(), hasItem(not(new WhoisObjectData(newObject.getObjectId(), 2))));
    }
}
