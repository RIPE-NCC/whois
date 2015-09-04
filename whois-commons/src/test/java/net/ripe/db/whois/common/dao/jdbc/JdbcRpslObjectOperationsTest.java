package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcRpslObjectOperationsTest {
    JdbcTemplate whoisTemplate = mock(JdbcTemplate.class);


    @Test
    public void testSanityCheckKickingIn() {
        for (String dbName : ImmutableList.of("WHOIS_UPDATE_RIPE", "MAILUPDATES")) {
            try {
                when(whoisTemplate.queryForObject("SELECT database()", String.class)).thenReturn(dbName);
                JdbcRpslObjectOperations.sanityCheck(whoisTemplate);
                fail("Database name '" + dbName + "' did not trigger exception");
            } catch (Exception e) {
                assertThat(e.getMessage(), endsWith("has no 'test' or 'grs' in the name, exiting"));
            }
        }
    }

    @Test
    public void testSanityCheckLettingThrough() {
        when(whoisTemplate.queryForObject(startsWith("SELECT count(*) FROM "), eq(Integer.class))).thenReturn(10);
        for (String dbName : ImmutableList.of("WHOIS_TEST_TEST", "test_1356291725259_DNSCHECK", "GRSteST", "WHOIS_MIRROR_APNIC_GRS")) {
            when(whoisTemplate.queryForObject(eq("SELECT database()"), eq(String.class))).thenReturn(dbName);
            JdbcRpslObjectOperations.sanityCheck(whoisTemplate);
        }
    }
}
