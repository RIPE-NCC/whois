package net.ripe.db.whois.api.rest;

import com.google.common.net.HttpHeaders;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

// Ref. http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
public class CacheControlFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // do not cache response
        responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        responseContext.getHeaders().putSingle(HttpHeaders.PRAGMA, "no-cache");
        responseContext.getHeaders().putSingle(HttpHeaders.EXPIRES, "0");
    }
}
