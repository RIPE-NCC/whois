package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.ip.IpInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MaintenanceModeFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceModeFilter.class);

    private static final Pattern UPDATE_URI = Pattern.compile("(?i)^/whois/syncupdates(?:/|$)");
    private static final Set<String> UPDATE_METHODS = ImmutableSet.of("POST", "PUT", "DELETE");

    private final MaintenanceMode maintenanceMode;

    @Autowired
    public MaintenanceModeFilter(final MaintenanceMode maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        // sadly there is no support for removing filters, so we'll have to make a fast track for normal operations
        if (maintenanceMode.allowUpdate()) {
            chain.doFilter(request, response);

        } else if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final String uri = httpRequest.getRequestURI();
            final String method = httpRequest.getMethod();
            final IpInterval remoteIp = IpInterval.parse(httpRequest.getRemoteAddr());

            final boolean isUpdate = UPDATE_METHODS.contains(method) || UPDATE_URI.matcher(uri).matches();
            final boolean allowed = isUpdate ? maintenanceMode.allowUpdate(remoteIp) : maintenanceMode.allowRead(remoteIp);

            if (allowed) {
                chain.doFilter(request, response);
            } else {
                final HttpServletResponse httpResponse = (HttpServletResponse)response;
                httpResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        } else {
            LOGGER.warn("Unexpected request: {}", request);
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
