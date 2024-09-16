package net.ripe.db.whois.api.rest;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;

import java.io.IOException;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

public class HttpBasicAuthResponseFilter implements ContainerResponseFilter {

    @Context
    private HttpServletRequest request;

    public static final String BASIC_CHARSET_ISO_8859_1_LATIN_1 = "Basic ,charset=\"iso-8859-1 / Latin-1\"";

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        if (responseContext.getStatus() == UNAUTHORIZED.getStatusCode() && RestServiceHelper.isBasicAuth((request))) {
            responseContext.getHeaders().putSingle(HttpHeaders.WWW_AUTHENTICATE, BASIC_CHARSET_ISO_8859_1_LATIN_1);
        }
    }
}
