package net.ripe.db.whois.api.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HttpRequestMessageTest {

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setup() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/some/path");
        lenient().when(request.getHeaders(any(String.class))).thenReturn(Collections.emptyEnumeration());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    }

    @Test
    public void log_no_headers() {
        assertThat(toString(request), is("GET /some/path\n"));
    }

    @Test
    public void log_single_header() {
        when(request.getHeaderNames()).thenReturn(enumeration("name"));
        when(request.getHeaders("name")).thenReturn(enumeration("value"));

        assertThat(toString(request), is("GET /some/path\nHeader: name=value\n"));
    }

    @Test
    public void log_multiple_headers() {
        when(request.getHeaderNames()).thenReturn(enumeration("header1", "header2", "header3"));
        when(request.getHeaders("header1")).thenReturn(enumeration("value1"));
        when(request.getHeaders("header2")).thenReturn(enumeration("value2"));
        when(request.getHeaders("header3")).thenReturn(enumeration("value3"));

        assertThat(toString(request), is("GET /some/path\nHeader: header1=value1\nHeader: header2=value2\nHeader: header3=value3\n"));
    }

    @Test
    public void log_empty_query_string() {
        when(request.getQueryString()).thenReturn("");

        assertThat(toString(request), is("GET /some/path\n"));
    }

    @Test
    public void log_encoded_query_parameter() {
        when(request.getQueryString()).thenReturn("overRIDE=username,user%3fSecret&password=p%3Fssword%26");

        assertThat(toString(request), is("GET /some/path?overRIDE=username,FILTERED&password=FILTERED\n"));
    }

    @Test
    public void log_query_parameters_including_password() {
        when(request.getQueryString()).thenReturn("password=secret");
        assertThat(toString(request), is("GET /some/path?password=FILTERED\n"));

        when(request.getQueryString()).thenReturn("password=secret&param");
        assertThat(toString(request), is("GET /some/path?password=FILTERED&param\n"));

        when(request.getQueryString()).thenReturn("password=secret&password=other");
        assertThat(toString(request), is("GET /some/path?password=FILTERED&password=FILTERED\n"));

        when(request.getQueryString()).thenReturn("override=username,USER123PASS&override=username2,USER123PASS,reason%20text&password=secret&password=other&param=value");
        assertThat(toString(request), is("GET /some/path?override=username,FILTERED&override=username2,FILTERED,reason%20text&password=FILTERED&password=FILTERED&param=value\n"));

        when(request.getQueryString()).thenReturn("param=value&override=username,secretuserpass&password=secret&password=other&override=username2%2Csecretuserpass&");
        assertThat(toString(request), is("GET /some/path?param=value&override=username,FILTERED&password=FILTERED&password=FILTERED&override=username2,FILTERED&\n"));

        when(request.getQueryString()).thenReturn("param=value&password=secret&param=password");
        assertThat(toString(request), is("GET /some/path?param=value&password=FILTERED&param=password\n"));
    }

    // helper methods
    private String toString(final HttpServletRequest request) {
        return new HttpRequestMessage(request).toString();
    }

    private Enumeration<String> enumeration(final String ... values) {
        final Vector<String> vector = new Vector<>(Arrays.asList(values));
        return vector.elements();
    }

}
