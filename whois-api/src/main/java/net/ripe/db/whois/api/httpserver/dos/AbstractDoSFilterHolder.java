package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.FilterHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public abstract class AbstractDoSFilterHolder extends FilterHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDoSFilterHolder.class);

    // 10 minutes until we consider the request is a violation and drop it
    private static final String MAX_REQUEST_PER_MS = "" + 10 * 60 * 1_000;

    AbstractDoSFilterHolder(final boolean dosFilterEnabled, final String trustedIpRanges) {
        if (!dosFilterEnabled) {
            LOGGER.info("DoSFilter is *not* enabled");
        }

        this.setFilter(generateWhoisDoSFilter());
        this.setName(getFilerName());
        this.setInitParameter("enabled", Boolean.toString(dosFilterEnabled));
        this.setInitParameter("delayMs", "-1"); // reject requests over threshold
        this.setInitParameter("remotePort", "false");
        this.setInitParameter("trackSessions", "false");
        this.setInitParameter("insertHeaders", "false");
        this.setInitParameter("ipWhitelist", trustedIpRanges);
        this.setInitParameter("maxRequestMs", getMaxRequestPerms());
    }

    protected abstract boolean isAllowedMethod(final HttpServletRequest request);

    protected abstract String getFilerName();

    protected WhoisDoSFilter generateWhoisDoSFilter(){
        return new WhoisDoSFilter(){
            @Override
            public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
                if (isAllowedMethod(request)){
                    super.doFilter(request, response, chain);
                    return;
                }
                chain.doFilter(request, response);
            }
        };
    }

    protected String getMaxRequestPerms() {
        return MAX_REQUEST_PER_MS;
    }
}
