package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcRpslObjectOperationsTest {
    JdbcTemplate whoisTemplate = mock(JdbcTemplate.class);

    @Before
    public void setup() {
    }

    @Test
    public void testSanityCheckKickingIn() {
        for (String dbName : ImmutableList.of("WHOIS_UPDATE_RIPE", "MAILUPDATES")) {
            try {
                when(whoisTemplate.<Object>queryForObject(eq("SELECT database()"), any(Class.class))).thenReturn(dbName);
                JdbcRpslObjectOperations.sanityCheck(whoisTemplate);
                fail("Database name '" + dbName + "' did not trigger exception");
            } catch (Exception e) {
                assertThat(e.getMessage(), endsWith("has no 'test' or 'grs' in the name, exiting"));
            }
        }
    }

    @Test
    public void testSanityCheckLettingThrough() {
        when(whoisTemplate.queryForInt(startsWith("SELECT count(*) FROM "), any(Class.class))).thenReturn(10);
        for (String dbName : ImmutableList.of("WHOIS_TEST_TEST", "test_1356291725259_DNSCHECK", "GRSteST", "WHOIS_MIRROR_APNIC_GRS")) {
            when(whoisTemplate.<Object>queryForObject(eq("SELECT database()"), any(Class.class))).thenReturn(dbName);
            JdbcRpslObjectOperations.sanityCheck(whoisTemplate);
        }
    }
}
