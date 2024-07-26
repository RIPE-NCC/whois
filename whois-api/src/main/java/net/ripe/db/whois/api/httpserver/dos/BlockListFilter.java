package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.hazelcast.BlockListJmx;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class BlockListFilter implements Filter {

    private final BlockListJmx blockListJmx;

    public BlockListFilter(final BlockListJmx blockListJmx){
        this.blockListJmx = blockListJmx;
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
        // TODO: Duplicated in WhoisDoSFilter, maybe put this into a utils or into a interface with a default method
        final IpInterval<?> parsed = IpInterval.parse(candidate);
        return switch (parsed) {
            case Ipv4Resource ipv4Resource -> {
                for (Ipv4Resource entry : blockListJmx.getIpv4blockedSet()) {
                    if (entry.contains(ipv4Resource)) {
                        yield true;
                    }
                }
                yield false;
            }
            case Ipv6Resource ipv6Resource -> {
                for (Ipv6Resource entry : blockListJmx.getIpv6blockedSet()) {
                    if (entry.contains(ipv6Resource)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> false;
        };
    }
}
