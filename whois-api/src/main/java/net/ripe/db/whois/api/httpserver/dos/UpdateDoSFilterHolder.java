package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static jakarta.ws.rs.HttpMethod.GET;

@Component
public class UpdateDoSFilterHolder extends AbstractDoSFilterHolder {

    public UpdateDoSFilterHolder(@Value("${dos.filter.enabled:false}") final boolean dosFilterEnabled,
                                 @Value("${ipranges.trusted}") final String trustedIpRanges,
                                 @Value("${dos.filter.max.updates:10}") final String dosUpdatesMaxSecs) {
        super(dosFilterEnabled, trustedIpRanges);

        final String maxRequestPerMs = "" + 10 * Integer.parseInt(dosUpdatesMaxSecs) * 1_000;
        this.setFilter(generateWhoisDoSUpdateFilter());
        this.setName("UpdateDoSFilter");
        this.setInitParameter("maxRequestMs", maxRequestPerMs);
        this.setInitParameter("maxRequestsPerSec", dosUpdatesMaxSecs);
    }

    private WhoisDoSFilter generateWhoisDoSUpdateFilter(){
        return new WhoisDoSFilter(){
            @Override
            public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
                if (request == null || GET.equalsIgnoreCase(request.getMethod())){
                    chain.doFilter(request, response);
                    return;
                }
                super.doFilter(request, response, chain);
            }
        };
    }
}
