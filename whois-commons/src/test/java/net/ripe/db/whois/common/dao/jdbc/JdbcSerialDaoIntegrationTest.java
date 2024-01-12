package net.ripe.db.whois.common.dao.jdbc;


import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class JdbcSerialDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Autowired JdbcSerialDao subject;
    @Value("${whois.source}") protected String source;

    @BeforeEach
    public void setup() {
        sourceContext.setCurrent(Source.slave(source));
    }

    @AfterEach
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    // getSerials()

    @Test
    public void getSerials() {
        databaseHelper.addObject("aut-num:AS4294967207");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("mntner:DEV-MNT");

        final SerialRange range = subject.getSerials();

        assertThat(range.getBegin(), is(1));
        assertThat(range.getEnd(), is(3));
    }

    // getById()

    @Test
    public void getById_create_inetnum() {
        final RpslObject inetnum = databaseHelper.addObject(RpslObject.parse("mntner:DEV-MNT"));

        assertThat(subject.getById(1).getRpslObject(), is(inetnum));
    }

    @Test
    public void getById_create_autnum() {
        final RpslObject autnum = databaseHelper.addObject("aut-num: AS1\ndescr: first");

        assertThat(subject.getById(1).getRpslObject(), is(autnum));
    }

    @Test
    public void getById_create_delete_create() {
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
    public void getById_delete() {
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
    public void getById_create_update_update() {
        databaseHelper.addObject("aut-num: AS1\ndescr: first");
        databaseHelper.updateObject("aut-num: AS1\ndescr: second");
        final RpslObject object3 = databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        assertThat(subject.getById(1).getRpslObject(), is(object3));
        assertThat(subject.getById(2).getRpslObject(), is(object3));
        assertThat(subject.getById(3).getRpslObject(), is(object3));
    }

    // getByIdForNrtm()

    @Test
    public void getByIdForNrtm_just_created_object() {
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
    public void getByIdForNrtm_object_still_in_last() {
        final RpslObject object1 = databaseHelper.addObject("aut-num: AS1\ndescr: first");
        final RpslObject object2 = databaseHelper.updateObject("aut-num: AS1\ndescr: second");
        final RpslObject object3 = databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        assertThat(subject.getByIdForNrtm(1).getRpslObject(), is(object1));
        assertThat(subject.getByIdForNrtm(2).getRpslObject(), is(object2));
        assertThat(subject.getByIdForNrtm(3).getRpslObject(), is(object3));
    }

    @Test
    public void getAgeOfExactOrNextExistingSerial_create_and_multiple_updates() {
        final LocalDateTime createTimestamp = LocalDateTime.parse("2001-02-04T17:00:00");

        testDateTimeProvider.setTime(createTimestamp);

        databaseHelper.addObject("aut-num: AS1\ndescr: first");

        testDateTimeProvider.setTime(createTimestamp.plusDays(1).plusMinutes(10));
        databaseHelper.updateObject("aut-num: AS1\ndescr: second");

        testDateTimeProvider.setTime(createTimestamp.plusDays(2).plusMinutes(20));
        databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        testDateTimeProvider.setTime(createTimestamp.plusDays(3).plusMinutes(30));
        databaseHelper.addObject("aut-num: AS2\ndescr: first");

        testDateTimeProvider.setTime(createTimestamp.plusDays(4).plusMinutes(40));
        databaseHelper.deleteObject(RpslObject.parse("aut-num: AS2\ndescr: first"));

        final LocalDateTime queryTimestamp = createTimestamp.plusDays(5).plusMinutes(50);
        testDateTimeProvider.setTime(queryTimestamp);

        assertThat(subject.getAgeOfExactOrNextExistingSerial(1), is(duration(createTimestamp, queryTimestamp)));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(2), is(duration(createTimestamp.plusDays(1).plusMinutes(10), queryTimestamp)));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(3), is(duration(createTimestamp.plusDays(2).plusMinutes(20), queryTimestamp)));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(4), is(duration(createTimestamp.plusDays(3).plusMinutes(30), queryTimestamp)));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(5), is(duration(createTimestamp.plusDays(4).plusMinutes(40), queryTimestamp)));
    }

    @Test
    public void getAgeOfExactOrNextExistingSerial_gap_in_serial() {
        final LocalDateTime createTimestamp = LocalDateTime.parse("2001-02-04T17:00:00");

        testDateTimeProvider.setTime(createTimestamp);
        databaseHelper.addObject("aut-num: AS1\ndescr: first");

        testDateTimeProvider.setTime(createTimestamp.plusDays(1).plusMinutes(10));
        databaseHelper.updateObject("aut-num: AS1\ndescr: second");

        testDateTimeProvider.setTime(createTimestamp.plusDays(2).plusMinutes(20));
        databaseHelper.updateObject("aut-num: AS1\ndescr: third");

        final LocalDateTime queryTimestamp = createTimestamp.plusDays(3).plusMinutes(30);
        testDateTimeProvider.setTime(queryTimestamp);

        // create a gap in the serials table
        databaseHelper.getWhoisTemplate().update("delete from serials where serial_id = ?", 2);

        assertThat(subject.getAgeOfExactOrNextExistingSerial(1), is(duration(createTimestamp, queryTimestamp)));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(2), is(duration(createTimestamp.plusDays(2).plusMinutes(20), queryTimestamp)));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(3), is(duration(createTimestamp.plusDays(2).plusMinutes(20), queryTimestamp)));
        assertThat(subject.getAgeOfExactOrNextExistingSerial(4), is(nullValue()));
    }

    @Test
    public void getAgeOfExactOrNextExistingSerial_non_existent_serial() {
        assertThat(subject.getAgeOfExactOrNextExistingSerial(12345), is(nullValue()));
    }

    // helper methods

    // get duration in seconds between two timestamps
    private Integer duration(final Temporal start, final Temporal end) {
        return Long.valueOf(Duration.between(start, end).get(ChronoUnit.SECONDS)).intValue();
    }

}
