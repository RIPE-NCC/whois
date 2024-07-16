package net.ripe.db.whois.api.httpserver.dos;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.api.conditional.DoSFilterCondition;
import org.eclipse.jetty.servlet.FilterHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static jakarta.ws.rs.HttpMethod.GET;

@Component
@Conditional(DoSFilterCondition.class)
public class LookupDoSFilterHolder extends FilterHolder {

    private static final String MAX_REQUEST_PER_SECOND = "50";
    private static final String MAX_REQUEST_PER_MS = "" + 10 * 60 * 1_000; // high default, 10 minutes

    public LookupDoSFilterHolder(@Value("${ipranges.trusted}") final String trustedIpRanges) {
        this.setFilter(generateWhoisDoSLookupFilter());
        this.setName("LookupDoSFilter");
        this.setInitParameter("maxRequestMs", MAX_REQUEST_PER_MS);
        this.setInitParameter("maxRequestsPerSec", MAX_REQUEST_PER_SECOND);
        this.setInitParameter("enabled", "true");
        this.setInitParameter("delayMs", "-1"); // reject requests over threshold
        this.setInitParameter("remotePort", "false");
        this.setInitParameter("trackSessions", "false");
        this.setInitParameter("insertHeaders", "false");
        this.setInitParameter("ipWhitelist", trustedIpRanges);
    }

    private WhoisDoSFilter generateWhoisDoSLookupFilter(){
        return new WhoisDoSFilter(){
            @Override
            public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
                if (request == null || !GET.equalsIgnoreCase(request.getMethod())){
                    chain.doFilter(request, response);
                    return;
                }
                super.doFilter(request, response, chain);
            }
        };
    }
}
