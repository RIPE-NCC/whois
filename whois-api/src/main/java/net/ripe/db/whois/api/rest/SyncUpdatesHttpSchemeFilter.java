package net.ripe.db.whois.api.rest;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SyncUpdatesHttpSchemeFilter implements Filter {

    @Value("${syncupdates.http.error:false}")
    private boolean errorIfHttp;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (sendError(httpRequest)){
            final HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.sendError(HttpStatus.UPGRADE_REQUIRED_426, "Please switch to HTTPS to continue using HTTPS.");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean sendError(final HttpServletRequest httpRequest) {
        return RestServiceHelper.isHttpProtocol(httpRequest) && errorIfHttp;
    }
}
