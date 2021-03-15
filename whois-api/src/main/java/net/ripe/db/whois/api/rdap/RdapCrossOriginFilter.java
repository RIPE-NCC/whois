package net.ripe.db.whois.api.rdap;

import com.google.common.net.HttpHeaders;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;
import java.io.IOException;

/**
 * RDAP specific changes to Cross-Origin Resource Sharing (CORS).
 *
 *   "When responding to queries, it is RECOMMENDED that servers use the
 *    Access-Control-Allow-Origin header field, as specified by
 *    [W3C.REC-cors-20140116].  A value of "*" is suitable when RDAP is
 *    used for public resources."
 *
 * Ref. https://tools.ietf.org/html/rfc7480#section-5.6
 *
 */
public class RdapCrossOriginFilter extends CrossOriginFilter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException
    {
        handle((HttpServletRequest)request, (HttpServletResponse)response);
        super.doFilter(request, response, chain);
    }

    private void handle(final HttpServletRequest request, final HttpServletResponse response) {
        if ((request.getHeader(HttpHeaders.ORIGIN) == null) && request.getMethod().equals(HttpMethod.GET)) {
            response.setHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        }
     }
}
