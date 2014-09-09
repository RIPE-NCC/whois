package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.client.RestClientUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RestClientUtilsTest {

    @Test
    public void encode_curly_braces() {
        assertThat(RestClientUtils.encode(""), is(""));
        assertThat(RestClientUtils.encode("123"), is("123"));
        assertThat(RestClientUtils.encode("{}"), is("%7B%7D"));
        assertThat(RestClientUtils.encode("{"), is("%7B"));
        assertThat(RestClientUtils.encode("{%7D"), is("%7B%257D"));
    }
}

