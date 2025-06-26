package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhoisQueryDoSFilter extends WhoisDoSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisQueryDoSFilter.class);

    public WhoisQueryDoSFilter(@Value("${dos.filter.max.query:50}") final String dosQueriesMaxSecs) {
        super(LOGGER, dosQueriesMaxSecs);
    }

    protected final boolean canProceed(final HttpServletRequest request) {
        if (request == null) {
            return  false;
        } else {
            return request.getMethod().equalsIgnoreCase(HttpMethod.GET);
        }
    }

}
