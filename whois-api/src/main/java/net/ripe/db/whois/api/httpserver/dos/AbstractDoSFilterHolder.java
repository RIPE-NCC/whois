package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.FilterHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AbstractDoSFilterHolder extends FilterHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDoSFilterHolder.class);

    AbstractDoSFilterHolder(final boolean dosFilterEnabled, final String trustedIpRanges){
        if (!dosFilterEnabled) {
            LOGGER.info("DoSFilter is *not* enabled");
            this.setFilter(generateBasicFilter());
            return;
        }

        this.setInitParameter("enabled", "true");
        this.setInitParameter("delayMs", "-1"); // reject requests over threshold
        this.setInitParameter("remotePort", "false");
        this.setInitParameter("trackSessions", "false");
        this.setInitParameter("insertHeaders", "false");
        this.setInitParameter("ipWhitelist", trustedIpRanges);
    }

    private Filter generateBasicFilter(){
        return new WhoisDoSFilter(){
            @Override
            public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
                super.doFilter(request, response, chain);
            }
        };
    }
}
