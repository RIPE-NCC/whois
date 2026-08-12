package net.ripe.db.whois.api;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CspHeaderFilter extends OncePerRequestFilter {
    private static final String CSP = "default-src 'none'; frame-ancestors 'none'; sandbox";

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        response.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, CSP);
        chain.doFilter(request, response);
    }
}

