package net.ripe.db.whois.api.httpserver.dos;

import com.google.common.net.InetAddresses;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.hazelcast.HazelcastBlockedIps;
import net.ripe.db.whois.common.hazelcast.WhoisHazelcastBlockedIps;
import net.ripe.db.whois.common.ip.IpInterval;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class BlockListFilter implements Filter {

    private final HazelcastBlockedIps hazelcastBlockedIps;

    public BlockListFilter(final HazelcastBlockedIps hazelcastBlockedIps){
        this.hazelcastBlockedIps = hazelcastBlockedIps;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (isBlockedIp(httpRequest.getRemoteAddr())){
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
            httpResponse.getWriter().write("You have been permanently blocked. Please contact support");
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
        return hazelcastBlockedIps.getIpBlockedSet().stream()
                .anyMatch(ipRange -> ipRange.getClass().equals(parsed.getClass()) && ipRange.contains(parsed));
    }
}
