package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@Ignore
public class RndVersionDaoTest extends AbstractInternalTest {
    @Autowired
    @Qualifier("whoisReadOnlySlaveDataSource")
    DataSource dataSource;

    @Autowired
    VersionDao subject;

    @Before
    public void setUp() {
        testDateTimeProvider.reset();
        databaseHelper.setupWhoisDatabase(new JdbcTemplate(dataSource));
    }

    @Test
    public void get_versions_for_existing_object() {
        final LocalDateTime localDateTime = new LocalDateTime();

        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(RpslObject.parse("domain:test.sk\ndescr:description1\nsource:RIPE\n"));
        updateDao.updateObject(objectInfo.getObjectId(), RpslObject.parse("domain:test.sk\ndescr:description2\nsource:RIPE\n"));
        updateDao.updateObject(objectInfo.getObjectId(), RpslObject.parse("domain:test.sk\ndescr:description3\nsource:RIPE\n"));

        final List<VersionInfo> versions = subject.getVersionsForTimestamp(ObjectType.DOMAIN, "test.sk", localDateTime.plusDays(1).toDateTime().getMillis());
        assertThat(versions, hasSize(3));
    }

    @Test
    public void get_versions_for_deleted_object() {
        final LocalDateTime localDateTime = new LocalDateTime();

        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(RpslObject.parse("domain:test.sk\ndescr:description1\nsource:RIPE\n"));
        updateDao.updateObject(objectInfo.getObjectId(), RpslObject.parse("domain:test.sk\ndescr:description2\nsource:RIPE\n"));
        updateDao.deleteObject(objectInfo.getObjectId(), objectInfo.getKey());

        final List<VersionInfo> versions = subject.getVersionsForTimestamp(ObjectType.DOMAIN, "test.sk", localDateTime.plusDays(1).toDateTime().getMillis());

        assertThat(versions, hasSize(3));
    }

    @Test
    public void get_versions_before_deleted_object() {
        final LocalDateTime localDateTime = new LocalDateTime();

        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(RpslObject.parse("domain:test.sk\ndescr:description1\nsource:RIPE\n"));
        updateDao.updateObject(objectInfo.getObjectId(), RpslObject.parse("domain:test.sk\ndescr:description2\nsource:RIPE\n"));

        testDateTimeProvider.setTime(localDateTime.plusDays(2));
        updateDao.deleteObject(objectInfo.getObjectId(), objectInfo.getKey());

        final List<VersionInfo> versions = subject.getVersionsForTimestamp(ObjectType.DOMAIN, "test.sk", localDateTime.plusDays(1).toDateTime().getMillis());

        assertThat(versions, hasSize(2));
    }

    @Test
    public void get_versions_recreated_object() {
        final LocalDateTime localDateTime = new LocalDateTime();

        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(RpslObject.parse("domain:test.sk\ndescr:description1\nsource:RIPE\n"));
        updateDao.updateObject(objectInfo.getObjectId(), RpslObject.parse("domain:test.sk\ndescr:description2\nsource:RIPE\n"));

        testDateTimeProvider.setTime(localDateTime.plusDays(2));
        updateDao.deleteObject(objectInfo.getObjectId(), objectInfo.getKey());

        testDateTimeProvider.setTime(localDateTime.plusDays(3));
        updateDao.createObject(RpslObject.parse("domain:test.sk\ndescr:description1\nsource:RIPE\n"));

        final List<VersionInfo> versions = subject.getVersionsForTimestamp(ObjectType.DOMAIN, "test.sk", localDateTime.plusDays(4).toDateTime().getMillis());
        assertThat(versions, hasSize(4));
    }
}