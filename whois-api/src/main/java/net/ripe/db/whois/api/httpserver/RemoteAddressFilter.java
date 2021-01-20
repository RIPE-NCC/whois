package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;

@Component
public class RemoteAddressFilter implements Filter {

    private final static Logger LOGGER = LoggerFactory.getLogger(RemoteAddressFilter.class);
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
                final String address = request.getRemoteAddr();
                if (address.startsWith("[") && address.endsWith("]")) {
                    return address.substring(1, address.length() - 1);
                }
                return address;
            }

            LOGGER.debug("Received Client IP address is {}", forwardedAddress);
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
