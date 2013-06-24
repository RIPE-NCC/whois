package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.IpRanges;
import org.eclipse.jetty.http.HttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAddressFilterTest {
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;

    @Mock FilterChain filterChain;
    @Mock IpRanges ipRanges;

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
    public void forward_header_with_zone_index() throws Exception {
        when(request.getHeaders(HttpHeaders.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Lists.newArrayList("193.0.20.1%1024")));
        when(request.getRemoteAddr()).thenReturn("10.0.0.0");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("193.0.20.1")), any(ServletResponse.class));
    }

    @Test
    public void forward_header_ripe_range() throws Exception {
        when(ipRanges.isTrusted(IpInterval.parse("193.0.20.1"))).thenReturn(true);
        when(request.getHeaders(HttpHeaders.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Lists.newArrayList("74.125.136.99, 193.0.20.1")));
        when(request.getRemoteAddr()).thenReturn("10.0.0.0");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("74.125.136.99")), any(ServletResponse.class));
    }

    @Test
    public void forward_headers_ripe_range() throws Exception {
        when(ipRanges.isTrusted(IpInterval.parse("193.0.20.1"))).thenReturn(true);
        when(request.getHeaders(HttpHeaders.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Lists.newArrayList("74.125.136.99", "193.0.20.1")));
        when(request.getRemoteAddr()).thenReturn("10.0.0.0");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("74.125.136.99")), any(ServletResponse.class));
    }

    private static class CheckRemoteAddress extends ArgumentMatcher<ServletRequest> {
        private final String address;

        private CheckRemoteAddress(final String address) {
            this.address = address;
        }

        @Override
        public boolean matches(final Object argument) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) argument;
            assertThat(httpServletRequest.getRemoteAddr(), is(address));
            return true;
        }
    }
}
