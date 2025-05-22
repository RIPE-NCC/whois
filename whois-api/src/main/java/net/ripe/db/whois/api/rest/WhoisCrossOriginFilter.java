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
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException
    {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        // GET request does not trigger a pre-flight request
        if(httpRequest.getMethod().equals(HttpMethod.GET)
                && httpRequest.getPathInfo().contains("syncupdates")
                && isCrossOrigin(httpRequest.getHeader(HttpHeaders.ORIGIN))) {

            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.UNAUTHORIZED_401, "Not Authorized");
            return;
        }

        handle((HttpServletRequest) request, (HttpServletResponse)response);
        super.doFilter(new CrossOriginRequestWrapper((HttpServletRequest) request), response, chain);
    }

    private void handle(final HttpServletRequest request, final HttpServletResponse response) {

        if ((request.getMethod().equals(HttpMethod.GET))
                || request.getMethod().equals(HttpMethod.HEAD)
                || request.getMethod().equals(HttpMethod.OPTIONS)) {

            response.setHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        }
    }

    private static final class CrossOriginRequestWrapper extends HttpServletRequestWrapper {

        private final String origin;

        private CrossOriginRequestWrapper(final HttpServletRequest request) {
            super(request);
            origin = request.getHeader(HttpHeaders.ORIGIN);
        }

        @Override
        public String getQueryString() {
            if(!isCrossOrigin(origin)) return super.getQueryString();

            final UriBuilder builder = UriBuilder.newInstance();
            builder.replaceQuery(super.getQueryString());

            builder.replaceQueryParam("password", null);
            builder.replaceQueryParam("override", null);

            return builder.build().getQuery();
        }


    }

    private static boolean isCrossOrigin(final String origin) {
        return StringUtils.isNotEmpty(origin);
    }
}
