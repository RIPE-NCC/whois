package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import static jakarta.ws.rs.HttpMethod.GET;

@Component
public class LookupDoSFilterHolder extends AbstractDoSFilterHolder {

    public static final String MAX_REQUEST_PER_SECOND = "50";
    private static final String MAX_REQUEST_PER_MS = "" + 10 * 60 * 1_000; // high default, 10 minutes

    public LookupDoSFilterHolder(@Value("${dos.filter.enabled:false}") final boolean dosFilterEnabled,
                                 @Value("${ipranges.trusted}") final String trustedIpRanges) {
        super(dosFilterEnabled, trustedIpRanges);

        this.setFilter(generateWhoisDoSFilter());
        this.setName(getFilerName());
        this.setInitParameter("maxRequestMs", getMaxRequestPerms());
        this.setInitParameter("maxRequestsPerSec", getMaxRequestPerSec());
    }

    @Override
    protected boolean isAllowedMethod(HttpServletRequest request) {
        return request == null || !GET.equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected String getFilerName() {
        return "LookupDoSFilter";
    }

    @Override
    protected String getMaxRequestPerms() {
        return MAX_REQUEST_PER_MS;
    }

    @Override
    protected String getMaxRequestPerSec() {
        return MAX_REQUEST_PER_SECOND;
    }
}
