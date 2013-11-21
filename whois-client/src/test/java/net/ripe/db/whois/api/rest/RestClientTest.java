package net.ripe.db.whois.api.rest;

import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {

    RestClient restClient = new RestClient();

    @Test
    public void test_multiple_passwords() {
        assertThat(restClient.queryParams(restClient.queryParam("password", "1", "2", "3")), Matchers.is("?password=1&password=2&password=3"));
    }

    @Test
    public void test_single_password() {
        assertThat(restClient.queryParams(restClient.queryParam("password", "1")), Matchers.is("?password=1"));
    }

    @Test(expected = javax.ws.rs.ProcessingException.class)
    public void testUriIsEncoded() throws Exception {
         //unexpected: java.net.URISyntaxException
        restClient.search(
                 "147.102.0.0 - 147.102.255.255",
                 Sets.newHashSet("TEST"),
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET,
                 Collections.EMPTY_SET);
    }
}
