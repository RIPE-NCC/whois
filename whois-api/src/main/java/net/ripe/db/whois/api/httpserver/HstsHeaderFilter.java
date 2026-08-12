package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HstsHeaderFilter extends OncePerRequestFilter {
    private static final String MAX_AGE = "max-age=31536000";

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        if (HttpScheme.HTTPS.is(request.getScheme())) {
            response.setHeader(HttpHeader.STRICT_TRANSPORT_SECURITY.asString(), MAX_AGE);
        }

        chain.doFilter(request, response);
    }
}
