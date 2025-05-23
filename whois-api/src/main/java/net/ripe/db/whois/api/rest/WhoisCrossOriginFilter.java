package net.ripe.db.whois.api.rest;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import java.io.IOException;

public class WhoisCrossOriginFilter extends CrossOriginFilter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        // GET request does not trigger a pre-flight request
        if(httpRequest.getMethod().equals(HttpMethod.GET)
                && httpRequest.getPathInfo().contains("syncupdates")
                && isCrossOrigin(httpRequest)) {

            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.UNAUTHORIZED_401, "Not Authorized");
            return;
        }

        addCORSHeaders(httpRequest, (HttpServletResponse) response);
        super.doFilter(new CrossOriginRequestWrapper(httpRequest), response, chain);
    }

    private void addCORSHeaders(final HttpServletRequest request, final HttpServletResponse response) {

        if(!isCrossOrigin(request)) {
            return;
        }

        if (request.getMethod().equals(HttpMethod.GET)
                || request.getMethod().equals(HttpMethod.HEAD)
                || request.getMethod().equals(HttpMethod.OPTIONS)) {

            response.setHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        }
    }

    private static final class CrossOriginRequestWrapper extends HttpServletRequestWrapper {

        private CrossOriginRequestWrapper(final HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getQueryString() {
            if(!isCrossOrigin( (HttpServletRequest) getRequest())) return super.getQueryString();

            return UriBuilder.newInstance()
                    .replaceQuery(super.getQueryString())
                    .replaceQueryParam("password", null)
                    .replaceQueryParam("override", null)
                    .build().getQuery();
        }
    }

    private static boolean isCrossOrigin(final HttpServletRequest request) {
        return StringUtils.isNotEmpty(request.getHeader(HttpHeaders.ORIGIN));
    }
}
