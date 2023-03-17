package net.ripe.db.whois.api.nrtm4;

import com.google.common.net.HttpHeaders;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

public class NrtmCacheControl  implements ContainerResponseFilter {

    public static final String NO_CACHE = "no-cache, no-store, must-revalidate";
    public static final String CACHE_ONE_MIN = "public, max-age=60";
    public static final String CACHE_ONE_WEEK = "public, max-age=604800";

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, getCacheControl(requestContext.getUriInfo().getPathParameters()));
    }

    private static String getCacheControl(final MultivaluedMap<String, String> pathParameters) {
        if(pathParameters.isEmpty() ||  !pathParameters.containsKey("filename")) {
            return NO_CACHE;
        }

        final String filename = pathParameters.get("filename").get(0);
        if(filename.startsWith(NrtmDocumentType.SNAPSHOT.getFileNamePrefix()) || filename.startsWith(NrtmDocumentType.DELTA.getFileNamePrefix())) {
            return CACHE_ONE_WEEK;
        }

        if(filename.startsWith(NrtmDocumentType.NOTIFICATION.getFileNamePrefix())) {
            return CACHE_ONE_MIN;
        }

        return NO_CACHE;
    }
}
