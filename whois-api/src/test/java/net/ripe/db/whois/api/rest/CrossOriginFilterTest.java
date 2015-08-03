package net.ripe.db.whois.api.rest;

import com.google.common.net.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrossOriginFilterTest {

    @Mock
    private ContainerRequestContext requestContext;
    @Mock
    private ContainerResponseContext responseContext;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private MultivaluedMap<String, Object> responseHeaders;

    private CrossOriginFilter subject;

    @Before
    public void setup() {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(requestContext.getHeaders()).thenReturn(new MultivaluedHashMap());
        when(responseContext.getHeaders()).thenReturn(responseHeaders);
        this.subject = new CrossOriginFilter();
    }

    @Test
    public void get_request_from_apps_db_ripe_net_is_allowed() throws Exception {
        configureRequestContext(HttpMethod.GET, "https://apps.db.ripe.net", "/some/path");

        subject.filter(requestContext, responseContext);

        verify(responseHeaders).putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://apps.db.ripe.net");
    }

    @Test
    public void get_request_from_outside_ripe_net_is_not_allowed() throws Exception {
        configureRequestContext(HttpMethod.GET, "https://www.foo.net", "/some/path");

        subject.filter(requestContext, responseContext);

        verify(responseHeaders, never()).putSingle(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), anyObject());
    }

    @Test
    public void preflight_request_from_apps_db_ripe_net_is_allowed() throws Exception {
        configureRequestContext(HttpMethod.OPTIONS, "https://apps.db.ripe.net", "/some/path");
        when(requestContext.getHeaderString(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(HttpMethod.POST);
        when(requestContext.getHeaderString(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn(HttpHeaders.X_USER_IP);

        subject.filter(requestContext, responseContext);

        verify(responseHeaders).putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://apps.db.ripe.net");
        verify(responseHeaders).putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,HEAD");
        verify(responseHeaders).putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "X-Requested-With,Content-Type,Accept,Origin");
    }

    @Test
    public void preflight_request_from_outside_ripe_net_is_not_allowed() throws Exception {
        configureRequestContext(HttpMethod.OPTIONS, "https://www.foo.net", "/some/path");
        when(requestContext.getHeaderString(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(HttpMethod.POST);
        when(requestContext.getHeaderString(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn(HttpHeaders.X_USER_IP);

        subject.filter(requestContext, responseContext);

        verify(responseHeaders, never()).putSingle(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), anyObject());
        verify(responseHeaders, never()).putSingle(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS), anyObject());
        verify(responseHeaders, never()).putSingle(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), anyObject());
    }

    @Test
    public void malformed_origin() throws Exception {
        configureRequestContext(HttpMethod.GET, "?invalid?", "/some/path");

        subject.filter(requestContext, responseContext);

        verify(responseHeaders, never()).putSingle(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), anyObject());
    }

    @Test
    public void host_and_port() throws Exception {
        configureRequestContext(HttpMethod.GET, "http://host.ripe.net:8443", "/some/path");

        subject.filter(requestContext, responseContext);

        verify(responseHeaders).putSingle(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://host.ripe.net:8443");
    }

    // helper methods

    private void configureRequestContext(final String method, final String origin, final String path) {
        when(requestContext.getMethod()).thenReturn(method);
        when(requestContext.getHeaderString(HttpHeaders.ORIGIN)).thenReturn(origin);
        when(uriInfo.getPath()).thenReturn(path);
    }

}