package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.client.RestClientUtils;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RestClientUtilsTest {

    @Test
    public void encode() {
        assertThat(RestClientUtils.encode(""), is(""));
        assertThat(RestClientUtils.encode("123"), is("123"));
        assertThat(RestClientUtils.encode("{}"), is("%7B%7D"));
        assertThat(RestClientUtils.encode("{"), is("%7B"));
        assertThat(RestClientUtils.encode("{%7D"), is("%7B%257D"));
        assertThat(SyncUpdateUtils.encode("a b c"), is("a+b+c"));
        assertThat(SyncUpdateUtils.encode("a+b+c"), is("a%2Bb%2Bc"));
    }
}

