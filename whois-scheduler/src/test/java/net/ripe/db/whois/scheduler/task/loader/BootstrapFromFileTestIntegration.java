package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.database.diff.Database;
import net.ripe.db.whois.common.support.database.diff.DatabaseDiff;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Category(IntegrationTest.class)
public class BootstrapFromFileTestIntegration extends AbstractSchedulerIntegrationTest {
    @Autowired
    private Bootstrap bootstrap;

    @Autowired
    private RpslObjectUpdateDao rpslObjectUpdateDao;

    @Test
    public void testThatBootstrapLeavesDatabaseInWorkingState() throws Exception {
        assertThat(whoisTemplate.queryForInt("select count(*) from x509"), is(1));
        assertThat(whoisTemplate.queryForInt("select count(*) from update_lock"), is(1));
        assertThat(whoisTemplate.queryForInt("select count(*) from mntner"), is(1));

        bootstrap.bootstrap();

        rpslObjectUpdateDao.createObject(RpslObject.parse("mntner: NINJA-MNT"));
    }

    @Test
    public void testSplitFileLoad() throws Exception {
        final Database before = new Database(whoisTemplate);

        bootstrap.setDumpFileLocation(applicationContext.getResource("TEST.db").getURI().getPath());
        final String result = bootstrap.bootstrap();

        assertThat(result, containsString("FINISHED\n220 succeeded, 0 failed\n"));
        assertThat(result.toLowerCase(), not(containsString("error")));

        final DatabaseDiff diff = Database.diff(before, new Database(whoisTemplate));

        final Database added = diff.getAdded();
        assertThat(added.getTable("serials"), hasSize(440));
        assertThat(added.getTable("last"), hasSize(220));
        assertThat(added.getTable("history"), hasSize(220));
        assertThat(added.getTable("organisation_id"), hasSize(4));
        assertThat(added.getTable("nic_hdl").size(), greaterThan(5));

        final Database removed = diff.getRemoved();
        assertThat(removed.getAll(), hasSize(0));
    }
}
