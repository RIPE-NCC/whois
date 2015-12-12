package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;

@Component
public class RemoteAddressFilter implements Filter {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            chain.doFilter(new RemoteAddressRequestWrapper((HttpServletRequest) request), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private static final class RemoteAddressRequestWrapper extends HttpServletRequestWrapper {
        private String remoteAddress;

        private RemoteAddressRequestWrapper(final HttpServletRequest request) {
            super(request);
            this.remoteAddress = getRemoteAddress(request);
        }

        @Override
        public String getRemoteAddr() {
            return remoteAddress;
        }

        private static String getRemoteAddress(final HttpServletRequest request) {
            final String forwardedAddress = getForwardedAddress(request);
            if (forwardedAddress == null) {
                return request.getRemoteAddr();
            }
            return forwardedAddress;
        }

        @Nullable
        private static String getForwardedAddress(final HttpServletRequest request) {
            final Enumeration<String> headers = request.getHeaders(HttpHeaders.X_FORWARDED_FOR);
            if (headers == null || !headers.hasMoreElements()) {
                return null;
            }

            final String header = headers.nextElement();
            if (Strings.isNullOrEmpty(header)) {
                return null;
            }

            return Iterables.getLast(COMMA_SPLITTER.split(header));
        }
    }
}
