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
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class WhoisCrossOriginFilter extends CrossOriginFilter {

    final protected String[] allowedHostsforCrossOrigin;

    @Autowired
    public WhoisCrossOriginFilter(@Value("${whois.allow.cross.origin.hosts}") final String[] allowedHostsforCrossOrigin) {
        this.allowedHostsforCrossOrigin = allowedHostsforCrossOrigin;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        addCORSHeaders(httpRequest, (HttpServletResponse) response);
        super.doFilter(new CrossOriginRequestWrapper(httpRequest, allowedHostsforCrossOrigin), response, chain);
    }

    private void addCORSHeaders(final HttpServletRequest request, final HttpServletResponse response) {
        if(!isOriginHeaderPresent(request)) {
            return;
        }

        final String accessControlAllowOrigin = getAccessControlAllowOriginHeader(request);
        if(accessControlAllowOrigin != null) response.setHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, accessControlAllowOrigin);

        final Boolean shouldAddCredentialHeader = isHostsAllowedForCrossOrigin(request, allowedHostsforCrossOrigin) && !"*".equals(accessControlAllowOrigin);
        response.setHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, shouldAddCredentialHeader.toString());
    }

    private String getAccessControlAllowOriginHeader(final HttpServletRequest request) {
        if (request.getMethod().equals(HttpMethod.GET)
                || request.getMethod().equals(HttpMethod.HEAD)
                || request.getMethod().equals(HttpMethod.OPTIONS)) {

            return "*";
        }

        return isHostsAllowedForCrossOrigin(request, allowedHostsforCrossOrigin) ? request.getHeader(HttpHeaders.ORIGIN) : null;
    }

    private static final class CrossOriginRequestWrapper extends HttpServletRequestWrapper {

        private final String[] allowedHostsforCrossOrigin;

        private CrossOriginRequestWrapper(final HttpServletRequest request, final String[] allowedHostsforCrossOrigin) {
            super(request);
            this.allowedHostsforCrossOrigin = allowedHostsforCrossOrigin;
        }

        @Override
        public String getQueryString() {
            if(isHostsAllowedForCrossOrigin((HttpServletRequest) getRequest(), allowedHostsforCrossOrigin)) return super.getQueryString();

            return UriBuilder.newInstance()
                    .replaceQuery(super.getQueryString())
                    .replaceQueryParam("password", null)
                    .replaceQueryParam("override", null)
                    .build().getQuery();
        }
    }

    private static boolean isOriginHeaderPresent(final HttpServletRequest request) {
        return StringUtils.isNotEmpty(request.getHeader(HttpHeaders.ORIGIN));
    }

    private static boolean isHostsAllowedForCrossOrigin(final HttpServletRequest request, final String[] allowedHostsforCrossOrigin) {
        if(!isOriginHeaderPresent(request)) return true;

        return Arrays.stream(allowedHostsforCrossOrigin).anyMatch( host -> host.equalsIgnoreCase(request.getHeader(HttpHeaders.ORIGIN)));
    }
}
