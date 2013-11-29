package net.ripe.db.whois.api.rest;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RestClientTest {

    @Test
    public void test_multiple_passwords() {
        assertThat(RestClient.joinQueryParams(RestClient.createQueryParams("password", "1", "2", "3")), is("?password=1&password=2&password=3"));
    }

    @Test
    public void test_single_password() {
        assertThat(RestClient.joinQueryParams(RestClient.createQueryParams("password", "1")), is("?password=1"));
    }

    @Test(expected = javax.ws.rs.ProcessingException.class)
    public void testUriIsEncoded() throws Exception {
         //unexpected: java.net.URISyntaxException
        new RestClient().search(
                 "147.102.0.0 - 147.102.255.255",
                 Sets.newHashSet("TEST"),
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET);
    }
}
