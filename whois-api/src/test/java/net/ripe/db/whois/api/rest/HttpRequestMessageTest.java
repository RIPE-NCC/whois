package net.ripe.db.whois.api.rest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HttpRequestMessageTest {

    private MockHttpServletRequest request;

    @Before
    public void setup() {
        request = new MockHttpServletRequest("GET", "/some/path");
    }

    @Test
    public void log_no_headers() {
        assertThat(toString(request), is("GET /some/path\n"));
    }

    @Test
    public void log_single_header() {
        request.addHeader("name", "value");

        assertThat(toString(request), is("GET /some/path\nHeader: name=value\n"));
    }

    @Test
    public void log_multiple_headers() {
        request.addHeader("header1", "value1");
        request.addHeader("header2", "value2");
        request.addHeader("header3", "value3");

        assertThat(toString(request), is("GET /some/path\nHeader: header1=value1\nHeader: header2=value2\nHeader: header3=value3\n"));
    }

    @Test
    public void log_empty_query_string() {
        request.setQueryString("");

        assertThat(toString(request), is("GET /some/path\n"));
    }

    @Test
    public void log_encoded_query_parameter() {
        request.setQueryString("password=p%3Fssword%26");

        assertThat(toString(request), is("GET /some/path?password=FILTERED\n"));
    }

    @Test
    public void log_query_parameters_including_password() {
        request.setQueryString("password=secret");
        assertThat(toString(request), is("GET /some/path?password=FILTERED\n"));

        request.setQueryString("password=secret&param");
        assertThat(toString(request), is("GET /some/path?password=FILTERED&param\n"));

        request.setQueryString("password=secret&password=other");
        assertThat(toString(request), is("GET /some/path?password=FILTERED&password=FILTERED\n"));

        request.setQueryString("password=secret&password=other&param=value");
        assertThat(toString(request), is("GET /some/path?password=FILTERED&password=FILTERED&param=value\n"));

        request.setQueryString("param=value&password=secret&password=other");
        assertThat(toString(request), is("GET /some/path?param=value&password=FILTERED&password=FILTERED\n"));

        request.setQueryString("param=value&password=secret&param=password");
        assertThat(toString(request), is("GET /some/path?param=value&password=FILTERED&param=password\n"));
    }

    // helper methods
    private String toString(final HttpServletRequest request) {
        return new HttpRequestMessage(request).toString();
    }

}