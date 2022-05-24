package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JdbcRpslObjectOperationsTest {

    @Mock
    private JdbcTemplate whoisTemplate;

    @Test
    public void sanityCheckKickingIn() {
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
    public void sanityCheckLettingThrough() {
        for (String dbName : ImmutableList.of("WHOIS_TEST_TEST", "GRSteST", "WHOIS_MIRROR_APNIC_GRS")) {
            when(whoisTemplate.queryForObject(eq("SELECT database()"), eq(String.class))).thenReturn(dbName);
            JdbcRpslObjectOperations.sanityCheck(whoisTemplate);
        }
    }

}
