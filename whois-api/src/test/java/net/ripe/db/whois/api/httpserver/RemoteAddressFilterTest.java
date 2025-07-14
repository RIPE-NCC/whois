package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    /**
     * Test that a RFC 3986 section 3.2.2 formatted IPv6 address is handled properly (e.g. [::1] vs ::1).
     * Jetty started passing IPv6 addresses in this format since 9.4.32.
     * @throws Exception shouldn't happen
     */
    @Test
    public void support_rfc3986_ipv6() throws Exception {
        when(request.getRemoteAddr()).thenReturn("[::1]");

        subject.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(argThat(new CheckRemoteAddress("::1")), any(ServletResponse.class));
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
