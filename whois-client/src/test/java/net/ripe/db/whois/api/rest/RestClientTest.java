package net.ripe.db.whois.api.rest;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Client;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

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
}
