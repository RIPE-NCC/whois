package net.ripe.db.whois.api.httpserver;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.IpRanges;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class RemoteAddressFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAddressFilter.class);
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    private final IpRanges ipRanges;

    @Autowired
    public RemoteAddressFilter(final IpRanges ipRanges) {
        this.ipRanges = ipRanges;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            chain.doFilter(new RemoteAddressRequestWrapper(ipRanges, (HttpServletRequest) request), response);
        } else {
            LOGGER.warn("Unexpected request: {}", request);
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private static class RemoteAddressRequestWrapper extends HttpServletRequestWrapper {
        private final IpRanges ipRanges;
        private final HttpServletRequest request;
        private String remoteAddress;

        private RemoteAddressRequestWrapper(final IpRanges ipRanges, final HttpServletRequest request) {
            super(request);
            this.ipRanges = ipRanges;
            this.request = request;
        }

        @Override
        public String getRemoteAddr() {
            if (remoteAddress == null) {
                List<String> forwardedAddresses = getForwardedAddresses();
                if (forwardedAddresses.isEmpty()) {
                    remoteAddress = request.getRemoteAddr();
                } else {
                    remoteAddress = getLastNonRipeNccRemoteAddress(forwardedAddresses);
                }
            }

            return remoteAddress;
        }

        private List<String> getForwardedAddresses() {
            final Enumeration<String> headers = request.getHeaders(HttpHeaders.X_FORWARDED_FOR);
            if (headers == null || !headers.hasMoreElements()) {
                return Collections.emptyList();
            }

            final List<String> result = Lists.newArrayList();
            while (headers.hasMoreElements()) {
                final String forwardedAddressHeader = headers.nextElement();
                for (String nextAddress : COMMA_SPLITTER.split(forwardedAddressHeader)) {
                    result.add(stripZoneIndex(nextAddress));
                }
            }

            return result;
        }

        private String stripZoneIndex(final String ipAddress) {
            return StringUtils.substringBefore(ipAddress, "%");
        }

        private String getLastNonRipeNccRemoteAddress(final List<String> forwardedAddresses) {
            String result = forwardedAddresses.get(0);

            for (final String nextAddress : Lists.reverse(forwardedAddresses)) {
                if (!isAddressInRipeNccRange(nextAddress)) {
                    result = nextAddress;
                    break;
                }
            }

            return result;
        }

        private boolean isAddressInRipeNccRange(final String address) {
            try {
                return ipRanges.isTrusted(IpInterval.parse(address));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Illegal address {}", address);
            }

            return false;
        }
    }
}
