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
        return IpUtil.isExistingIp(candidate, blockListJmx.getIpv4blockedSet(), blockListJmx.getIpv6blockedSet());
    }
}
