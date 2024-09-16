package net.ripe.db.whois.query;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

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
            assertThat(queryFlag.toString(), longFlag, not(nullValue()));
            assertThat(queryFlag.toString(), longFlag, startsWith("-"));
        }
    }
}
