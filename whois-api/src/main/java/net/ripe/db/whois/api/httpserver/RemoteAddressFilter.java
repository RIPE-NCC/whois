package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@Component
public class RemoteAddressFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAddressFilter.class);

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

        //TODO: Ipv6 address will have brackets by default. Make changes to ipv6 parser instead and check all usages of getRemoteAddress
        private static String getRemoteAddress(final HttpServletRequest request) {
            final String address = request.getRemoteAddr();
            return (address.startsWith("[") && address.endsWith("]")) ? address.substring(1, address.length() - 1) : address;
        }
    }
}
