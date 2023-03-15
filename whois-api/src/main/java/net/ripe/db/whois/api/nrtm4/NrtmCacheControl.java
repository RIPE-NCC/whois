package net.ripe.db.whois.api.nrtm4;

import com.google.common.net.HttpHeaders;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

public class NrtmCacheControl  implements ContainerResponseFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        // do not cache response
        responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, "public");
        responseContext.getHeaders().putSingle(HttpHeaders.EXPIRES, "31536000");
    }
}
