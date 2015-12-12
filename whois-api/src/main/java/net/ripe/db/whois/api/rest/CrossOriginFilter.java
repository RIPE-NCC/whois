package net.ripe.db.whois.api.rest;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

// Ref. http://www.w3.org/TR/cors/
public class CrossOriginFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossOriginFilter.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private static final String ALLOWED_ORIGIN = ".ripe.net";
    private static final List<String> ALLOWED_METHODS = Lists.newArrayList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.HEAD);
    private static final List<String> ALLOWED_HEADERS = Lists.newArrayList(HttpHeaders.X_REQUESTED_WITH, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.ORIGIN);

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        final String origin = requestContext.getHeaderString(HttpHeaders.ORIGIN);
        if (!StringUtils.isEmpty(origin)) {

            if (!originMatches(origin)) {
                final String path = requestContext.getUriInfo().getPath();
                LOGGER.info("Request to {}, not allowing origin {}", path, origin);
                return;
            }

            if (isSimpleRequest(requestContext)) {
                // simple cross-origin request
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            } else {
                // pre-flight cross-origin request
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, COMMA_JOINER.join(ALLOWED_METHODS));
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, COMMA_JOINER.join(ALLOWED_HEADERS));
            }
        }
    }

    private boolean isSimpleRequest(final ContainerRequestContext requestContext) {
        switch (requestContext.getMethod()) {
            case HttpMethod.GET:
            case HttpMethod.POST:
            case HttpMethod.HEAD:
                // request-method header is required for preflight requests
                return requestContext.getHeaderString(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) == null;
            default:
                return false;
        }
    }

    private boolean originMatches(final String origin) {
        try {
            return (new URL(origin)).getHost().endsWith(ALLOWED_ORIGIN);
        } catch (MalformedURLException e) {
            LOGGER.warn("malformed URL " + origin);
            return false;
        }
    }
}
