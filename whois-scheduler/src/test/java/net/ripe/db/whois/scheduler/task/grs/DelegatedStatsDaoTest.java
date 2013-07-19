package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class DelegatedStatsDaoTest extends AbstractSchedulerIntegrationTest {
    @Autowired DelegatedStatsDao subject;

    @Test
    public void findResource_autnum() {
        databaseHelper.getDelegatedStatsTemplate().update("" +
                "INSERT INTO delegated_stats(source, country, type, resource_start, resource_end, prefix_length, value, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "afrinic", "ZB", "asn", "6351", "6351", 1, "value", "allocated");

        final List<String> result = subject.findSourceForResource("AS6351");

        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("afrinic"));
    }

    @Test
    public void findResource_autnum_outside_range() throws SQLException {
        databaseHelper.getDelegatedStatsTemplate().update("" +
                "INSERT INTO delegated_stats(source, country, type, resource_start, resource_end, prefix_length, value, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "afrinic", "ZB", "asn", "6351", "6354", 4, "value", "allocated");

        final List<String> result = subject.findSourceForResource("AS6355");
        DatabaseHelper.dumpSchema(databaseHelper.getDelegatedStatsTemplate().getDataSource());

        assertThat(result, is(emptyIterable()));
    }
}
