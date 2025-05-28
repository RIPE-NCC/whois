package net.ripe.db.whois.api.syncupdate;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SyncUpdateUtilsTest {

    @Test
    public void encode() {
        assertThat(SyncUpdateUtils.encode(""), is(""));
        assertThat(SyncUpdateUtils.encode("123"), is("123"));
        assertThat(SyncUpdateUtils.encode("{}"), is("%7B%7D"));
        assertThat(SyncUpdateUtils.encode("{"), is("%7B"));
        assertThat(SyncUpdateUtils.encode("{%7D"), is("%7B%257D"));
        assertThat(SyncUpdateUtils.encode("a b c"), is("a+b+c"));
        assertThat(SyncUpdateUtils.encode("a+b+c"), is("a%2Bb%2Bc"));
    }

}
