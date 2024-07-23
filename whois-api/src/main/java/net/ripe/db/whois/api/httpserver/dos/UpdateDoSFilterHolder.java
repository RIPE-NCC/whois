package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import static jakarta.ws.rs.HttpMethod.GET;
import static jakarta.ws.rs.HttpMethod.POST;
import static jakarta.ws.rs.HttpMethod.PUT;

@Component
public class UpdateDoSFilterHolder extends AbstractDoSFilterHolder {

    private final String dosUpdatesMaxSecs;

    public UpdateDoSFilterHolder(@Value("${dos.filter.enabled:false}") final boolean dosFilterEnabled,
                                 @Value("${ipranges.trusted}") final String trustedIpRanges,
                                 @Value("${dos.filter.max.updates:10}") final String dosUpdatesMaxSecs) {
        super(dosFilterEnabled, trustedIpRanges);

        this.dosUpdatesMaxSecs = dosUpdatesMaxSecs;
        this.setInitParameter("maxRequestMs", getMaxRequestPerms());
        this.setInitParameter("maxRequestsPerSec", getMaxRequestPerSec());

    }

    @Override
    protected boolean isAllowedMethod(final HttpServletRequest request) {
        return request != null && (POST.equalsIgnoreCase(request.getMethod()) || PUT.equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected String getFilerName() {
        return "UpdateDoSFilter";
    }

    private String getMaxRequestPerms() {
        return "" + 10 * Integer.parseInt(dosUpdatesMaxSecs) * 1_000;
    }

    private String getMaxRequestPerSec() {
        return dosUpdatesMaxSecs;
    }
}
