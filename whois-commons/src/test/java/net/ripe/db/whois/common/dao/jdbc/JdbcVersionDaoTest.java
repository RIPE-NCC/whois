package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.VersionDateTime;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.assertNull;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class JdbcVersionDaoTest extends AbstractDaoTest {
    @Autowired VersionDao subject;

    @Before
    public void before() {
        loadScripts(databaseHelper.getWhoisTemplate(), "broken.sql");
    }

    @Test
    public void findNoObject() {
        VersionLookupResult history = subject.findByKey(ObjectType.MNTNER, "MAINT-ANY");
        assertNull(history);
    }

    @Test
    public void noHistoryForObject() {
        VersionLookupResult result = subject.findByKey(ObjectType.AS_SET, "AS-TEST");
        final List<VersionInfo> history = result.getVersionInfos();

        assertThat(result.getObjectType(), is(ObjectType.AS_SET));
        assertThat(result.getPkey(), is("AS-TEST"));
        assertNull(result.getLastDeletionTimestamp());

        assertThat(history, hasSize(1));
        assertThat(history.get(0).isInLast(), is(true));
        assertThat(history.get(0).getOperation(), is(Operation.UPDATE));
        assertThat(history.get(0).getObjectId(), is(824));
        assertThat(history.get(0).getSequenceId(), is(3));
        assertThat(history.get(0).getTimestamp(), is(new VersionDateTime(1032338056L)));

        final RpslObject rpslObject = subject.getRpslObject(history.get(0));

        assertThat(rpslObject, is(RpslObject.parse("" +
                "as-set: AS-TEST\n" +
                "descr:  Description\n" +
                "source: RIPE\n")));
    }

    @Test
    public void historyForDeletedObject() {
        VersionLookupResult result = subject.findByKey(ObjectType.DOMAIN, "test.sk");
        final List<VersionInfo> history = result.getVersionInfos();

        assertNotNull(history);
        assertThat(history, hasSize(0));
        assertNotNull(result.getLastDeletionTimestamp());

        databaseHelper.addObject("domain:test.sk\ndescr:description1\nsource:RIPE\n");
        databaseHelper.updateObject("domain:test.sk\ndescr:description2\nsource:RIPE\n");
        databaseHelper.updateObject("domain:test.sk\ndescr:description3\nsource:RIPE\n");

        VersionLookupResult rerun = subject.findByKey(ObjectType.DOMAIN, "test.sk");
        final List<VersionInfo> recreated = rerun.getVersionInfos();
        assertThat(recreated.size(), is(3));
        for (int i = 0; i < recreated.size(); i++) {
            assertThat(recreated.get(i).getOperation(), is(Operation.UPDATE));
        }
    }

    @Test
    public void longHistory() {
        VersionLookupResult result = subject.findByKey(ObjectType.AUT_NUM, "AS20507");
        final List<VersionInfo> history = result.getVersionInfos();

        assertNotNull(history);
        assertThat(history, hasSize(4));

        isMatching(history.get(0), new VersionInfo(false, 4709, 81, 1032341936L, Operation.UPDATE));
        isMatching(history.get(2), new VersionInfo(false, 4709, 83, 1034602217L, Operation.UPDATE));
        isMatching(history.get(3), new VersionInfo(false, 4709, 84, 1034685022L, Operation.UPDATE));
    }

    public void isMatching(VersionInfo got, VersionInfo expected) {
        isMatching(null, got, expected);
    }

    public void isMatching(String message, VersionInfo got, VersionInfo expected) {
        assertThat(message, got.toString(), is(expected.toString()));
    }
}
