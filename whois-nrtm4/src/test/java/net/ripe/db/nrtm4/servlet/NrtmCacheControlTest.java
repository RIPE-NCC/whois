package net.ripe.db.nrtm4.servlet;

import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NrtmCacheControlTest {

    @Mock
    private ContainerRequestContext requestContext;
    @Mock
    private ContainerResponseContext responseContext;
    @Mock
    private UriInfo uriInfo;

    private MultivaluedMap headers;
    private MultivaluedMap pathParameters;
    private NrtmCacheControl subject;

    @BeforeEach
    public void setup() {
        this.subject = new NrtmCacheControl();
        this.headers = new MultivaluedHashMap();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        this.pathParameters = new MultivaluedHashMap();
        when(uriInfo.getPathParameters()).thenReturn(pathParameters);
    }


    @Test
    public void no_filename() throws IOException {
        subject.filter(requestContext, responseContext);

        assertThat(cacheControl(), is("no-cache, no-store, must-revalidate"));
    }

    @Test
    public void empty_filename() throws IOException {
        pathParameters.add("filename", "");

        subject.filter(requestContext, responseContext);

        assertThat(cacheControl(), is("no-cache, no-store, must-revalidate"));
    }

    @Test
    public void snapshot() throws IOException {
        pathParameters.add("filename", "nrtm-snapshot.32800.RIPE.db44e038-1f07-4d54-a307-1b32339f141a.87907b455ae88be2260e160bc2f740ea.json.gz");

        subject.filter(requestContext, responseContext);

        assertThat(cacheControl(), is("public, max-age=604800"));
    }

    // helper methods

    private String cacheControl() {
        return ((LinkedList)headers.get(HttpHeaders.CACHE_CONTROL)).get(0).toString();
    }

}
