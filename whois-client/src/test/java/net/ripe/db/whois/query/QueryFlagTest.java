package net.ripe.db.whois.query;

import net.ripe.db.whois.query.QueryFlag;
import org.junit.Test;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class QueryFlagTest {
    @Test
    public void getFlags() {
        for (QueryFlag queryFlag : QueryFlag.values()) {
            for (final String flag : queryFlag.getFlags()) {
                assertThat(queryFlag.toString(), flag, not(startsWith("-")));
                assertThat(queryFlag.getName(), not(startsWith("-")));
            }
        }
    }

    @Test
    public void getLongFlag() {
        for (QueryFlag queryFlag : QueryFlag.values()) {
            final String longFlag = queryFlag.getLongFlag();
            assertNotNull(queryFlag.toString(), longFlag);
            assertThat(queryFlag.toString(), longFlag, startsWith("-"));
        }
    }
}
