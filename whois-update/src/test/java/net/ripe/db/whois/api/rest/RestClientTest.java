package net.ripe.db.whois.api.rest;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class RestClientTest {
    @Test
    public void test_multiple_passwords() {
        RestClient restClient = new RestClient();
        assertThat(restClient.formatPasswords("1", "2", "3"), Matchers.is("?password=1&password=2&password=3"));
    }

    @Test
    public void test_single_password() {
        RestClient restClient = new RestClient();
        assertThat(restClient.formatPasswords("1"), Matchers.is("?password=1"));
    }
}
