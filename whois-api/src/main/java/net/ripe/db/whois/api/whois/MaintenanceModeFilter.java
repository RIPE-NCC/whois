package net.ripe.db.whois.api.whois;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.domain.ip.IpInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MaintenanceModeFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceModeFilter.class);

    private static final Pattern WHICH_WHOIS_API = Pattern.compile("(?i)^/whois/(\\w+)(?:/|$)");
    private static final Set<String> READ_API = ImmutableSet.of("geolocation", "grs-lookup", "grs-search", "lookup", "search", "tags", "version");
    private static final Set<String> UPDATE_API = ImmutableSet.of("create", "delete", "modify", "syncupdates", "update");

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
            final Matcher matcher = WHICH_WHOIS_API.matcher(uri);
            if (matcher.matches()) {
                final String service = matcher.group(1).toLowerCase();
                final IpInterval remoteIp = IpInterval.parse(httpRequest.getRemoteAddr());

                if (UPDATE_API.contains(service) && maintenanceMode.allowUpdate(remoteIp) ||
                        READ_API.contains(service) && maintenanceMode.allowRead(remoteIp)) {
                    chain.doFilter(request, response);
                }
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
