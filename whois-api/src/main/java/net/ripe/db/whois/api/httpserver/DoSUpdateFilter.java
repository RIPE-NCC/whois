package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class DoSUpdateFilter extends WhoisDoSFilter {

    @Override
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request != null) {
            final String method = request.getMethod();
            if (!"GET".equalsIgnoreCase(method)) {
                // Only apply DoSFilter logic for UPDATE requests
                super.doFilter(request, response, chain);
                return;
            }
        }
        // For GET requests, just pass through the chain
        chain.doFilter(request, response);
    }
}
