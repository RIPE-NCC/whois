package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhoisUpdateDoSFilter extends WhoisDoSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisUpdateDoSFilter.class);
    
    public WhoisUpdateDoSFilter(@Value("${dos.filter.max.update:10}") final String dosUpdatesMaxSecs) {
        super(LOGGER, dosUpdatesMaxSecs);
    }

    protected final boolean canProceed(final HttpServletRequest request) {
        if (request == null) {
            return  false;
        } else {
            return !request.getMethod().equalsIgnoreCase(HttpMethod.GET);
        }
    }
}
