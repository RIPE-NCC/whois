package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExtensionOverridesAcceptHeaderFilterTest {

    @Mock HttpServletRequest request;
    @Mock ServletResponse response;
    @Mock FilterChain chain;
    private ExtensionOverridesAcceptHeaderFilter subject;

    @Before
    public void setup() {
        subject = new ExtensionOverridesAcceptHeaderFilter();
    }

    @Test
    public void xml_extension() throws Exception {
        final HttpServletRequest request = doFilter("http", "localhost", "/whois/test/inet6num/No%20clue%20what%20the%20range%20is.xml");

        assertThat(request.getRequestURI(), is("/whois/test/inet6num/No%20clue%20what%20the%20range%20is"));
        assertThat(request.getRequestURL().toString(), is("http://localhost/whois/test/inet6num/No%20clue%20what%20the%20range%20is"));
        assertThat(request.getHeader("Accept"), is("application/xml"));
        assertThat(toList(request.getHeaders("Accept")), contains("application/xml"));
    }

    @Test
    public void json_extension() throws Exception {
        final HttpServletRequest request = doFilter("http", "localhost", "/whois/test/inet6num/No%20clue%20what%20the%20range%20is.json");

        assertThat(request.getRequestURI(), is("/whois/test/inet6num/No%20clue%20what%20the%20range%20is"));
        assertThat(request.getRequestURL().toString(), is("http://localhost/whois/test/inet6num/No%20clue%20what%20the%20range%20is"));
        assertThat(request.getHeader("Accept"), is("application/json"));
        assertThat(toList(request.getHeaders("Accept")), contains("application/json"));
    }

    @Test
    public void unknown_extension() throws Exception {
        final HttpServletRequest request = doFilter("http", "localhost", "/whois/test/inet6num/No%20clue%20what%20the%20range%20is.unknown");

        assertThat(request.getRequestURI(), is("/whois/test/inet6num/No%20clue%20what%20the%20range%20is.unknown"));
        assertThat(request.getRequestURL().toString(), is("http://localhost/whois/test/inet6num/No%20clue%20what%20the%20range%20is.unknown"));
        assertThat(request.getHeader("Accept"), is(nullValue()));
        assertThat(request.getHeaders("Accept"), is(nullValue()));
    }

    @Test
    public void no_extension() throws Exception {
        final HttpServletRequest request = doFilter("http", "localhost", "/whois/test/inet6num/No%20clue%20what%20the%20range%20is");

        assertThat(request.getRequestURI(), is("/whois/test/inet6num/No%20clue%20what%20the%20range%20is"));
        assertThat(request.getRequestURL().toString(), is("http://localhost/whois/test/inet6num/No%20clue%20what%20the%20range%20is"));
        assertThat(request.getHeader("Accept"), is(nullValue()));
        assertThat(request.getHeaders("Accept"), is(nullValue()));
    }

    @Test
    public void root_slash() throws Exception {
        final HttpServletRequest request = doFilter("http", "localhost", "/");

        assertThat(request.getRequestURI(), is("/"));
        assertThat(request.getRequestURL().toString(), is("http://localhost/"));
        assertThat(request.getHeader("Accept"), is(nullValue()));
        assertThat(request.getHeaders("Accept"), is(nullValue()));
    }

    @Test
    public void root_no_slash() throws Exception {
        final HttpServletRequest request = doFilter("http", "localhost", "");

        assertThat(request.getRequestURI(), is(""));
        assertThat(request.getRequestURL().toString(), is("http://localhost"));
        assertThat(request.getHeader("Accept"), is(nullValue()));
        assertThat(request.getHeaders("Accept"), is(nullValue()));
    }

    // helper methods

    private HttpServletRequest doFilter(final String scheme, final String host, final String path) throws IOException, ServletException {
        final List<HttpServletRequest> updatedRequest = Lists.newArrayList();
        final StringBuffer url = new StringBuffer(String.format("%s://%s%s", scheme, host, path));
        when(request.getRequestURI()).thenReturn(path);
        when(request.getRequestURL()).thenReturn(url);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                updatedRequest.add((HttpServletRequest) invocation.getArguments()[0]);
                return null;
            }
        }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        subject.doFilter(request, response, chain);

        return updatedRequest.iterator().next();
    }

    private List<String> toList(final Enumeration<String> enumeration) {
        final List<String> result = Lists.newArrayList();
        while (enumeration.hasMoreElements()) {
            result.add(enumeration.nextElement());
        }
        return result;
    }
}