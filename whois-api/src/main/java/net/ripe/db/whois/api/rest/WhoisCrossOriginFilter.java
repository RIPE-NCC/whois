package net.ripe.db.whois.api.rest;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

//TODO [MA] : remove this filter when WHOIS no longer supports MD5 Password for user as well as override restructured to not use passwords
@Component
public class WhoisCrossOriginFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisCrossOriginFilter.class);

    final protected String[] allowedHostsforCrossOrigin;

    @Autowired
    public WhoisCrossOriginFilter(@Value("${whois.allow.cross.origin.hosts:}") final String[] allowedHostsforCrossOrigin) {
        this.allowedHostsforCrossOrigin = allowedHostsforCrossOrigin;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new CrossOriginRequestWrapper((HttpServletRequest) request, allowedHostsforCrossOrigin), response);
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

    private static boolean isHostsAllowedForCrossOrigin(final HttpServletRequest request, final String[] allowedHostsforCrossOrigin) {

        final String origin = request.getHeader(HttpHeaders.ORIGIN);

        if(StringUtils.isEmpty(origin)) return true;

        if(allowedHostsforCrossOrigin.length == 0 ) return false;

        try {
            final URI uri = new URI(origin);
            return Arrays.stream(allowedHostsforCrossOrigin).anyMatch( host -> host.equalsIgnoreCase(uri.getHost()));
        } catch (Exception e) {
            LOGGER.debug("Failed to parse origin header", e);
            return false;
        }
    }
}
