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
import net.ripe.db.whois.common.hazelcast.IpBlockManager;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.query.QueryMessages;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class IpBlockListFilter implements Filter {

    private final IpBlockManager ipBlockManager;

    public IpBlockListFilter(final IpBlockManager ipBlockManager){
        this.ipBlockManager = ipBlockManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        if (ipBlockManager.isBlockedIp(httpRequest.getRemoteAddr())){
            sendError((HttpServletResponse) servletResponse, httpRequest);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private static void sendError(final HttpServletResponse httpResponse, final HttpServletRequest httpRequest) throws IOException {
        //TODO: refactor using Response.writeError() when upgrading to Jetty 12 https://jetty.org/docs/jetty/12/programming-guide/migration/11-to-12.html
        httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS_429);
        httpResponse.getWriter().write(QueryMessages.accessDeniedForAbuse(httpRequest.getRemoteAddr()).getFormattedText());
    }
}
