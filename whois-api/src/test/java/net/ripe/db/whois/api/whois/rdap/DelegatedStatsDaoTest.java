package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.domain.CIString;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class DelegatedStatsDaoTest extends AbstractIntegrationTest {
    @Autowired DelegatedStatsDao subject;

    @Before
    public void setup() {
        databaseHelper.getDelegatedStatsTemplate().update("" +
                "DELETE FROM delegated_stats");
    }

    @Test
    public void findResource_autnum() {
        databaseHelper.getDelegatedStatsTemplate().update("" +
                "INSERT INTO delegated_stats(source, country, type, resource_start, resource_end, prefix_length, value, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "afrinic", "ZB", "asn", "6351", "6351", 1, "value", "allocated");

        final CIString result = subject.findSourceForResource("AS6351");

        assertThat(result, is(CIString.ciString("afrinic")));
    }

    @Test
    public void findResource_autnum_outside_range() throws SQLException {
        databaseHelper.getDelegatedStatsTemplate().update("" +
                "INSERT INTO delegated_stats(source, country, type, resource_start, resource_end, prefix_length, value, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "afrinic", "ZB", "asn", "6351", "6354", 4, "value", "allocated");

        final CIString result = subject.findSourceForResource("AS6355");

        assertThat(result, is(nullValue()));
    }

    @Test
    public void findResource_inetnum() {
        databaseHelper.getDelegatedStatsTemplate().update("" +
                "INSERT INTO delegated_stats(source, country, type, resource_start, resource_end, prefix_length, value, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "apnic", "NZ", "ipv4", "3238002688", "3254779903", 8, "value", "allocated"); //193.0/8

        final CIString result = subject.findSourceForResource("193.0.0.0/24");

        assertThat(result, is(CIString.ciString("apnic")));
    }

    @Test
    public void findResource_inetnum_out_of_range() throws SQLException {
        databaseHelper.getDelegatedStatsTemplate().update("" +
                "INSERT INTO delegated_stats(source, country, type, resource_start, resource_end, prefix_length, value, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "apnic", "NZ", "ipv4", "3238002688", "3238002943", 24, "value", "allocated"); //193.0/24

        final CIString result = subject.findSourceForResource("193.0.0.0/8");

        assertThat(result, is(nullValue()));
    }
}
