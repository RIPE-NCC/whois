package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhoisUpdateDoSFilter extends WhoisDoSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisUpdateDoSFilter.class);

    public WhoisUpdateDoSFilter() {
        super(LOGGER);
    }

    protected final boolean canProceed(final HttpServletRequest request) {
        if (request == null) {
            return  false;
        } else {
            return !request.getMethod().equalsIgnoreCase(HttpMethod.GET);
        }
    }
}
