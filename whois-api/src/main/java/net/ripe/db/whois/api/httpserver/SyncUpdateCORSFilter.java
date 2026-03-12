package net.ripe.db.whois.api.httpserver;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriBuilder;
import net.ripe.db.whois.query.QueryMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

/*
 * Pre-flight check is skipped for syncudates as it allows APPLICATION_FORM_URLENCODED
 * It allows cross-origin requests from specified hosts
 */
//TODO [MA] : explore use of Sec-Fetch-* headers instead
@Component
public class SyncUpdateCORSFilter implements Filter {

    final String[] allowedHostsforCrossOrigin;

    public SyncUpdateCORSFilter(@Value("${whois.allow.cross.origin.hosts:}") final String[] allowedHostsforCrossOrigin) {
        this.allowedHostsforCrossOrigin = allowedHostsforCrossOrigin;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if(!isSyncUpdateAllowed(httpRequest)) {
            httpResponse.setStatus(HttpStatus.FORBIDDEN_403);
            httpResponse.getWriter().write("Access denied for syncupdates");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private boolean isSyncUpdateAllowed(final HttpServletRequest request) {
        if(StringUtils.isEmpty(request.getHeader(HttpHeaders.ORIGIN))) {
            return true;
        }

        final String host = URI.create(request.getHeader(HttpHeaders.ORIGIN)).getHost();
        return  Arrays.stream(allowedHostsforCrossOrigin).anyMatch( allowedhost -> allowedhost.equalsIgnoreCase(host));
    }
}
