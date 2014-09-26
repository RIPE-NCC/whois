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
import java.util.List;

// Ref. http://www.w3.org/TR/cors/
public class CrossOriginFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrossOriginFilter.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final List<String> allowedMethods = Lists.newArrayList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.HEAD);
    private final List<String> allowedHeaders = Lists.newArrayList(HttpHeaders.X_REQUESTED_WITH, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.ORIGIN);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final String origin = requestContext.getHeaderString(HttpHeaders.ORIGIN);
        if (!StringUtils.isEmpty(origin)) {
            final String path = requestContext.getUriInfo().getPath();

            if (isSimpleRequest(requestContext)) {
                LOGGER.debug("Request to {} is a simple cross-origin request", path);
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            } else {
                LOGGER.debug("Request to {} is a preflight cross-origin request", path);
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, COMMA_JOINER.join(allowedMethods));
                responseContext.getHeaders().putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, COMMA_JOINER.join(allowedHeaders));
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
}
