package net.ripe.db.whois.internal.api.rnd.dao;

import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class RndVersionDaoTest extends AbstractInternalTest {

    @Autowired
    RndVersionDao subject;

    @Test
    public void get_versions_for_existing_object() {
        testDateTimeProvider.reset();

        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(RpslObject.parse("domain:test.sk\ndescr:description1\nsource:RIPE\n"));
        updateDao.updateObject(objectInfo.getObjectId(), RpslObject.parse("domain:test.sk\ndescr:description2\nsource:RIPE\n"));
        updateDao.updateObject(objectInfo.getObjectId(), RpslObject.parse("domain:test.sk\ndescr:description3\nsource:RIPE\n"));

        final VersionLookupResult legacyVersions = jdbcVersionDao.findByKey(ObjectType.DOMAIN, "test.sk");
        long millies = Iterables.getLast(legacyVersions.getAllVersions()).getTimestamp().getTimestamp().toDateTime().getMillis();

        final List<VersionInfo> versions = subject.getVersionsForTimestamp(ObjectType.DOMAIN, "test.sk", millies);
        assertThat(versions.size(), greaterThanOrEqualTo(1));
        assertThat(versions.size(), lessThanOrEqualTo(3));
    }
}