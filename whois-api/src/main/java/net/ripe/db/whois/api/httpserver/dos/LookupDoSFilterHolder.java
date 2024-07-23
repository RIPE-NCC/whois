package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import static jakarta.ws.rs.HttpMethod.GET;

@Component
public class LookupDoSFilterHolder extends AbstractDoSFilterHolder {

    private final String dosQueriesMaxSecs;

    public LookupDoSFilterHolder(@Value("${dos.filter.enabled:false}") final boolean dosFilterEnabled,
                                 @Value("${ipranges.trusted}") final String trustedIpRanges,
                                 @Value("${dos.filter.max.query:50}") final String dosQueriesMaxSecs) {
        super(dosFilterEnabled, trustedIpRanges);

        this.dosQueriesMaxSecs = dosQueriesMaxSecs;
        this.setInitParameter("maxRequestsPerSec", getMaxRequestPerSec());
    }

    @Override
    protected boolean isAllowedMethod(HttpServletRequest request) {
        return request != null && GET.equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected String getFilerName() {
        return "LookupDoSFilter";
    }

    private String getMaxRequestPerSec() {
        return dosQueriesMaxSecs;
    }
}
