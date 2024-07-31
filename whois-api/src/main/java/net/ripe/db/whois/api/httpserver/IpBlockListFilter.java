package net.ripe.db.whois.api.httpserver;

import com.google.common.net.InetAddresses;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.hazelcast.BlockedIps;
import net.ripe.db.whois.common.ip.IpInterval;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class IpBlockListFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpBlockListFilter.class);

    private final BlockedIps blockedIps;

    public IpBlockListFilter(final BlockedIps blockedIps){
        this.blockedIps = blockedIps;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        if (isBlockedIp(httpRequest.getRemoteAddr())){

            final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
            httpResponse.getWriter().write("Your account has been permanently blocked due to suspected abusive " +
                    "behavior. Please contact support for further assistance.");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private boolean isBlockedIp(final String candidate) {
        final IpInterval<?> parsed = IpInterval.asIpInterval(InetAddresses.forString(candidate));
        try {
            return blockedIps.getIpBlockedSet().stream()
                    .anyMatch(ipRange -> ipRange.getClass().equals(parsed.getClass()) && ipRange.contains(parsed));
        } catch (Exception ex){
            LOGGER.error("Failed to check if remote address is in block list due to", ex);
        }
        return false;
    }
}
