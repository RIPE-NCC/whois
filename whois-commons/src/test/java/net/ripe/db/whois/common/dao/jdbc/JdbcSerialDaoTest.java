package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class JdbcSerialDaoTest extends AbstractDaoTest {

    private static final int SECONDS_PER_DAY = 60 * 60 * 24;

    @Autowired JdbcSerialDao subject;
    @Value("${whois.source}") protected String source;

    @Before
    public void setup() {
        sourceContext.setCurrent(Source.slave(source));
    }

    @After
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    @Test
    public void getSerials() {
        databaseHelper.addObject("aut-num:AS4294967207");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("mntner:DEV-MNT");

        final SerialRange range = subject.getSerials();

        assertThat(range.getBegin(), is(1));
        assertThat(range.getEnd(), is(3));
    }

    @Test
    public void getSerialEntryById() {
        final RpslObject inetnum = RpslObject.parse("mntner:DEV-MNT");
        databaseHelper.addObject(inetnum);

        final SerialEntry entry = subject.getById(1);

        assertThat(entry.getRpslObject(), is(inetnum));
    }

    @Test
    public void getSerialCreateDelete() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS1");
        final RpslObject object = databaseHelper.addObject(autnum);
        databaseHelper.deleteObject(object);
        databaseHelper.addObject(autnum);

        final SerialEntry first = subject.getById(1);
        assertThat(first.getOperation(), is(Operation.UPDATE));
        assertThat(first.getRpslObject(), is(autnum));

        final SerialEntry second = subject.getById(2);
        assertThat(second.getOperation(), is(Operation.DELETE));
        assertThat(second.getRpslObject(), is(autnum));

        final SerialEntry third = subject.getById(3);
        assertThat(third.getOperation(), is(Operation.UPDATE));
        assertThat(third.getRpslObject(), is(autnum));
    }

    @Test
    public void getSerialEntry_just_created_object() {
        final RpslObject object1 = databaseHelper.addObject("aut-num: AS1\ndescr: first");

        assertThat(subject.getById(1).getRpslObject(), is(object1));
    }

    @Test
    public void getSerialEntry_deleted_object() {
        final RpslObject object1 = databaseHelper.addObject("aut-num: AS1\ndescr: first");
        final RpslObject object2 = databaseHelper.updateObject("aut-num: AS1\ndescr: second");
        databaseHelper.deleteObject(object2);

        assertThat(subject.getById(1).getRpslObject(), is(object1));
        assertThat(subject.getById(2).getRpslObject(), is(object2));

        final SerialEntry serialEntry = subject.getByIdForNrtm(3);
        assertThat(serialEntry.getOperation(), is(Operation.DELETE));
        assertThat(serialEntry.getRpslObject(), is(object2)); //it should return the version prior to deletion
    }

    @Test
    public void getSerialEntry_object_still_in_last() {
        databaseHelper.addObject("aut-num: AS1\ndescr: first");
        databaseHelper.updateObject("aut-num: AS1\ndescr: second");
        final RpslObject object3 = databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        assertThat(subject.getById(1).getRpslObject(), is(object3));
        assertThat(subject.getById(2).getRpslObject(), is(object3));
        assertThat(subject.getById(3).getRpslObject(), is(object3));
    }

    @Test
    public void getSerialEntryForNrtm_just_created_object() {
        final RpslObject object = databaseHelper.addObject("aut-num: AS1\ndescr: first");

        assertThat(subject.getByIdForNrtm(1).getRpslObject(), is(object));
    }

    @Test
    public void getSerialEntryForNrtm_deleted_object() {
        final RpslObject object1 = databaseHelper.addObject("aut-num: AS1\ndescr: first");
        final RpslObject object2 = databaseHelper.updateObject("aut-num: AS1\ndescr: second");
        databaseHelper.deleteObject(object2);

        assertThat(subject.getByIdForNrtm(1).getRpslObject(), is(object1));
        assertThat(subject.getByIdForNrtm(2).getRpslObject(), is(object2));

        final SerialEntry serialEntry = subject.getByIdForNrtm(3);
        assertThat(serialEntry.getOperation(), is(Operation.DELETE));
        assertThat(serialEntry.getRpslObject(), is(object2)); //it should return the version prior to deletion
    }

    @Test
    public void getSerialEntryForNrtm_object_still_in_last() {
        final RpslObject object1 = databaseHelper.addObject("aut-num: AS1\ndescr: first");
        final RpslObject object2 = databaseHelper.updateObject("aut-num: AS1\ndescr: second");
        final RpslObject object3 = databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        assertThat(subject.getByIdForNrtm(1).getRpslObject(), is(object1));
        assertThat(subject.getByIdForNrtm(2).getRpslObject(), is(object2));
        assertThat(subject.getByIdForNrtm(3).getRpslObject(), is(object3));
    }

    @Test
    public void getAgeOfExactOrNextExistingSerial_normal_scenario() {
        //10 mins error range to give build machine enough time to run
        final LocalDateTime event1 = LocalDateTime.now();
        final LocalDateTime event2 = event1.plusDays(1).plusMinutes(10);
        final LocalDateTime event3 = event1.plusDays(2).plusMinutes(20);
        final LocalDateTime event4 = event1.plusDays(3).plusMinutes(30);
        final LocalDateTime event5 = event1.plusDays(4).plusMinutes(40);
        final LocalDateTime queryTime = event1.plusDays(5).plusMinutes(50);

        testDateTimeProvider.setTime(event1);
        databaseHelper.addObject("aut-num: AS1\ndescr: first");

        testDateTimeProvider.setTime(event2);
        databaseHelper.updateObject("aut-num: AS1\ndescr: second");

        testDateTimeProvider.setTime(event3);
        databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        testDateTimeProvider.setTime(event4);
        databaseHelper.addObject("aut-num: AS2\ndescr: first");

        testDateTimeProvider.setTime(event5);
        databaseHelper.deleteObject(RpslObject.parse("aut-num: AS2\ndescr: first"));

        testDateTimeProvider.setTime(queryTime);



        final int expectedAge1 = Long.valueOf(java.time.Duration.between(event1, queryTime).toDays()).intValue();
        final int expectedAge2 = Long.valueOf(java.time.Duration.between(event2, queryTime).toDays()).intValue();
        final int expectedAge3 = Long.valueOf(java.time.Duration.between(event3, queryTime).toDays()).intValue();
        final int expectedAge4 = Long.valueOf(java.time.Duration.between(event4, queryTime).toDays()).intValue();
        final int expectedAge5 = Long.valueOf(java.time.Duration.between(event5, queryTime).toDays()).intValue();

        assertThat(subject.getAgeOfExactOrNextExistingSerial(1) / SECONDS_PER_DAY, is(expectedAge1));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(2) / SECONDS_PER_DAY, is(expectedAge2));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(3) / SECONDS_PER_DAY, is(expectedAge3));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(4) / SECONDS_PER_DAY, is(expectedAge4));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(5) / SECONDS_PER_DAY, is(expectedAge5));
    }

    @Test
    public void getAgeOfExactOrNextExistingSerial_gap_in_serial() {
        //10 mins error range to give build machine enough time to run
        final LocalDateTime event1 = LocalDateTime.now();
        final LocalDateTime event2 = event1.plusDays(1).plusMinutes(10);
        final LocalDateTime event3 = event1.plusDays(2).plusMinutes(20);
        final LocalDateTime queryTime = event1.plusDays(3).plusMinutes(30);

        testDateTimeProvider.setTime(event1);
        databaseHelper.addObject("aut-num: AS1\ndescr: first");

        testDateTimeProvider.setTime(event2);
        databaseHelper.updateObject("aut-num: AS1\ndescr: second");

        testDateTimeProvider.setTime(event3);
        databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        testDateTimeProvider.setTime(queryTime);

        databaseHelper.getWhoisTemplate().update("delete from serials where serial_id = ?", 2);


        final int expectedAge1 = Long.valueOf(java.time.Duration.between(event1, queryTime).toDays()).intValue();
        final int expectedAge3 = Long.valueOf(java.time.Duration.between(event3, queryTime).toDays()).intValue();

        assertThat(subject.getAgeOfExactOrNextExistingSerial(1)/ SECONDS_PER_DAY, is(expectedAge1));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(2)/ SECONDS_PER_DAY, is(expectedAge3));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(3)/ SECONDS_PER_DAY, is(expectedAge3));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(10), is(nullValue()));
    }

    @Test
    public void getAgeOfExactOrNextExistingSerial_non_existent_serial() {
        databaseHelper.addObject("aut-num: AS1\ndescr: first");

        assertThat(subject.getAgeOfExactOrNextExistingSerial(10), is(nullValue()));
    }
}
