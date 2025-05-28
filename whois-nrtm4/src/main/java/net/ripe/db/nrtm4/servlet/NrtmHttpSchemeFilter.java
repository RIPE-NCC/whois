package net.ripe.db.nrtm4.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NrtmHttpSchemeFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (isHttps(httpRequest)){
            chain.doFilter(request, response);
        } else {
            final HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.sendError(HttpStatus.UPGRADE_REQUIRED_426, "HTTPS required");
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    private boolean isHttps(final HttpServletRequest request) {
        return HttpScheme.HTTPS.is(request.getScheme());
    }
}
