package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAddressFilterTest {
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;

    @Mock FilterChain filterChain;

    @InjectMocks RemoteAddressFilter subject;

    @Test
    public void init() throws Exception {
        subject.init(null);
    }

    @Test
    public void destroy() {
        subject.destroy();
    }

    @Test
    public void no_servletRequest() throws Exception {
        final ServletRequest servletRequest = mock(ServletRequest.class);
        final ServletResponse servletResponse = mock(ServletResponse.class);

        subject.doFilter(servletRequest, servletResponse, filterChain);

        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @Test
    public void no_forward_header() throws Exception {
        when(request.getRemoteAddr()).thenReturn("10.0.0.0");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("10.0.0.0")), any(ServletResponse.class));
    }

    @Test
    public void forward_header() throws Exception {
        when(request.getHeaders(HttpHeaders.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Lists.newArrayList("193.0.20.1")));
        when(request.getRemoteAddr()).thenReturn("10.0.0.0");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("193.0.20.1")), any(ServletResponse.class));
    }

    @Test
    public void forward_headers_ripe_range() throws Exception {
        when(request.getHeaders(HttpHeaders.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Lists.newArrayList("74.125.136.99", "193.0.20.1")));
        when(request.getRemoteAddr()).thenReturn("10.0.0.0");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("74.125.136.99")), any(ServletResponse.class));
    }

    @Test
    public void forward_header_comma_separated_values() throws Exception {
        when(request.getHeaders(HttpHeaders.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Lists.newArrayList("74.125.136.99, 193.0.20.1")));
        when(request.getRemoteAddr()).thenReturn("10.0.0.0");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("193.0.20.1")), any(ServletResponse.class));
    }

    private static class CheckRemoteAddress implements ArgumentMatcher<ServletRequest> {
        private final String address;

        private CheckRemoteAddress(final String address) {
            this.address = address;
        }

        @Override
        public boolean matches(final ServletRequest servletRequest) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            assertThat(httpServletRequest.getRemoteAddr(), is(address));
            return true;
        }
    }
}
