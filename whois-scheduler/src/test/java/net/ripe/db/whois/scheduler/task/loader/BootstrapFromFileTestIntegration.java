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

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@Category(IntegrationTest.class)
public class BootstrapFromFileTestIntegration extends AbstractSchedulerIntegrationTest {
    @Autowired
    private Bootstrap bootstrap;

    @Autowired
    private RpslObjectUpdateDao rpslObjectUpdateDao;

    @Test
    public void testThatBootstrapLeavesDatabaseInWorkingState() throws Exception {
        assertThat(whoisTemplate.queryForObject("select count(*) from x509", Integer.class).intValue(), is(1));
        assertThat(whoisTemplate.queryForObject("select count(*) from update_lock", Integer.class).intValue(), is(1));

        bootstrap.bootstrap();

        rpslObjectUpdateDao.createObject(RpslObject.parse("mntner: NINJA-MNT"));
    }

    @Test
    public void testSplitFileLoad() throws Exception {
        final Database before = new Database(whoisTemplate);

        bootstrap.setDumpFileLocation(applicationContext.getResource("TEST.db").getURI().getPath());
        final String result = bootstrap.bootstrap();

        assertThat(result, containsString("FINISHED\n220 succeeded\n0 failed in pass 1\n0 failed in pass 2\n"));

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

    @Test
    public void split_file_with_errors_added_safe() throws IOException {
        split_file_with_errors_added(LoaderMode.SAFE);
    }

    @Test
    public void split_file_with_errors_added_risky() throws IOException {
        split_file_with_errors_added(LoaderMode.FAST_AND_RISKY);
    }

    public void split_file_with_errors_added(final LoaderMode loaderMode) throws IOException {

        bootstrap.setDumpFileLocation(applicationContext.getResource("TEST_BOOTSTRAP_LOAD_DUMP.db").getURI().getPath());

        final String bootstrapLoadResults = bootstrap.bootstrap();

        assertThat(bootstrapLoadResults, containsString("FINISHED\n3 succeeded\n0 failed in pass 1\n0 failed in pass 2\n"));

        final Database bootstrapLoad = new Database(whoisTemplate);

        final String additionalLoadResults;
        final String[] dumpFiles = {applicationContext.getResource("TEST_ADDITIONAL_LOAD_DUMP_WITH_ERROR.db").getURI().getPath()};

        if (loaderMode == LoaderMode.FAST_AND_RISKY){
            additionalLoadResults = bootstrap.loadTextDumpRisky(dumpFiles);
        } else {
            additionalLoadResults = bootstrap.loadTextDumpSafe(dumpFiles);
        }

        assertThat(additionalLoadResults, containsString(
                "FINISHED\n2 succeeded\n1 failed in pass 1\n1 failed in pass 2\n"));

        assertThat(additionalLoadResults, containsString("Error in pass 1 in '[person] AA2-TEST   " +
                "Incorrect Person': net.ripe.db.whois.update.autokey.ClaimException"));

        assertThat(additionalLoadResults, containsString("EmptyResultDataAccessException: Incorrect result size"));

        final Database additionalLoad = new Database(whoisTemplate);

        assertThat(additionalLoad.getTable("serials"), hasSize(10));
        assertThat(additionalLoad.getTable("last"), hasSize(5));
        assertThat(additionalLoad.getTable("history"), hasSize(5));
        assertThat(additionalLoad.getTable("mntner"), hasSize(2));
        assertThat(additionalLoad.getTable("nic_hdl"), hasSize(2));
        assertThat(additionalLoad.getTable("person_role"), hasSize(3));

        final DatabaseDiff diff = Database.diff(bootstrapLoad, additionalLoad);

        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getAdded().getTable("last"), hasSize(2));
        assertThat(diff.getAdded().getTable("nic_hdl"), hasSize(1));
        assertThat(diff.getAdded().getTable("person_role"), hasSize(1));
    }

}
