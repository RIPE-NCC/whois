package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhoisQueryDoSFilter extends WhoisDoSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisQueryDoSFilter.class);

    public WhoisQueryDoSFilter() {
        super(LOGGER);
    }

    protected final boolean canProceed(final HttpServletRequest request) {
        if (request == null) {
            return  false;
        } else {
            return request.getMethod().equalsIgnoreCase(HttpMethod.GET);
        }
    }

}
