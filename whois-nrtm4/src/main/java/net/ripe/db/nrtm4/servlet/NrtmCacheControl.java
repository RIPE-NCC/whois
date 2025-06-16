package net.ripe.db.nrtm4.servlet;

import com.google.common.base.Splitter;
import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class NrtmCacheControl  implements ContainerResponseFilter {

    private static final Splitter DOT_SPLITTER = Splitter.on('.').omitEmptyStrings().trimResults();

    private static final Map<String, String> filenameToCacheControl = Map.of(
                                                                    NrtmDocumentType.SNAPSHOT.getFileNamePrefix(),  "public, max-age=604800",
                                                                    NrtmDocumentType.DELTA.getFileNamePrefix(),  "public, max-age=604800",
                                                                    NrtmDocumentType.NOTIFICATION.getFileNamePrefix(),  "public, max-age=60");
    public static final String NO_CACHE = "no-cache, no-store, must-revalidate";

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, getCacheControl(requestContext.getUriInfo().getPathParameters()));
    }

    private static String getCacheControl(final MultivaluedMap<String, String> pathParameters) {
        if(pathParameters.isEmpty() ||  !pathParameters.containsKey("filename")) {
            return NO_CACHE;
        }

        final Iterator<String> split = DOT_SPLITTER.split(pathParameters.get("filename").get(0)).iterator();
        if (split.hasNext()) {
            final String cacheControl = filenameToCacheControl.get(split.next());
            if (cacheControl != null) {
                return cacheControl;
            }
        }

        return NO_CACHE;
    }
}
